import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * [2/4] Redis ZSET Cache — Write-Through 캐시 (affectedStatuses + Lua Fence)
 * ============================================================
 *
 * 아키텍처: Redis ZSET Write-Through 캐시
 *   - 읽기: Redis ZSET ZREVRANGEBYSCORE → 캐시 미스 시 DB fallback + 비동기 warmup
 *   - 쓰기: DB UPDATE + @TransactionalEventListener → affectedStatuses 선택 ZSET 갱신
 *   - Lua Fence: 비동기 Write-Through 동시성 보호 (오래된 작업이 최신 데이터를 덮어쓰기 방지)
 *
 * 트래픽 설계 (4개 스크립트 공통):
 *   - 읽기 VU 70개: 주방 뷰(67%) + 관리자 뷰(33%), 동시 실행
 *   - 쓰기 VU 30개: ORDERED→RECEIVED(50%) + RECEIVED→COMPLETED(50%)
 *   - 실행 시간: 30초
 *
 * 성능 목표:
 *   - 읽기 p(95) < 100ms (캐시 히트 기준)
 *   - 쓰기 p(95) < 500ms
 *   - 쓰기 예상 외 에러율 < 2%
 *
 * 실행 전 Redis 초기화 (권장):
 *   docker exec yabam-redis-test redis-cli FLUSHALL
 *
 * 실행 방법:
 *   ./k6.exe run scripts/pos_cache_zset_v6.js > k6-cache-zset-v6.txt
 * ============================================================
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8011';

const USER_PASSPORTS = [
	'%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

// ── 전용 커스텀 메트릭 ────────────────────────────────────────
const cacheReadLatency  = new Trend('cache_read_duration', true);
const cacheWriteLatency = new Trend('cache_write_duration', true);
const cacheWriteErrors  = new Rate('cache_write_errors');

// ── 데이터 패턴 (full_init_data_v3.sql 기준) ──────────────────
function getOrderedId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10;
}

function getReceivedId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10 - 9;
}

export const options = {
	scenarios: {
		cache_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'read' },
		},
		cache_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'write' },
		},
	},

	thresholds: {
		'http_req_duration{scenario:cache_read}':  ['p(95)<100'],
		'http_req_duration{scenario:cache_write}': ['p(95)<500'],
		'cache_read_duration':  ['p(95)<100'],
		'cache_write_duration': ['p(95)<500'],
		'cache_write_errors':   ['rate<0.02'],
	},

	summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

// ── 캐시 워밍업: 5개 매장 전체 ZSET 초기화 ──────────────────────
export function setup() {
	console.log('[setup] Redis ZSET 캐시 워밍업 시작 (5개 매장)');
	for (let saleId = 1; saleId <= 5; saleId++) {
		const storeIdx = saleId - 1;
		const params = {
			headers: {
				'Content-Type': 'application/json',
				'X-User-Info': USER_PASSPORTS[storeIdx],
			},
			timeout: '10s',
		};
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED&pageSize=20`,
			params
		);
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=20`,
			params
		);
	}
	console.log('[setup] 캐시 워밍업 완료');
}

export default function () {
	const scenario = __ENV.MY_SCENARIO;
	const storeIdx = (__VU - 1) % 5;
	const saleId   = storeIdx + 1;
	const headers  = {
		'Content-Type': 'application/json',
		'X-User-Info': USER_PASSPORTS[storeIdx],
	};

	if (scenario === 'read') {
		runRead(saleId, headers);
	} else {
		runWrite(storeIdx, headers);
	}
}

function runRead(saleId, headers) {
	const isKitchen = (__VU % 3) !== 2;
	const statuses  = isKitchen ? 'ORDERED,RECEIVED' : 'ORDERED,RECEIVED,COMPLETED';
	const pSize     = isKitchen ? 20 : 30;

	const url = `${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=${statuses}&pageSize=${pSize}`;
	const start = Date.now();
	const res = http.get(url, {
		headers,
		tags: { scenario: 'cache_read', type: 'read' },
	});
	cacheReadLatency.add(Date.now() - start);

	check(res, {
		'cache read: status 200': (r) => r.status === 200,
	});

	sleep(isKitchen ? 1.0 : 2.0);
}

function runWrite(storeIdx, headers) {
	const seed     = __VU * 1000 + Math.floor(Date.now() / 1000);
	const isAccept = (__VU % 2) === 0;

	const orderId      = isAccept ? getOrderedId(storeIdx, seed) : getReceivedId(storeIdx, seed);
	const targetStatus = isAccept ? 'RECEIVED' : 'COMPLETED';

	const url = `${BASE_URL}/api/v1/orders/${orderId}/status?orderStatus=${targetStatus}`;
	const start = Date.now();
	const res = http.patch(url, null, {
		headers,
		tags: { scenario: 'cache_write', type: 'write' },
	});
	cacheWriteLatency.add(Date.now() - start);

	const isExpected = res.status === 200 || res.status === 400 || res.status === 422;
	cacheWriteErrors.add(!isExpected);

	check(res, {
		'cache write: 처리 완료 (200/400/422)': (r) =>
			r.status === 200 || r.status === 400 || r.status === 422,
	});

	sleep(0.5);
}

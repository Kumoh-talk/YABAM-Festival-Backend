import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * [1/4] RDB Single — DB 직접 조회 (캐시 없음)
 * ============================================================
 *
 * 아키텍처: 단일 MySQL DB 직접 조회 (bypassCache=true)
 *   - 읽기: DB 직접 SELECT (Redis 미사용)
 *   - 쓰기: DB PATCH (UPDATE + Commit)
 *
 * 트래픽 설계 (4개 스크립트 공통):
 *   - 읽기 VU 70개: 주방 뷰(67%) + 관리자 뷰(33%), 동시 실행
 *   - 쓰기 VU 30개: ORDERED→RECEIVED(50%) + RECEIVED→COMPLETED(50%)
 *   - 실행 시간: 30초
 *
 * 성능 목표:
 *   - 읽기 p(95) < 2,000ms
 *   - 쓰기 p(95) < 500ms
 *   - 쓰기 예상 외 에러율 < 2%
 *
 * 실행 방법:
 *   ./k6.exe run scripts/pos_rdb_single_v6.js > k6-rdb-single-v6.txt
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
const rdbReadLatency  = new Trend('rdb_read_duration', true);
const rdbWriteLatency = new Trend('rdb_write_duration', true);
const rdbWriteErrors  = new Rate('rdb_write_errors');

// ── 데이터 패턴 (full_init_data_v3.sql 기준) ──────────────────
// 매장 N의 ORDERED orderId: (N-1)*6000 + k*10, k=1..600
function getOrderedId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10;
}

// 매장 N의 RECEIVED orderId: (N-1)*6000 + k*10 - 9, k=1..600
function getReceivedId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10 - 9;
}

export const options = {
	scenarios: {
		rdb_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'read' },
		},
		rdb_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'write' },
		},
	},

	thresholds: {
		'http_req_duration{scenario:rdb_read}':  ['p(95)<2000'],
		'http_req_duration{scenario:rdb_write}': ['p(95)<500'],
		'rdb_read_duration':  ['p(95)<2000'],
		'rdb_write_duration': ['p(95)<500'],
		'rdb_write_errors':   ['rate<0.02'],
	},

	summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

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

	const url = `${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=${statuses}&pageSize=${pSize}&bypassCache=true`;
	const start = Date.now();
	const res = http.get(url, {
		headers,
		tags: { scenario: 'rdb_read', type: 'read' },
	});
	rdbReadLatency.add(Date.now() - start);

	check(res, {
		'rdb read: status 200': (r) => r.status === 200,
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
		tags: { scenario: 'rdb_write', type: 'write' },
	});
	rdbWriteLatency.add(Date.now() - start);

	// 400/422: 이미 처리된 주문 (도메인 정상 케이스)
	const isExpected = res.status === 200 || res.status === 400 || res.status === 422;
	rdbWriteErrors.add(!isExpected);

	check(res, {
		'rdb write: 처리 완료 (200/400/422)': (r) =>
			r.status === 200 || r.status === 400 || r.status === 422,
	});

	sleep(0.5);
}

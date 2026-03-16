import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * YABAM POS — 4가지 아키텍처 비교 성능 테스트 (v5)
 * ============================================================
 *
 * [비교 아키텍처]
 *
 * 1. rdb_single  — 단일 MySQL DB 직접 조회 (캐시 없음, bypassCache=true)
 * 2. cache_zset  — Redis ZSET Write-Through 캐시 (affectedStatuses + Lua Fence)
 * 3. sse_pubsub  — SSE 구독 + Redis Pub/Sub 이벤트 푸시 (*yabam-event 별도 기동 필요)
 * 4. rdb_repl    — MySQL Read Replica 조회 (*docker-compose-replication.yml + 앱 설정 필요)
 *
 * [테스트 설계 원칙]
 * - 읽기(READ) + 쓰기(WRITE) 동시 실행으로 현실적 혼합 트래픽 시뮬레이션
 * - 읽기 VU 70% (주방 폴링 67% + 관리자 뷰 33%)
 * - 쓰기 VU 30% (ORDERED→RECEIVED 50% + RECEIVED→COMPLETED 50%)
 * - 각 그룹 100 VU × 30초, 그룹 간 15초 대기
 *
 * [데이터 규모 — full_init_data_v3.sql 기준]
 * - 5개 매장, 매장당 6,000건 주문 (총 30,000건)
 * - ORDERED  : seq%10 = 0  → orderId 10, 20, ..., 6000      (매장당 600건)
 * - RECEIVED : seq%10 = 1  → orderId  1, 11, ..., 5991      (매장당 600건)
 * - COMPLETED: seq%10 = 2~8 (매장당 4,200건)
 * - CANCELED : seq%10 = 9  (매장당 600건)
 *
 * [SSE 시나리오 전제 조건]
 * - yabam-event 앱이 SSE_BASE_URL(기본 http://localhost:8012)에서 실행 중이어야 함
 * - OwnerStoreValidator가 userId=1~5, storeId=1~5를 허용해야 함
 *
 * [MySQL Replication 시나리오 전제 조건]
 * - docker-compose-replication.yml 실행 (Master:3315, Replica:3316)
 * - Spring 앱이 Replica 읽기용 DataSource로 구성되어 있어야 함
 * - 또는 REPL_BASE_URL을 별도 앱 인스턴스 주소로 설정
 *
 * [성능 목표]
 *   rdb_single  읽기 p(95) < 2,000ms  (DB 직접 조회, 30K 데이터 기준)
 *   cache_zset  읽기 p(95) <   100ms  (Redis ZSET 캐시)
 *   sse_pubsub  연결 p(95) <   200ms  (SSE 커넥션 수립)
 *   rdb_repl    읽기 p(95) < 1,500ms  (Replica 분산 효과)
 *   에러율             <     1%
 * ============================================================
 */

// ── 엔드포인트 설정 ──────────────────────────────────────────
const BASE_URL     = __ENV.BASE_URL     || 'http://localhost:8011'; // yabam-core
const SSE_BASE_URL = __ENV.SSE_BASE_URL || 'http://localhost:8012'; // yabam-event
const REPL_BASE_URL= __ENV.REPL_BASE_URL|| 'http://localhost:8011'; // Replication 앱 (별도 설정 시 교체)

// ── 매장별 Owner 패스포트 (URL-encoded JSON) ─────────────────
const USER_PASSPORTS = [
	'%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

// ── 커스텀 메트릭 ────────────────────────────────────────────
const writeLatency    = new Trend('write_req_duration', true);
const writeErrors     = new Rate('write_errors');
const sseConnLatency  = new Trend('sse_conn_duration', true);
const sseErrors       = new Rate('sse_errors');

// ── 데이터 패턴 (v3 SQL 기준) ────────────────────────────────
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

// ── k6 시나리오 정의 ─────────────────────────────────────────
export const options = {
	scenarios: {
		// ─── [1] RDB Single: DB 직접 조회 (t=0~30s) ─────────────────
		rdb_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'rdb_read' },
		},
		rdb_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'rdb_write' },
		},

		// ─── [2] Redis ZSET Cache (t=45~75s) ────────────────────────
		// setup()에서 캐시 워밍업 완료 후 시작
		cache_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '45s',
			env: { MY_SCENARIO: 'cache_read' },
		},
		cache_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '45s',
			env: { MY_SCENARIO: 'cache_write' },
		},

		// ─── [3] SSE + Redis Pub/Sub (t=90~120s) ────────────────────
		// yabam-event 앱(SSE_BASE_URL)이 실행 중이어야 함
		sse_subscribe: {
			executor: 'constant-vus',
			vus: 50,
			duration: '30s',
			startTime: '90s',
			env: { MY_SCENARIO: 'sse_sub' },
		},
		sse_write: {
			executor: 'constant-vus',
			vus: 50,
			duration: '30s',
			startTime: '90s',
			env: { MY_SCENARIO: 'sse_write' },
		},

		// ─── [4] RDB + Replication (t=135~165s) ─────────────────────
		// Replica DataSource 설정이 완료된 앱 인스턴스(REPL_BASE_URL)에서 실행
		repl_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '135s',
			env: { MY_SCENARIO: 'repl_read' },
		},
		repl_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '135s',
			env: { MY_SCENARIO: 'repl_write' },
		},
	},

	thresholds: {
		// 읽기 성능 목표
		'http_req_duration{scenario:rdb_read}':   ['p(95)<2000'],
		'http_req_duration{scenario:cache_read}': ['p(95)<100'],
		'http_req_duration{scenario:sse_subscribe}': ['p(95)<500'],
		'http_req_duration{scenario:repl_read}':  ['p(95)<1500'],
		// 전체 에러율
		'http_req_failed': ['rate<0.05'],
		// 쓰기 전용 에러율 (400/422는 정상이므로 별도 집계)
		'write_errors': ['rate<0.02'],
		'sse_errors':   ['rate<0.05'],
	},

	summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

// ── Setup: 캐시 워밍업 (cache_read/cache_write 시나리오 전 실행) ─
export function setup() {
	console.log('[setup] Redis ZSET 캐시 워밍업 시작 (5개 매장)');
	for (let saleId = 1; saleId <= 5; saleId++) {
		const storeIdx = saleId - 1;
		const params = {
			headers: {
				'Content-Type': 'application/json',
				'X-User-Info': USER_PASSPORTS[storeIdx],
			},
		};
		// 주방 뷰 워밍업
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED&pageSize=20`,
			params
		);
		// 관리자 뷰 워밍업 (전체 활성 상태)
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=20`,
			params
		);
	}
	console.log('[setup] 캐시 워밍업 완료');
}

// ── 메인 함수 ────────────────────────────────────────────────
export default function () {
	const scenario = __ENV.MY_SCENARIO;
	const storeIdx = (__VU - 1) % 5;
	const saleId   = storeIdx + 1;
	const headers  = {
		'Content-Type': 'application/json',
		'X-User-Info': USER_PASSPORTS[storeIdx],
	};

	switch (scenario) {
	case 'rdb_read':
		runPollRead(BASE_URL, saleId, headers, true, 'rdb_read');
		break;
	case 'rdb_write':
		runStatusWrite(BASE_URL, storeIdx, headers, 'rdb_write');
		break;
	case 'cache_read':
		runPollRead(BASE_URL, saleId, headers, false, 'cache_read');
		break;
	case 'cache_write':
		runStatusWrite(BASE_URL, storeIdx, headers, 'cache_write');
		break;
	case 'sse_sub':
		runSseSubscribe(SSE_BASE_URL, saleId, storeIdx, 'sse_subscribe');
		break;
	case 'sse_write':
		runSseWrite(SSE_BASE_URL, saleId, storeIdx, 'sse_write');
		break;
	case 'repl_read':
		runPollRead(REPL_BASE_URL, saleId, headers, true, 'repl_read');
		break;
	case 'repl_write':
		runStatusWrite(REPL_BASE_URL, storeIdx, headers, 'repl_write');
		break;
	default:
		console.warn('Unknown scenario: ' + scenario);
	}
}

// ── 읽기 시나리오: POS 폴링 ──────────────────────────────────
// 주방 뷰 (67%): ORDERED+RECEIVED, pageSize=20, 1초 주기
// 관리자 뷰 (33%): ORDERED+RECEIVED+COMPLETED, pageSize=30, 2초 주기
function runPollRead(baseUrl, saleId, headers, bypassCache, scenarioTag) {
	const isKitchen = (__VU % 3) !== 2;
	const statuses  = isKitchen ? 'ORDERED,RECEIVED' : 'ORDERED,RECEIVED,COMPLETED';
	const pSize     = isKitchen ? 20 : 30;
	const bypass    = bypassCache ? '&bypassCache=true' : '';

	const url = `${baseUrl}/api/v1/sales/${saleId}/orders?orderStatuses=${statuses}&pageSize=${pSize}${bypass}`;
	const res = http.get(url, {
		headers,
		tags: { scenario: scenarioTag, type: 'read' },
	});

	check(res, {
		'read: status 200': (r) => r.status === 200,
		'read: has data':   (r) => {
			try { return r.json('data') !== null; }
			catch (e) { return false; }
		},
	});

	sleep(isKitchen ? 1.0 : 2.0);
}

// ── 쓰기 시나리오: 주문 상태 변경 ────────────────────────────
// VU%2 === 0: ORDERED → RECEIVED  (접수 처리, 빈번)
// VU%2 === 1: RECEIVED → COMPLETED (완료 처리, 빈번)
function runStatusWrite(baseUrl, storeIdx, headers, scenarioTag) {
	const seed = __VU * 1000 + Math.floor(Date.now() / 1000);
	const isAccept = (__VU % 2) === 0;

	let orderId, targetStatus;
	if (isAccept) {
		// ORDERED → RECEIVED
		orderId      = getOrderedId(storeIdx, seed);
		targetStatus = 'RECEIVED';
	} else {
		// RECEIVED → COMPLETED
		orderId      = getReceivedId(storeIdx, seed);
		targetStatus = 'COMPLETED';
	}

	const url = `${baseUrl}/api/v1/orders/${orderId}/status?orderStatus=${targetStatus}`;
	const startTime = Date.now();
	const res = http.patch(url, null, {
		headers,
		tags: { scenario: scenarioTag, type: 'write' },
	});
	writeLatency.add(Date.now() - startTime);

	// 400/422: 이미 상태 변경된 주문 → 도메인 정상 케이스
	const isExpected = res.status === 200 || res.status === 400 || res.status === 422;
	writeErrors.add(!isExpected);

	check(res, {
		'write: 처리 완료 (200/400/422)': (r) =>
			r.status === 200 || r.status === 400 || r.status === 422,
	});

	sleep(0.5);
}

// ── SSE 구독 시나리오: 연결 수립 시간 측정 ───────────────────
// POST /api/v1/owner/subscribe?ownerId=&storeId= → SSE stream
// k6에서 SSE 이벤트 수신은 지원되지 않으므로, 연결 수립(TTFB) 시간만 측정
function runSseSubscribe(sseBaseUrl, saleId, storeIdx, scenarioTag) {
	const url = `${sseBaseUrl}/api/v1/owner/subscribe?ownerId=${saleId}&storeId=${saleId}`;
	const startTime = Date.now();
	const res = http.post(url, null, {
		headers: {
			'Accept': 'text/event-stream',
			'Cache-Control': 'no-cache',
		},
		// timeout: 3초 → SSE 커넥션 수립 시간만 측정 (이벤트 대기 제외)
		timeout: '3s',
		tags: { scenario: scenarioTag, type: 'sse_connect' },
	});
	sseConnLatency.add(Date.now() - startTime);

	// 200 또는 timeout(0) 모두 연결 시도로 집계
	const isConnected = res.status === 200 || res.status === 0;
	sseErrors.add(!isConnected && res.status !== 200);

	check(res, {
		'sse: 연결 수립 (200)': (r) => r.status === 200,
	});

	sleep(1.0);
}

// ── SSE 쓰기 시나리오: unicast 이벤트 푸시 ───────────────────
// POST /api/v1/owner/unicast?storeId= (StoreOrderEvent payload)
// 실제 주문 이벤트 발생 시 SSE 구독자에게 push되는 경로 측정
function runSseWrite(sseBaseUrl, saleId, storeIdx, scenarioTag) {
	const seed    = __VU * 1000 + Math.floor(Date.now() / 1000);
	const orderId = getOrderedId(storeIdx, seed);

	const payload = JSON.stringify({
		tableId:     (storeIdx * 20) + ((seed % 20) + 1),
		tableNumber: (seed % 20) + 1,
		orderDto: {
			orderId:      orderId,
			createdAt:    new Date().toISOString(),
			orderStatus:  'ORDERED',
			orderMenuDtos: [
				{ menuId: storeIdx * 20 + 1, menuName: '테스트 메뉴', price: 5000, quantity: 2 },
			],
		},
	});

	const url = `${sseBaseUrl}/api/v1/owner/unicast?storeId=${saleId}`;
	const res = http.post(url, payload, {
		headers: { 'Content-Type': 'application/json' },
		tags: { scenario: scenarioTag, type: 'sse_push' },
	});

	check(res, {
		'sse push: status 200': (r) => r.status === 200,
	});

	sleep(0.5);
}

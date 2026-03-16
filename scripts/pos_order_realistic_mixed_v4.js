import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * YABAM 대학 축제 POS - 현실적 혼합 트래픽 성능 테스트 (v4)
 * ============================================================
 *
 * [시나리오 설계 근거]
 *
 * 1. 실제 POS 사용 패턴
 *    대학 축제 POS는 높은 읽기/쓰기 혼합 트래픽이 발생한다:
 *    (A) POS 폴링 (읽기, 가장 빈번)
 *        - 목적: 신규 주문 실시간 확인
 *        - 쿼리: ORDERED+RECEIVED (주방 뷰) 또는 전체 (관리자 뷰)
 *        - 주기: 1~2초
 *    (B) 주문 상태 변경 (쓰기)
 *        - ORDERED → RECEIVED: 접수 처리 (빈번)
 *        - RECEIVED → COMPLETED: 완료 처리 (빈번)
 *        - 주문 취소: CANCELED (드묾)
 *
 * 2. 부하 규모
 *    5개 부스 × 피크타임:
 *    - 읽기 VU: 80 (POS 폴링)
 *    - 쓰기 VU: 20 (상태 변경)
 *    - 총 100 VU
 *
 * 3. 3가지 비교 시나리오
 *
 *    [1] no_cache (t=0s, 30s)
 *        - bypassCache=true, 읽기 전용
 *        - 매 요청마다 DB 직접 조회
 *        - 기준선(Baseline): DB 성능 측정
 *
 *    [2] cache_read_only (t=45s, 30s)
 *        - bypassCache=false, 읽기 전용
 *        - Redis ZSET 캐시 히트 경로
 *        - 쓰기 없음 → 캐시 갱신 부하 없음
 *        - 순수 캐시 읽기 성능 측정
 *
 *    [3] cache_mixed_write (t=90s, 30s)
 *        - bypassCache=false, 읽기 80% + 쓰기 20% 혼합
 *        - 쓰기 발생 시마다 Write-Through 캐시 갱신 트리거
 *        - 실제 현장 환경 시뮬레이션
 *        - 캐시 갱신 부하가 읽기 성능에 미치는 영향 측정
 *
 * 4. 데이터 규모 (v3 더미 데이터 기준)
 *    - 30,000건 주문 (5매장 × 6,000건/매장)
 *    - ORDERED orderId 패턴: i%10==0 → 10, 20, ..., 6000 (매장당 600건)
 *    - 매장 N의 ORDERED 범위: (N-1)*6000+10, ..., N*6000 (10 단위)
 *
 * 5. 성능 목표
 *    no_cache          p(95) < 3,000ms  (DB 직접 조회 기준)
 *    cache_read_only   p(95) <   100ms  (순수 Redis 캐시 목표)
 *    cache_mixed_write p(95) <   200ms  (쓰기 부하 포함 캐시 목표)
 *    에러율                  <     1%
 * ============================================================
 */

const BASE_URL = 'http://localhost:8011';

// 매장별 owner 패스포트 (URL-encoded JSON)
const USER_PASSPORTS = [
	'%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

// 커스텀 메트릭
const writeLatency = new Trend('write_req_duration', true);
const writeErrors = new Rate('write_errors');
const cacheHitRate = new Rate('cache_reads');

// 매장 N의 ORDERED orderId 풀 (10 단위, 매장당 600개)
// Store N: (N-1)*6000 + 10*k, k=1..600
function getOrderedOrderId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1; // 1..600
	return base + k * 10;
}

// 매장 N의 RECEIVED orderId 풀 (i%10==1 → orderId 1,11,21... per store)
// 실제 RECEIVED 여부는 런타임에 확인되므로 orderId만 전달
function getReceivedOrderId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10 - 9; // 1, 11, 21, ... → RECEIVED orderId 근사값
}

export const options = {
	scenarios: {
		// ─── [1] no_cache: DB 직접 조회 (기준선) ───────────────────────
		no_cache: {
			executor: 'constant-vus',
			vus: 100,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'no_cache' },
		},
		// ─── [2] cache_read_only: 순수 캐시 읽기 ───────────────────────
		// baseline 종료 후 15초 대기 (DB 안정화 + setup warm-up 반영)
		cache_read_only: {
			executor: 'constant-vus',
			vus: 100,
			duration: '30s',
			startTime: '45s',
			env: { MY_SCENARIO: 'cache_read_only' },
		},
		// ─── [3] cache_mixed_write: 캐시 읽기 + 쓰기 혼합 ─────────────
		// cache_read_only 종료 후 15초 대기 (캐시 안정화)
		cache_mixed_write: {
			executor: 'constant-vus',
			vus: 100,
			duration: '30s',
			startTime: '90s',
			env: { MY_SCENARIO: 'cache_mixed_write' },
		},
	},
	thresholds: {
		'http_req_duration{scenario:no_cache}':         ['p(95)<3000'],
		'http_req_duration{scenario:cache_read_only}':  ['p(95)<100'],
		'http_req_duration{scenario:cache_mixed_write}': ['p(95)<200'],
		'http_req_failed': ['rate<0.01'],
	},
	summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

/**
 * setup(): 캐시 워밍업
 * cache_read_only/cache_mixed_write 시나리오 시작 전에 모든 매장의 ZSET을 초기화한다.
 */
export function setup() {
	for (let saleId = 1; saleId <= 5; saleId++) {
		const storeIdx = saleId - 1;
		const params = {
			headers: {
				'Content-Type': 'application/json',
				'X-User-Info': USER_PASSPORTS[storeIdx],
			},
		};

		// 주방 뷰 warm-up (ORDERED+RECEIVED)
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED&pageSize=20`,
			params
		);
		// 관리자 뷰 warm-up (전체 활성 상태) → 캐시 전체 적재
		http.get(
			`${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=20`,
			params
		);
	}
	console.log('[setup] 5개 매장 캐시 워밍업 완료');
}

export default function () {
	const scenario = __ENV.MY_SCENARIO;

	// VU를 5개 매장에 균등 분산
	const storeIdx = (__VU - 1) % 5;
	const saleId = storeIdx + 1;

	const headers = {
		'Content-Type': 'application/json',
		'X-User-Info': USER_PASSPORTS[storeIdx],
	};

	if (scenario === 'cache_mixed_write' && __VU % 5 === 0) {
		// ─── 쓰기 VU (전체 VU의 20%) ─────────────────────────────────
		// 상태 변경: ORDERED→RECEIVED (주문 접수 처리 시뮬레이션)
		runWriteScenario(saleId, storeIdx, headers);
	} else {
		// ─── 읽기 VU (80% 또는 전체) ─────────────────────────────────
		runReadScenario(scenario, saleId, headers);
	}
}

/**
 * 읽기 시나리오: POS 폴링
 * - 67%: 주방 뷰 (ORDERED+RECEIVED, 1초 주기)
 * - 33%: 관리자 전체 뷰 (ORDERED+RECEIVED+COMPLETED, 2초 주기)
 */
function runReadScenario(scenario, saleId, headers) {
	const isKitchenPoll = (__VU % 3) !== 2;
	const statuses = isKitchenPoll ? 'ORDERED,RECEIVED' : 'ORDERED,RECEIVED,COMPLETED';
	const pSize = isKitchenPoll ? 20 : 30;

	const base = `${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=${statuses}&pageSize=${pSize}`;
	const url = scenario === 'no_cache' ? `${base}&bypassCache=true` : base;

	const res = http.get(url, { headers, tags: { scenario: scenario, type: 'read' } });

	check(res, {
		'status 200': (r) => r.status === 200,
		'has data':   (r) => {
			try { return r.json('data') !== null; }
			catch (e) { return false; }
		},
	});

	if (scenario !== 'no_cache') {
		cacheHitRate.add(res.status === 200);
	}

	sleep(isKitchenPoll ? 1.0 : 2.0);
}

/**
 * 쓰기 시나리오: 주문 상태 변경 (ORDERED→RECEIVED)
 * - ORDERED 상태 orderId 풀에서 랜덤 선택
 * - 실패(이미 상태 변경됨 등)는 에러로 집계하지 않음
 */
function runWriteScenario(saleId, storeIdx, headers) {
	// 매 이터레이션마다 다른 orderId 선택 (VU 번호 + 이터레이션 번호 기반 pseudo-random)
	const seed = __VU * 1000 + Math.floor(Date.now() / 1000);
	const orderId = getOrderedOrderId(storeIdx, seed);

	const patchUrl = `${BASE_URL}/api/v1/orders/${orderId}/status?orderStatus=RECEIVED`;
	const startTime = Date.now();
	const patchRes = http.patch(patchUrl, null, {
		headers,
		tags: { scenario: 'cache_mixed_write', type: 'write' },
	});
	writeLatency.add(Date.now() - startTime);

	// 상태 전이 실패(422 등)는 정상 케이스로 처리 (이미 변경된 주문)
	const isSuccess = patchRes.status === 200 || patchRes.status === 422 || patchRes.status === 400;
	writeErrors.add(!isSuccess);

	check(patchRes, {
		'write: 처리 완료 (200/400/422)': (r) =>
			r.status === 200 || r.status === 422 || r.status === 400,
	});

	sleep(0.5); // 쓰기는 폴링보다 느린 빈도로 발생
}

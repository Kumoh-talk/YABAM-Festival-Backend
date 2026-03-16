import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * [3/4] SSE + Redis Pub/Sub — 서버 푸시 이벤트 아키텍처
 * ============================================================
 *
 * 아키텍처: SSE 롱폴링 + 이벤트 서버 unicast 푸시
 *   - 구독(읽기): POST /api/v1/owner/subscribe → SSE 커넥션 수립 시간(TTFB) 측정
 *   - 푸시(쓰기): POST /api/v1/owner/unicast   → StoreOrderEvent 직접 푸시 응답 시간 측정
 *
 * !! 전제 조건 !!
 *   yabam-event 앱이 SSE_BASE_URL(기본 http://localhost:8012)에서 실행 중이어야 함
 *
 *   Kafka 없이 기동하려면 (테스트용):
 *     ./gradlew :application:yabam:yabam-event:bootRun \
 *       --args='--spring.profiles.active=local --server.port=8012
 *               --spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration'
 *
 * 트래픽 설계 (4개 스크립트 공통):
 *   - 구독 VU 50개: SSE 커넥션 수립 시도 (1초 주기)
 *   - 푸시 VU 50개: unicast 이벤트 푸시 (0.5초 주기)
 *   - 실행 시간: 30초
 *
 * NOTE: k6는 SSE 스트림을 수신하는 것을 지원하지 않습니다.
 *       - 구독 시나리오: TTFB(Time-To-First-Byte) = 커넥션 수립 레이턴시만 측정
 *       - 3초 타임아웃 설정으로 스트림 수신 대기를 조기 종료
 *       - 200 또는 timeout(status=0) 모두 커넥션 시도로 집계
 *
 * 성능 목표:
 *   - 구독 연결 p(95) < 200ms
 *   - unicast 푸시 p(95) < 100ms
 *   - SSE 연결 에러율 < 5%
 *
 * 실행 방법:
 *   ./k6.exe run scripts/pos_sse_pubsub_v6.js > k6-sse-pubsub-v6.txt
 *   또는:
 *   ./k6.exe run scripts/pos_sse_pubsub_v6.js \
 *     --env SSE_BASE_URL=http://localhost:8012 > k6-sse-pubsub-v6.txt
 * ============================================================
 */

const SSE_BASE_URL = __ENV.SSE_BASE_URL || 'http://localhost:8012';
const CORE_BASE_URL = __ENV.BASE_URL    || 'http://localhost:8011';

// ── 전용 커스텀 메트릭 ────────────────────────────────────────
const sseConnLatency = new Trend('sse_conn_duration', true);
const ssePushLatency = new Trend('sse_push_duration', true);
const sseConnErrors  = new Rate('sse_conn_errors');
const ssePushErrors  = new Rate('sse_push_errors');

// ── 데이터 패턴 (full_init_data_v3.sql 기준) ──────────────────
function getOrderedId(storeIdx, seed) {
	const base = storeIdx * 6000;
	const k = (seed % 600) + 1;
	return base + k * 10;
}

export const options = {
	scenarios: {
		// SSE 구독 VU: 커넥션 수립 시간 측정
		sse_subscribe: {
			executor: 'constant-vus',
			vus: 50,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'subscribe' },
		},
		// SSE 쓰기 VU: unicast 이벤트 푸시 응답 시간 측정
		sse_push: {
			executor: 'constant-vus',
			vus: 50,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'push' },
		},
	},

	thresholds: {
		'http_req_duration{scenario:sse_subscribe}': ['p(95)<200'],
		'http_req_duration{scenario:sse_push}':      ['p(95)<100'],
		'sse_conn_duration': ['p(95)<200'],
		'sse_push_duration': ['p(95)<100'],
		'sse_conn_errors':   ['rate<0.05'],
		'sse_push_errors':   ['rate<0.05'],
	},

	summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

export default function () {
	const scenario = __ENV.MY_SCENARIO;
	const storeIdx = (__VU - 1) % 5;
	const saleId   = storeIdx + 1;

	if (scenario === 'subscribe') {
		runSseSubscribe(saleId, storeIdx);
	} else {
		runSsePush(saleId, storeIdx);
	}
}

// ── SSE 구독: 커넥션 수립 시간 측정 ──────────────────────────
// 3초 타임아웃으로 TTFB만 측정 (스트림 전체 대기 제외)
function runSseSubscribe(saleId, storeIdx) {
	const url = `${SSE_BASE_URL}/api/v1/owner/subscribe?ownerId=${saleId}&storeId=${saleId}`;
	const start = Date.now();
	const res = http.post(url, null, {
		headers: {
			'Accept': 'text/event-stream',
			'Cache-Control': 'no-cache',
		},
		timeout: '3s',
		tags: { scenario: 'sse_subscribe', type: 'sse_connect' },
	});
	sseConnLatency.add(Date.now() - start);

	// 200: 연결 성공, 0: 타임아웃(스트림 열림 상태에서 정상 종료)
	const isOk = res.status === 200 || res.status === 0;
	sseConnErrors.add(!isOk);

	check(res, {
		'sse subscribe: 연결 수립 (200 또는 timeout)': (r) =>
			r.status === 200 || r.status === 0,
	});

	sleep(1.0);
}

// ── SSE 푸시: unicast 이벤트 직접 발행 응답 시간 측정 ──────────
// POST /api/v1/owner/unicast?storeId={saleId}
// StoreOrderEvent 페이로드를 직접 전송 (Kafka 우회)
function runSsePush(saleId, storeIdx) {
	const seed    = __VU * 1000 + Math.floor(Date.now() / 1000);
	const orderId = getOrderedId(storeIdx, seed);
	const tableNumber = (seed % 20) + 1;

	const payload = JSON.stringify({
		tableId:     storeIdx * 20 + tableNumber,
		tableNumber: tableNumber,
		orderDto: {
			orderId:       orderId,
			createdAt:     new Date().toISOString(),
			orderStatus:   'ORDERED',
			orderMenuDtos: [
				{ menuId: storeIdx * 20 + 1, menuName: '테스트 메뉴', price: 5000, quantity: 2 },
			],
		},
	});

	const url = `${SSE_BASE_URL}/api/v1/owner/unicast?storeId=${saleId}`;
	const start = Date.now();
	const res = http.post(url, payload, {
		headers: { 'Content-Type': 'application/json' },
		tags: { scenario: 'sse_push', type: 'sse_push' },
	});
	ssePushLatency.add(Date.now() - start);

	const isOk = res.status === 200;
	ssePushErrors.add(!isOk);

	check(res, {
		'sse push: status 200': (r) => r.status === 200,
	});

	sleep(0.5);
}

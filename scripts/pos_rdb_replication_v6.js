import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ============================================================
 * [4/4] RDB + MySQL Replication — 읽기/쓰기 분산
 * ============================================================
 *
 * 아키텍처: MySQL Master-Replica Read/Write Split
 *   - 읽기: Replica DB (READ_ONLY, SELECT 전용) → Master 쓰기 부하와 격리
 *   - 쓰기: Master DB (INSERT/UPDATE/DELETE)
 *
 * !! 전제 조건 !!
 *   docker-compose-replication.yml 기동 (Master:3315, Replica:3316):
 *     docker-compose -f docker-compose-replication.yml up -d
 *     bash scripts/init-replication.sh
 *     docker exec -i yabam-mysql-master mysql -uroot -p1234 local_mydb < ./scripts/full_init_data_v3.sql
 *
 *   Spring 앱을 Replication 프로파일로 기동 (REPL_BASE_URL):
 *     ./gradlew :application:yabam:yabam-core:bootRun \
 *       --args='--spring.profiles.active=local,replication --server.port=8013'
 *
 *   기본 동작 (REPL_BASE_URL 미지정):
 *     단일 MySQL(3315) 앱(8011)에 bypassCache=true로 요청 — Replication 효과는 측정 안 됨
 *     → 비교 기준선으로 활용 가능
 *
 * 트래픽 설계 (4개 스크립트 공통):
 *   - 읽기 VU 70개: 주방 뷰(67%) + 관리자 뷰(33%), 동시 실행
 *   - 쓰기 VU 30개: ORDERED→RECEIVED(50%) + RECEIVED→COMPLETED(50%)
 *   - 실행 시간: 30초
 *
 * 성능 목표:
 *   - 읽기 p(95) < 1,500ms (Replica 분산 효과)
 *   - 쓰기 p(95) < 500ms
 *   - 쓰기 예상 외 에러율 < 2%
 *
 * 실행 방법:
 *   # Replication 앱이 8013에서 기동된 경우:
 *   ./k6.exe run scripts/pos_rdb_replication_v6.js \
 *     --env REPL_BASE_URL=http://localhost:8013 > k6-rdb-replication-v6.txt
 *
 *   # 비교 기준선(단일 DB, replication 효과 없음):
 *   ./k6.exe run scripts/pos_rdb_replication_v6.js > k6-rdb-replication-v6.txt
 * ============================================================
 */

// 읽기: Replication 앱 URL (Replica DataSource)
// 쓰기: 항상 Master(단일 앱) URL — Replication 라우팅은 앱 내부에서 처리
const REPL_BASE_URL = __ENV.REPL_BASE_URL || 'http://localhost:8011';
const BASE_URL      = __ENV.BASE_URL      || 'http://localhost:8011';

const USER_PASSPORTS = [
	'%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
	'%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

// ── 전용 커스텀 메트릭 ────────────────────────────────────────
const replReadLatency  = new Trend('repl_read_duration', true);
const replWriteLatency = new Trend('repl_write_duration', true);
const replWriteErrors  = new Rate('repl_write_errors');

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
		repl_read: {
			executor: 'constant-vus',
			vus: 70,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'read' },
		},
		repl_write: {
			executor: 'constant-vus',
			vus: 30,
			duration: '30s',
			startTime: '0s',
			env: { MY_SCENARIO: 'write' },
		},
	},

	thresholds: {
		'http_req_duration{scenario:repl_read}':  ['p(95)<1500'],
		'http_req_duration{scenario:repl_write}': ['p(95)<500'],
		'repl_read_duration':  ['p(95)<1500'],
		'repl_write_duration': ['p(95)<500'],
		'repl_write_errors':   ['rate<0.02'],
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

// 읽기는 Replica 앱 URL로 전송 (Spring 내부에서 readOnly DataSource로 라우팅)
function runRead(saleId, headers) {
	const isKitchen = (__VU % 3) !== 2;
	const statuses  = isKitchen ? 'ORDERED,RECEIVED' : 'ORDERED,RECEIVED,COMPLETED';
	const pSize     = isKitchen ? 20 : 30;

	const url = `${REPL_BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=${statuses}&pageSize=${pSize}&bypassCache=true`;
	const start = Date.now();
	const res = http.get(url, {
		headers,
		tags: { scenario: 'repl_read', type: 'read' },
	});
	replReadLatency.add(Date.now() - start);

	check(res, {
		'repl read: status 200': (r) => r.status === 200,
	});

	sleep(isKitchen ? 1.0 : 2.0);
}

// 쓰기는 Master 앱 URL로 전송 (Spring 내부에서 master DataSource로 라우팅)
function runWrite(storeIdx, headers) {
	const seed     = __VU * 1000 + Math.floor(Date.now() / 1000);
	const isAccept = (__VU % 2) === 0;

	const orderId      = isAccept ? getOrderedId(storeIdx, seed) : getReceivedId(storeIdx, seed);
	const targetStatus = isAccept ? 'RECEIVED' : 'COMPLETED';

	const url = `${BASE_URL}/api/v1/orders/${orderId}/status?orderStatus=${targetStatus}`;
	const start = Date.now();
	const res = http.patch(url, null, {
		headers,
		tags: { scenario: 'repl_write', type: 'write' },
	});
	replWriteLatency.add(Date.now() - start);

	const isExpected = res.status === 200 || res.status === 400 || res.status === 422;
	replWriteErrors.add(!isExpected);

	check(res, {
		'repl write: 처리 완료 (200/400/422)': (r) =>
			r.status === 200 || r.status === 400 || r.status === 422,
	});

	sleep(0.5);
}

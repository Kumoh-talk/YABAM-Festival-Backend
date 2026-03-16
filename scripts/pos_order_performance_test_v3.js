import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * ============================================================
 * YABAM 대학 축제 POS - 현실적 성능 테스트 시나리오 (v3)
 * ============================================================
 *
 * [시나리오 설계 근거]
 *
 * 1. 실제 POS 사용 패턴 분석
 *    대학 축제 POS는 두 종류의 폴링이 발생한다:
 *    (A) 주방 디스플레이 폴링 (가장 빈번)
 *        - 목적: 신규 주문 접수 즉시 확인, 조리 우선순위 파악
 *        - 쿼리: ORDERED + RECEIVED (미처리 주문만)
 *        - 주기: 1~2초 (테스트: 1.0s)
 *        - 비율: 전체 VU의 67% (VU % 3 != 2)
 *    (B) 관리자/홀 전체 뷰 (덜 빈번)
 *        - 목적: 전체 주문 현황 파악, 테이블 서비스 관리
 *        - 쿼리: ORDERED + RECEIVED + COMPLETED
 *        - 주기: 2~3초 (테스트: 2.0s)
 *        - 비율: 전체 VU의 33% (VU % 3 == 2)
 *
 * 2. 부하 규모 설계
 *    5개 부스 × 피크타임 기준:
 *    - 주방 단말: 부스당 2-3대 → 부스당 12 VU (여유분 포함)
 *    - 홀 관리자: 부스당 1-2명 → 부스당 8 VU
 *    - 총 VU: 5 × (12 + 8) = 100 VU (피크타임 최악 시나리오)
 *
 *    실제 RPS 추정:
 *    - 주방 폴링: 67 VU × (1 req / 1.0s) ≒ 67 RPS
 *    - 관리자 뷰: 33 VU × (1 req / 2.0s) ≒ 17 RPS
 *    - 총 합산: ~84 RPS (5개 매장 합산, 매장당 ~17 RPS)
 *
 * 3. 데이터 규모 (v3 더미 데이터 기준)
 *    - 30,000건 주문 (5매장 × 6,000건/매장)
 *    - 매장당 캐시 대상 (ORDERED+RECEIVED+COMPLETED): 5,400건
 *    - 매장당 주방 폴링 대상 (ORDERED+RECEIVED): 1,200건
 *    - Redis ZSET: sale:{id}:orders:{STATUS} 키별 분리
 *
 * 4. 비교 시나리오
 *    baseline: bypassCache=true → 3단계 QueryDSL (MySQL IN 쿼리 + 조인)
 *    cached  : 정상 캐시 경로  → Redis ZSET ZREVRANGEBYSCORE
 *
 *    cached 시나리오 시작 전 명시적 cache warm-up:
 *    - setup() 함수에서 매장별 1회 호출 → ZSET 초기화
 *    - 이후 모든 요청은 캐시 HIT 상태
 *
 * 5. 임계값 (성능 목표)
 *    baseline p(95) < 3,000ms  (QueryDSL 3단계, 30,000건 기준)
 *    cached   p(95) <   100ms  (Redis ZSET 조회 목표)
 *    에러율        <     1%    (서비스 안정성)
 * ============================================================
 */

const BASE_URL = 'http://localhost:8011';

// 매장별 owner userId (saleId == ownerId 가정, v3 더미 데이터와 일치)
const USER_PASSPORTS = [
    '%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

export const options = {
    scenarios: {
        // ─── Baseline: bypassCache=true, DB 직접 조회 ───────────────────
        baseline: {
            executor: 'constant-vus',
            vus: 100,
            duration: '30s',
            startTime: '0s',
            env: { MY_SCENARIO: 'baseline' },
        },
        // ─── Cached: Redis ZSET 캐시 조회 ───────────────────────────────
        // baseline 종료 후 15초 대기 (DB 안정화 + 캐시 warm-up 완료 보장)
        cached: {
            executor: 'constant-vus',
            vus: 100,
            duration: '30s',
            startTime: '45s',
            env: { MY_SCENARIO: 'cached' },
        },
    },
    thresholds: {
        'http_req_duration{scenario:baseline}': ['p(95)<3000'],
        'http_req_duration{scenario:cached}':   ['p(95)<100'],
        'http_req_failed':                       ['rate<0.01'],
    },
    summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

/**
 * setup(): cached 시나리오 시작 전 캐시 워밍업
 * k6 실행 전 한 번만 호출되며, 5개 매장 각각에 대해
 * 주방 뷰 + 관리자 뷰 쿼리를 1회씩 날려 Redis ZSET을 초기화한다.
 *
 * 근거: baseline 테스트 중 DB 쿼리가 cache miss에 의한 warm-up 트리거를 발생시키지만,
 *       cached 시나리오 시작 시점(t=45s)에 warm-up이 완료됐다고 보장이 안 됨.
 *       명시적 warm-up으로 캐시 히트율 100% 보장.
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

        // 관리자 뷰 warm-up (ORDERED+RECEIVED+COMPLETED) → 캐시 전체 적재
        http.get(
            `${BASE_URL}/api/v1/sales/${saleId}/orders?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=20`,
            params
        );
    }
    console.log('[setup] Cache warm-up completed for all 5 sales.');
}

export default function () {
    const scenario = __ENV.MY_SCENARIO;

    // VU를 5개 매장에 균등 분산 (1-based VU 번호)
    const storeIdx = (__VU - 1) % 5;
    const saleId   = storeIdx + 1;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-User-Info': USER_PASSPORTS[storeIdx],
        },
        tags: { scenario: scenario },
    };

    // ─── 쿼리 패턴 분기 ──────────────────────────────────────────────
    // VU % 3 in [0, 1] (67%): 주방 폴링 — ORDERED+RECEIVED
    //   근거: 부스당 주방 단말이 홀 단말보다 많음 (2:1 비율)
    // VU % 3 == 2 (33%): 관리자 뷰 — ORDERED+RECEIVED+COMPLETED
    //   근거: 전체 주문 현황 파악이 필요한 관리자/홀 단말
    const isKitchenPoll = (__VU % 3) !== 2;
    const statuses = isKitchenPoll
        ? 'ORDERED,RECEIVED'
        : 'ORDERED,RECEIVED,COMPLETED';
    const pSize = isKitchenPoll ? 20 : 30; // 관리자 뷰는 더 많은 데이터 필요

    const base = `${BASE_URL}/api/v1/sales/${saleId}/orders`
        + `?orderStatuses=${statuses}&pageSize=${pSize}`;
    const url = scenario === 'baseline' ? `${base}&bypassCache=true` : base;

    const res = http.get(url, params);

    check(res, {
        'status 200': (r) => r.status === 200,
        'has data': (r) => {
            try { return r.json('data') !== null; }
            catch (e) { return false; }
        },
    });

    // ─── 폴링 주기 ────────────────────────────────────────────────────
    // 주방 폴링: 1.0s (실제 1~2s 중 보수적 1s 적용)
    // 관리자 뷰: 2.0s (실제 2~3s 중 보수적 2s 적용)
    //
    // 예상 RPS:
    //   주방: 67 VU / 1.0s = 67 RPS
    //   관리자: 33 VU / 2.0s = 17 RPS
    //   합산: ~84 RPS total / 5매장 = 매장당 ~17 RPS
    sleep(isKitchenPoll ? 1.0 : 2.0);
}

import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * ============================================================
 * YABAM 대학 축제 POS - 현실적 성능 테스트 시나리오 (v2)
 * ============================================================
 *
 * [시나리오 설계 근거]
 *  실제 대학 축제 환경:
 *  - 5개 부스(매장)가 동시 운영
 *  - 각 부스: POS 단말 2~3대, 직원 1~2명이 폴링
 *  - 폴링 주기: 실제 1~3초 → 테스트는 0.5s (부하 증폭)
 *  - 동시 VU: 매장당 20 VU = 5개 매장 × 20 = 100 VU 총계
 *    (피크 타임 매장당 20개 기기/탭이 동시 폴링하는 최악 시나리오)
 *
 * [데이터 규모]
 *  - 5개 매장, 각 2,000건 = 총 10,000건 주문
 *  - 상태: ORDERED 30% / RECEIVED 10% / COMPLETED 50% / CANCELED 10%
 *  - 폴링 대상(ORDERED+RECEIVED+COMPLETED): 매장당 1,800건
 *
 * [쿼리 패턴]
 *  - 각 VU: 자신의 매장(saleId = (__VU - 1) % 5 + 1) 주문 조회
 *  - pageSize=20 (주방 화면에 표시되는 최신 20건)
 *  - userId도 매장별로 분리 (saleId == ownerId 가정)
 *
 * [비교 시나리오]
 *  baseline : bypassCache=true → 3단계 QueryDSL (RDB 직접)
 *  cached   : 정상 캐시 경로   → Redis ZSET reverseRangeByScore
 *
 * [임계값]
 *  baseline p(95) < 2000ms  (인덱스 최적화된 DB 쿼리 기준)
 *  cached   p(95) <  100ms  (Redis 기준 목표)
 *  에러율   < 1%
 * ============================================================
 */
export const options = {
    scenarios: {
        baseline: {
            executor: 'constant-vus',
            vus: 100,
            duration: '30s',
            startTime: '0s',
            env: { MY_SCENARIO: 'baseline' },
        },
        cached: {
            executor: 'constant-vus',
            vus: 100,
            duration: '30s',
            startTime: '40s',   // baseline 완료 후 10s 대기
            env: { MY_SCENARIO: 'cached' },
        },
    },
    thresholds: {
        'http_req_duration{scenario:baseline}': ['p(95)<2000'],
        'http_req_duration{scenario:cached}':   ['p(95)<100'],
        'http_req_failed':                       ['rate<0.01'],
    },
    summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
};

const BASE_URL = 'http://localhost:8011';

// 매장별 owner userId (1~5)
const USER_PASSPORTS = [
    '%7B%22userId%22%3A1%2C%22userNickname%22%3A%22owner1%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A2%2C%22userNickname%22%3A%22owner2%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A3%2C%22userNickname%22%3A%22owner3%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A4%2C%22userNickname%22%3A%22owner4%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
    '%7B%22userId%22%3A5%2C%22userNickname%22%3A%22owner5%22%2C%22userRole%22%3A%22ROLE_OWNER%22%7D',
];

export default function () {
    const scenario = __ENV.MY_SCENARIO;

    // VU를 5개 매장에 균등 분산 (VU 번호 1-based)
    const storeIdx = (__VU - 1) % 5;
    const saleId   = storeIdx + 1;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-User-Info': USER_PASSPORTS[storeIdx],
        },
        tags: { scenario: scenario },
    };

    // ORDERED+RECEIVED+COMPLETED: 주방 화면 + 완료 확인용 (폴링 전체 뷰)
    const base = `${BASE_URL}/api/v1/sales/${saleId}/orders`
        + `?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=20`;
    const url = scenario === 'baseline' ? `${base}&bypassCache=true` : base;

    const res = http.get(url, params);

    check(res, {
        'status 200': (r) => r.status === 200,
        'has data':   (r) => {
            try { return r.json('data') !== null; }
            catch (e) { return false; }
        },
    });

    // 0.5s sleep: VU당 ~2 req/s → 100 VU × 2 = 약 200 RPS
    // 매장당 20 VU × 2 = 40 RPS/매장 (피크 타임 현실적 수준)
    sleep(0.5);
}

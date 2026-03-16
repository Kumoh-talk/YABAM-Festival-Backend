-- =============================================================================
-- YABAM 대학 축제 POS 현실적 더미 데이터 (v3)
--
-- [설계 근거 - 3일 대학 축제 시뮬레이션]
--
-- 1. 매장 규모 (5개 부스)
--    금오공과대학교 축제 부스 5개 동시 운영을 가정
--    각 부스: 20개 테이블, 4인석
--
-- 2. 테이블 회전 (테이블당 30회)
--    - 축제 운영: 11:00-22:00 = 11시간/일 × 3일 = 33시간
--    - 평균 체류시간: 약 1시간 (그룹당) → 테이블 33회전 ≒ 30회 (보수적 추정)
--    - 30 receipts/table × 20 tables = 600 receipts/store
--
-- 3. 주문 빈도 (영수증당 10개)
--    - 평균 주문 횟수: 1그룹당 초기 주문 3건 + 추가 주문 2건 × 2회 = 7건
--    - 테이블 회전 경계 오버헤드 포함하여 10건으로 설정
--    - 5 stores × 20 tables × 30 receipts × 10 orders = 30,000건
--
-- 4. 상태 분포 (seq%10 기준)
--    ORDERED   10% (seq%10 = 0)     - 방금 접수된 신규 주문 (활성)
--    RECEIVED  10% (seq%10 = 1)     - 주방 접수 완료, 조리 중 (활성)
--    COMPLETED 70% (seq%10 = 2~8)   - 서빙 완료 (과거 누적, 가장 많음)
--    CANCELED  10% (seq%10 = 9)     - 취소 (비활성)
--
--    → 캐시 대상 (ORDERED+RECEIVED+COMPLETED): 90% × 6,000 = 5,400건/매장
--    → 실제 주방 폴링 대상 (ORDERED+RECEIVED): 20% × 6,000 = 1,200건/매장
--    → 현실 반영: 축제 중반 이후 완료 주문이 대부분을 차지하는 패턴
--
-- 5. 메뉴 구성 (매장당 20개)
--    카테고리 4개 × 메뉴 5개 = 20개 (실제 부스 메뉴판 기준)
--    주문당 메뉴: 2~3개 (seq 기반 순환)
--
-- [성능 테스트 영향]
--    - DB baseline 쿼리: 3단계 QueryDSL
--        1단계 IN 쿼리: 600 receipt_ids per store (기존 200의 3배)
--        2단계 스캔: 최대 5,400건 필터링
--        3단계 조인: pageSize(20) × 2-3 메뉴 = ~60 rows
--    - Redis cached 쿼리: ZREVRANGEBYSCORE on 5,400-entry ZSET → O(log N + M)
-- =============================================================================

USE local_mydb;

-- 기존 데이터 전체 삭제 (외래키 순서 준수)
DELETE FROM order_menus;
DELETE FROM orders;
DELETE FROM receipts;
DELETE FROM tables;
DELETE FROM menus;
DELETE FROM menu_categories;
DELETE FROM sales;
DELETE FROM stores;

-- AUTO_INCREMENT 초기화
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE order_menus AUTO_INCREMENT = 1;
ALTER TABLE menus AUTO_INCREMENT = 1;
ALTER TABLE menu_categories AUTO_INCREMENT = 1;
ALTER TABLE sales AUTO_INCREMENT = 1;
ALTER TABLE stores AUTO_INCREMENT = 1;

-- =============================================================================
-- 1. 매장 5개 (금오공과대학교 축제 부스)
--    실제 축제 부스명 및 위치 좌표 사용 (현실감 부여)
-- =============================================================================
INSERT INTO stores (id, owner_id, is_open, name, latitude, longitude, university,
                    table_time, table_cost, created_at, updated_at, description, head_image_url)
VALUES
(1, 1, 1, '불닭발 포차',   36.1432, 128.3172, '금오공과대학교', 60, 500, NOW(), NOW(), '매콤한 닭발 & 닭갈비 전문', 'https://dummy.com/store1.jpg'),
(2, 2, 1, '소떡소떡 부스', 36.1433, 128.3175, '금오공과대학교', 60, 500, NOW(), NOW(), '소시지·떡볶이·핫도그',     'https://dummy.com/store2.jpg'),
(3, 3, 1, '오삼불고기',    36.1434, 128.3178, '금오공과대학교', 60, 500, NOW(), NOW(), '오징어·삼겹살 불고기 콤보',  'https://dummy.com/store3.jpg'),
(4, 4, 1, '바삭 닭강정',   36.1435, 128.3181, '금오공과대학교', 60, 500, NOW(), NOW(), '순살 & 뼈닭강정',           'https://dummy.com/store4.jpg'),
(5, 5, 1, '순대볶음 마차', 36.1436, 128.3184, '금오공과대학교', 60, 500, NOW(), NOW(), '순대·야채·당면 볶음',        'https://dummy.com/store5.jpg');

-- =============================================================================
-- 2. 영업 5개 (각 매장 1개 활성 영업, 3일 전 시작 - 축제 중간 시점)
-- =============================================================================
INSERT INTO sales (id, open_date_time, store_id)
VALUES
(1, DATE_SUB(NOW(), INTERVAL 30 HOUR), 1),
(2, DATE_SUB(NOW(), INTERVAL 30 HOUR), 2),
(3, DATE_SUB(NOW(), INTERVAL 30 HOUR), 3),
(4, DATE_SUB(NOW(), INTERVAL 30 HOUR), 4),
(5, DATE_SUB(NOW(), INTERVAL 30 HOUR), 5);

-- =============================================================================
-- 3. 메뉴 카테고리 (매장당 4개) + 메뉴 (카테고리당 5개 = 매장당 20개)
--    근거: 실제 축제 부스는 준비/운영 부담으로 카테고리 3-5개, 메뉴 15-25개 운영
-- =============================================================================
DROP PROCEDURE IF EXISTS InsertMenuDataV3;
DELIMITER //
CREATE PROCEDURE InsertMenuDataV3()
BEGIN
  DECLARE s INT DEFAULT 1;
  DECLARE c INT;
  DECLARE m INT;
  DECLARE cat_id INT;
  DECLARE menu_id INT;

  WHILE s <= 5 DO
    SET c = 1;
    WHILE c <= 4 DO
      SET cat_id = (s - 1) * 4 + c;
      INSERT INTO menu_categories (id, store_id, name, menu_category_order, created_at, updated_at)
      VALUES (cat_id, s,
              CASE c
                WHEN 1 THEN '시그니처'
                WHEN 2 THEN '사이드'
                WHEN 3 THEN '세트 메뉴'
                ELSE '음료·디저트'
              END,
              c, NOW(), NOW());

      SET m = 1;
      WHILE m <= 5 DO
        SET menu_id = (s - 1) * 20 + (c - 1) * 5 + m;
        INSERT INTO menus (id, store_id, name, price, description,
                           is_sold_out, is_recommended, menu_order,
                           menu_category_id, created_at, updated_at)
        VALUES (
          menu_id, s,
          CONCAT(
            CASE s
              WHEN 1 THEN CASE c WHEN 1 THEN CASE m WHEN 1 THEN '매운닭발(小)' WHEN 2 THEN '매운닭발(大)' WHEN 3 THEN '닭갈비' WHEN 4 THEN '불닭볶음면' ELSE '닭발볶음밥' END
                               WHEN 2 THEN CASE m WHEN 1 THEN '공깃밥' WHEN 2 THEN '계란말이' WHEN 3 THEN '콘치즈' WHEN 4 THEN '어묵탕' ELSE '순대' END
                               WHEN 3 THEN CASE m WHEN 1 THEN '닭발+닭갈비 세트' WHEN 2 THEN '2인 세트' WHEN 3 THEN '3인 세트' WHEN 4 THEN '4인 패밀리' ELSE '단체 세트' END
                               ELSE CASE m WHEN 1 THEN '막걸리' WHEN 2 THEN '소주' WHEN 3 THEN '콜라' WHEN 4 THEN '사이다' ELSE '물' END END
              WHEN 2 THEN CASE c WHEN 1 THEN CASE m WHEN 1 THEN '떡볶이(小)' WHEN 2 THEN '떡볶이(大)' WHEN 3 THEN '소시지구이' WHEN 4 THEN '핫도그' ELSE '순댓국' END
                               WHEN 2 THEN CASE m WHEN 1 THEN '튀김모둠' WHEN 2 THEN '치즈볼' WHEN 3 THEN '오뎅꼬치' WHEN 4 THEN '계란빵' ELSE '호떡' END
                               WHEN 3 THEN CASE m WHEN 1 THEN '떡볶이+소시지 세트' WHEN 2 THEN '2인 분식세트' WHEN 3 THEN '야식 세트' WHEN 4 THEN '커플 세트' ELSE '단체 세트' END
                               ELSE CASE m WHEN 1 THEN '달고나라떼' WHEN 2 THEN '레모네이드' WHEN 3 THEN '아이스티' WHEN 4 THEN '콜라' ELSE '생수' END END
              WHEN 3 THEN CASE c WHEN 1 THEN CASE m WHEN 1 THEN '오삼불고기(小)' WHEN 2 THEN '오삼불고기(大)' WHEN 3 THEN '삼겹살' WHEN 4 THEN '오징어구이' ELSE '낙지볶음' END
                               WHEN 2 THEN CASE m WHEN 1 THEN '공깃밥' WHEN 2 THEN '쌈채소' WHEN 3 THEN '된장찌개' WHEN 4 THEN '미역국' ELSE '계란후라이' END
                               WHEN 3 THEN CASE m WHEN 1 THEN '오삼+삼겹 세트' WHEN 2 THEN '2인 고기세트' WHEN 3 THEN '3인 BBQ세트' WHEN 4 THEN '4인 파티세트' ELSE '단체 패키지' END
                               ELSE CASE m WHEN 1 THEN '소주' WHEN 2 THEN '맥주' WHEN 3 THEN '막걸리' WHEN 4 THEN '사이다' ELSE '생수' END END
              WHEN 4 THEN CASE c WHEN 1 THEN CASE m WHEN 1 THEN '순살강정(小)' WHEN 2 THEN '순살강정(大)' WHEN 3 THEN '뼈닭강정' WHEN 4 THEN '간장강정' ELSE '양념강정' END
                               WHEN 2 THEN CASE m WHEN 1 THEN '감자튀김' WHEN 2 THEN '코울슬로' WHEN 3 THEN '옥수수' WHEN 4 THEN '치즈스틱' ELSE '어니언링' END
                               WHEN 3 THEN CASE m WHEN 1 THEN '강정+감자 세트' WHEN 2 THEN '2인 치킨세트' WHEN 3 THEN '3인 파티세트' WHEN 4 THEN '패밀리 박스' ELSE '단체 박스' END
                               ELSE CASE m WHEN 1 THEN '콜라' WHEN 2 THEN '사이다' WHEN 3 THEN '제로콜라' WHEN 4 THEN '오렌지주스' ELSE '생수' END END
              ELSE            CASE c WHEN 1 THEN CASE m WHEN 1 THEN '순대볶음(小)' WHEN 2 THEN '순대볶음(大)' WHEN 3 THEN '당면볶음' WHEN 4 THEN '모둠순대' ELSE '순대국밥' END
                               WHEN 2 THEN CASE m WHEN 1 THEN '마늘소스' WHEN 2 THEN '새우젓' WHEN 3 THEN '깍두기' WHEN 4 THEN '떡볶이소스' ELSE '공깃밥' END
                               WHEN 3 THEN CASE m WHEN 1 THEN '순대+당면 세트' WHEN 2 THEN '2인 포장세트' WHEN 3 THEN '3인 모둠세트' WHEN 4 THEN '야식 세트' ELSE '단체 박스' END
                               ELSE CASE m WHEN 1 THEN '막걸리' WHEN 2 THEN '소주' WHEN 3 THEN '복분자' WHEN 4 THEN '콜라' ELSE '생수' END END
            END
          ),
          -- 가격: 기본 4000원 + 카테고리×1000 + 메뉴번호×500
          4000 + (c * 1000) + (m * 500),
          CONCAT('Store', s, ' Category', c, ' Menu', m),
          0,
          IF(m = 1, 1, 0),  -- 카테고리 첫 번째 메뉴만 추천
          m,
          cat_id,
          NOW(), NOW()
        );
        SET m = m + 1;
      END WHILE;
      SET c = c + 1;
    END WHILE;
    SET s = s + 1;
  END WHILE;
END //
DELIMITER ;
CALL InsertMenuDataV3();
DROP PROCEDURE InsertMenuDataV3;

-- =============================================================================
-- 4. 테이블(20개/매장) + 영수증(30개/테이블) + 주문(10개/영수증) + 주문메뉴(2-3개/주문)
--
-- [수치 근거]
--   테이블 20개: 4인석 기준, 80명 동시 수용 (현실적인 부스 크기)
--   영수증 30개: 11h/일 × 3일 / 1.1h 체류 = 30회전 (평균 33분 체류)
--   주문 10개: 초기 3-4건 + 중간 추가 2-3건 × 2 = 9-10건
--   주문메뉴 2-3개: 그룹당 메뉴 다양성 반영
--
-- [데이터 총계]
--   5 × 20 × 30 × 10 = 30,000건 주문
--   5 × 20 × 30 × 10 × 2.5(평균) ≒ 75,000건 주문메뉴
--
-- [상태 분포 (seq % 10)]
--   seq%10 = 0       → ORDERED (10%):   3,000건 - 현재 접수 대기 중인 신규 주문
--   seq%10 = 1       → RECEIVED (10%):  3,000건 - 주방에서 조리 중인 주문
--   seq%10 = 2~8 (7) → COMPLETED (70%): 21,000건 - 이미 서빙 완료된 과거 주문
--   seq%10 = 9       → CANCELED (10%):  3,000건 - 취소된 주문
--
--   → 캐시 저장 대상 (ORDERED+RECEIVED+COMPLETED): 27,000건 (90%)
--   → per store 캐시: 5,400건 ZSET entries
--
-- UUID 전략 (결정론적 BINARY(16)):
--   테이블 ID:  s*1000 + t (16진수 패딩)
--   영수증 ID:  s*10000000 + t*100000 + r (16진수 패딩)
-- =============================================================================
DROP PROCEDURE IF EXISTS InsertOrderDataV3;
DELIMITER //
CREATE PROCEDURE InsertOrderDataV3()
BEGIN
  DECLARE s       INT DEFAULT 1;
  DECLARE t       INT;
  DECLARE r       INT;
  DECLARE o       INT;
  DECLARE seq     INT DEFAULT 0;
  DECLARE tid     BINARY(16);
  DECLARE rid     BINARY(16);
  DECLARE oid     BIGINT;
  DECLARE ostatus  VARCHAR(20);
  DECLARE omstatus VARCHAR(20);
  DECLARE menu1   INT;
  DECLARE menu2   INT;
  DECLARE menu3   INT;
  DECLARE qty1    INT;
  DECLARE qty2    INT;
  DECLARE qty3    INT;
  -- 주문 시간: 영수증(r) 시간 기준으로 30시간 범위에 분산
  DECLARE receipt_minutes_ago INT;
  DECLARE order_minutes_offset INT;

  WHILE s <= 5 DO
    SET t = 1;
    WHILE t <= 20 DO

      -- 테이블 INSERT
      SET tid = UNHEX(LPAD(HEX(s * 1000 + t), 32, '0'));
      INSERT INTO tables (id, table_number, table_x, table_y,
                          is_active, capacity, store_id, created_at, updated_at)
      VALUES (tid, t,
              ((t - 1) MOD 5)  * 80,
              ((t - 1) DIV 5) * 80,
              1, 4, s, NOW(), NOW());

      SET r = 1;
      WHILE r <= 30 DO

        -- 영수증 시간: 30시간 범위에 r에 따라 분산 (최근 주문이 r=1, 가장 오래된 r=30)
        -- r=1: 1시간 전, r=30: 30시간 전 (3일 축제 커버)
        SET receipt_minutes_ago = r * 60;
        SET rid = UNHEX(LPAD(HEX(s * 10000000 + t * 100000 + r), 32, '0'));
        INSERT INTO receipts (id, sale_id, table_id, is_adjustment, created_at, updated_at)
        VALUES (rid, s, tid, 0,
                DATE_SUB(NOW(), INTERVAL receipt_minutes_ago MINUTE),
                DATE_SUB(NOW(), INTERVAL receipt_minutes_ago MINUTE));

        SET o = 1;
        WHILE o <= 10 DO

          -- 상태 결정 (seq % 10)
          SET seq = seq + 1;
          IF (seq % 10) = 0 THEN
            SET ostatus = 'ORDERED';
          ELSEIF (seq % 10) = 1 THEN
            SET ostatus = 'RECEIVED';
          ELSEIF (seq % 10) < 9 THEN
            SET ostatus = 'COMPLETED';
          ELSE
            SET ostatus = 'CANCELED';
          END IF;

          SET omstatus = CASE ostatus
            WHEN 'ORDERED'   THEN 'ORDERED'
            WHEN 'RECEIVED'  THEN 'COOKING'
            WHEN 'COMPLETED' THEN 'COMPLETED'
            ELSE 'CANCELED'
          END;

          -- 주문 시간: 영수증 시간 + o번째 주문 offset (최근 주문이 r=1,o=10)
          SET order_minutes_offset = receipt_minutes_ago - (10 - o) * 5;

          -- 주문 INSERT
          INSERT INTO orders (status, table_price, description, receipt_id, created_at, updated_at)
          VALUES (
            ostatus,
            5000 + ((seq % 6) * 1000),  -- 5000~10000원 가격 순환
            CONCAT('S', s, '-T', t, '-R', r, '-O', o),
            rid,
            DATE_SUB(NOW(), INTERVAL order_minutes_offset MINUTE),
            DATE_SUB(NOW(), INTERVAL order_minutes_offset MINUTE)
          );
          SET oid = LAST_INSERT_ID();

          -- 주문메뉴 2-3개 (해당 매장 20개 메뉴에서 순환)
          SET menu1 = (s - 1) * 20 + (seq         % 20) + 1;
          SET menu2 = (s - 1) * 20 + ((seq + 10)  % 20) + 1;
          SET qty1  = 1 + (o % 3);   -- 1~3개
          SET qty2  = 1;

          INSERT INTO order_menus (quntity, menu_id, order_id, status,
                                   completed_count, created_at, updated_at)
          VALUES (qty1, menu1, oid, omstatus,
                  IF(ostatus = 'COMPLETED', qty1, 0), NOW(), NOW());

          INSERT INTO order_menus (quntity, menu_id, order_id, status,
                                   completed_count, created_at, updated_at)
          VALUES (qty2, menu2, oid, omstatus,
                  IF(ostatus = 'COMPLETED', 1, 0), NOW(), NOW());

          -- 3번째 메뉴 (5번 주문마다 추가 - 약 33% 확률로 3개 메뉴 주문)
          IF (o % 3) = 0 THEN
            SET menu3 = (s - 1) * 20 + ((seq + 5) % 20) + 1;
            SET qty3  = 1;
            INSERT INTO order_menus (quntity, menu_id, order_id, status,
                                     completed_count, created_at, updated_at)
            VALUES (qty3, menu3, oid, omstatus,
                    IF(ostatus = 'COMPLETED', 1, 0), NOW(), NOW());
          END IF;

          SET o = o + 1;
        END WHILE; -- orders
        SET r = r + 1;
      END WHILE; -- receipts
      SET t = t + 1;
    END WHILE; -- tables
    SET s = s + 1;
  END WHILE; -- stores
END //
DELIMITER ;
CALL InsertOrderDataV3();
DROP PROCEDURE InsertOrderDataV3;

-- =============================================================================
-- 검증 쿼리: 데이터 적재 현황 및 상태 분포 확인
-- =============================================================================
SELECT
  s.id                                AS sale_id,
  st.name                             AS store_name,
  COUNT(DISTINCT tbl.id)              AS table_cnt,
  COUNT(DISTINCT r.id)                AS receipt_cnt,
  COUNT(DISTINCT o.id)                AS total_order_cnt,
  SUM(o.status = 'ORDERED')           AS ordered_cnt,
  SUM(o.status = 'RECEIVED')          AS received_cnt,
  SUM(o.status = 'COMPLETED')         AS completed_cnt,
  SUM(o.status = 'CANCELED')          AS canceled_cnt,
  ROUND(SUM(o.status IN ('ORDERED','RECEIVED','COMPLETED'))
        / COUNT(o.id) * 100, 1)       AS cache_target_pct
FROM sales s
JOIN stores      st  ON st.id     = s.store_id
JOIN receipts    r   ON r.sale_id = s.id
JOIN tables      tbl ON tbl.id    = r.table_id
LEFT JOIN orders o   ON o.receipt_id = r.id
GROUP BY s.id, st.name
ORDER BY s.id;

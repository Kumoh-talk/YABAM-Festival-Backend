-- =============================================================================
-- YABAM 대학 축제 POS 현실적 더미 데이터 (v2)
--
-- 설계 근거:
--   5개 부스(store) × 1개 영업(sale) × 20개 테이블 × 10개 영수증 × 10개 주문
--   = 10,000건 주문 (실제 3일 대학 축제 규모 시뮬레이션)
--
-- 상태 분포 (order_seq % 10 기준):
--   ORDERED   30% (0,1,2) - 접수 대기 중
--   RECEIVED  10% (3)     - 주방 접수 완료, 조리 중
--   COMPLETED 50% (4~8)   - 서빙 완료 (과거 누적 주문)
--   CANCELED  10% (9)     - 취소된 주문
--
-- 메뉴: 카테고리 3개 × 메뉴 4개 = 12개/매장 (실제 축제 메뉴판 기준)
-- 주문메뉴: 주문당 2개 (방문 그룹당 2-3가지 메뉴 주문 반영)
-- =============================================================================

USE local_mydb;

-- 기존 데이터 전체 삭제 (외래키 순서 준수)
DELETE FROM order_menus;
DELETE FROM orders;
DELETE FROM payments;
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
-- =============================================================================
INSERT INTO stores (id, owner_id, is_open, name, location, university,
                    table_time, table_cost, created_at, updated_at, description, head_image_url)
VALUES
(1, 1, 1, '불닭발 포차',   '37.5411, 128.3972', '금오공과대학교', 60, 500, NOW(), NOW(), '매콤한 닭발 & 닭갈비 전문', NULL),
(2, 2, 1, '소떡소떡 부스', '37.5412, 128.3975', '금오공과대학교', 60, 500, NOW(), NOW(), '소시지·떡볶이·핫도그',     NULL),
(3, 3, 1, '오삼불고기',    '37.5413, 128.3978', '금오공과대학교', 60, 500, NOW(), NOW(), '오징어·삼겹살 불고기 콤보',  NULL),
(4, 4, 1, '바삭 닭강정',   '37.5414, 128.3981', '금오공과대학교', 60, 500, NOW(), NOW(), '순살 & 뼈닭강정',           NULL),
(5, 5, 1, '순대볶음 마차', '37.5415, 128.3984', '금오공과대학교', 60, 500, NOW(), NOW(), '순대·야채·당면 볶음',        NULL);

-- =============================================================================
-- 2. 영업 5개 (각 매장 1개 활성 영업, 6시간 전 시작)
-- =============================================================================
INSERT INTO sales (id, open_date_time, store_id)
VALUES
(1, DATE_SUB(NOW(), INTERVAL 6 HOUR), 1),
(2, DATE_SUB(NOW(), INTERVAL 6 HOUR), 2),
(3, DATE_SUB(NOW(), INTERVAL 6 HOUR), 3),
(4, DATE_SUB(NOW(), INTERVAL 6 HOUR), 4),
(5, DATE_SUB(NOW(), INTERVAL 6 HOUR), 5);

-- =============================================================================
-- 3. 메뉴 카테고리 (매장당 3개) + 메뉴 (카테고리당 4개 = 매장당 12개)
-- =============================================================================
DROP PROCEDURE IF EXISTS InsertMenuData;
DELIMITER //
CREATE PROCEDURE InsertMenuData()
BEGIN
  DECLARE s INT DEFAULT 1;
  DECLARE c INT;
  DECLARE m INT;
  DECLARE cat_id INT;
  DECLARE menu_id INT;

  WHILE s <= 5 DO
    SET c = 1;
    WHILE c <= 3 DO
      SET cat_id = (s - 1) * 3 + c;
      INSERT INTO menu_categories (id, store_id, name, menu_category_order, created_at, updated_at)
      VALUES (cat_id, s,
              CASE c WHEN 1 THEN '메인 메뉴' WHEN 2 THEN '사이드 메뉴' ELSE '음료' END,
              c, NOW(), NOW());

      SET m = 1;
      WHILE m <= 4 DO
        SET menu_id = (s - 1) * 12 + (c - 1) * 4 + m;
        INSERT INTO menus (id, store_id, name, price, description,
                           is_sold_out, is_recommended, menu_order,
                           menu_category_id, created_at, updated_at)
        VALUES (
          menu_id, s,
          CONCAT(
            CASE s
              WHEN 1 THEN CASE m WHEN 1 THEN '매운닭발(小)' WHEN 2 THEN '매운닭발(大)' WHEN 3 THEN '닭갈비' ELSE '닭발볶음밥' END
              WHEN 2 THEN CASE m WHEN 1 THEN '떡볶이'       WHEN 2 THEN '소시지'       WHEN 3 THEN '핫도그'   ELSE '순댓국'   END
              WHEN 3 THEN CASE m WHEN 1 THEN '오삼불고기'   WHEN 2 THEN '삼겹살'       WHEN 3 THEN '오징어구이' ELSE '공기밥' END
              WHEN 4 THEN CASE m WHEN 1 THEN '순살강정(小)' WHEN 2 THEN '순살강정(大)' WHEN 3 THEN '뼈닭강정'  ELSE '강정세트' END
              ELSE CASE m        WHEN 1 THEN '순대볶음(小)' WHEN 2 THEN '순대볶음(大)' WHEN 3 THEN '당면볶음'  ELSE '모둠순대' END
            END
          ),
          3000 + (c * 1000) + (m * 500),
          CONCAT('Store', s, ' Cat', c, ' Menu', m),
          0,
          IF(m = 1, 1, 0),
          m, cat_id, NOW(), NOW()
        );
        SET m = m + 1;
      END WHILE;
      SET c = c + 1;
    END WHILE;
    SET s = s + 1;
  END WHILE;
END //
DELIMITER ;
CALL InsertMenuData();
DROP PROCEDURE InsertMenuData;

-- =============================================================================
-- 4. 테이블(매장당 20개) + 영수증(테이블당 10개) + 주문(영수증당 10개) + 주문메뉴(주문당 2개)
--
-- UUID 전략 (결정론적 BINARY(16)):
--   테이블 ID:  LPAD(HEX(storeId * 1000 + tableNum), 32, '0')
--   영수증 ID:  LPAD(HEX(storeId * 1000000 + tableNum * 10000 + receiptNum), 32, '0')
-- =============================================================================
DROP PROCEDURE IF EXISTS InsertOrderData;
DELIMITER //
CREATE PROCEDURE InsertOrderData()
BEGIN
  DECLARE s       INT DEFAULT 1;
  DECLARE t       INT;
  DECLARE r       INT;
  DECLARE o       INT;
  DECLARE seq     INT DEFAULT 0;      -- 전역 순번 (상태 분포에 사용)
  DECLARE tid     BINARY(16);
  DECLARE rid     BINARY(16);
  DECLARE oid     BIGINT;
  DECLARE ostatus VARCHAR(20);
  DECLARE omstatus VARCHAR(20);
  DECLARE menu1   INT;
  DECLARE menu2   INT;
  DECLARE qty1    INT;
  DECLARE qty2    INT;

  WHILE s <= 5 DO
    SET t = 1;
    WHILE t <= 20 DO

      -- 테이블 INSERT
      SET tid = UNHEX(LPAD(HEX(s * 1000 + t), 32, '0'));
      INSERT INTO tables (id, table_number, table_x, table_y,
                          is_active, capacity, store_id, created_at, updated_at)
      VALUES (tid, t,
              ((t - 1) MOD 5)  * 60,
              ((t - 1) DIV 5) * 60,
              1, 4, s, NOW(), NOW());

      SET r = 1;
      WHILE r <= 10 DO

        -- 영수증 INSERT (시간은 r 시간 전으로 분산)
        SET rid = UNHEX(LPAD(HEX(s * 1000000 + t * 10000 + r), 32, '0'));
        INSERT INTO receipts (id, sale_id, table_id, is_adjustment, created_at, updated_at)
        VALUES (rid, s, tid, 0,
                DATE_SUB(NOW(), INTERVAL r HOUR),
                DATE_SUB(NOW(), INTERVAL r HOUR));

        SET o = 1;
        WHILE o <= 10 DO

          -- 상태 결정 (seq % 10)
          -- 0,1,2 → ORDERED(30%), 3 → RECEIVED(10%), 4~8 → COMPLETED(50%), 9 → CANCELED(10%)
          SET seq = seq + 1;
          IF (seq % 10) < 3 THEN
            SET ostatus = 'ORDERED';
          ELSEIF (seq % 10) = 3 THEN
            SET ostatus = 'RECEIVED';
          ELSEIF (seq % 10) < 9 THEN
            SET ostatus = 'COMPLETED';
          ELSE
            SET ostatus = 'CANCELED';
          END IF;

          -- 주문메뉴 상태 매핑
          SET omstatus = CASE ostatus
            WHEN 'ORDERED'   THEN 'ORDERED'
            WHEN 'RECEIVED'  THEN 'COOKING'
            WHEN 'COMPLETED' THEN 'COMPLETED'
            ELSE 'CANCELED'
          END;

          -- 주문 INSERT
          INSERT INTO orders (status, table_price, description, receipt_id, created_at, updated_at)
          VALUES (
            ostatus,
            5000 + (o * 1000),
            CONCAT('S', s, '-T', t, '-R', r, '-O', o),
            rid,
            DATE_SUB(NOW(), INTERVAL (r * 60 + (10 - o)) MINUTE),
            DATE_SUB(NOW(), INTERVAL (r * 60 + (10 - o)) MINUTE)
          );
          SET oid = LAST_INSERT_ID();

          -- 주문메뉴 2개 (해당 매장 12개 메뉴에서 순환 선택)
          SET menu1 = (s - 1) * 12 + (seq       % 12) + 1;
          SET menu2 = (s - 1) * 12 + ((seq + 6) % 12) + 1;
          SET qty1  = 1 + (o % 3);   -- 1~3개
          SET qty2  = 1;

          INSERT INTO order_menus (quntity, menu_id, order_id, status,
                                   completed_count, created_at, updated_at)
          VALUES (qty1, menu1, oid, omstatus,
                  IF(ostatus = 'COMPLETED', 1, 0), NOW(), NOW());

          INSERT INTO order_menus (quntity, menu_id, order_id, status,
                                   completed_count, created_at, updated_at)
          VALUES (qty2, menu2, oid, omstatus,
                  IF(ostatus = 'COMPLETED', 1, 0), NOW(), NOW());

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
CALL InsertOrderData();
DROP PROCEDURE InsertOrderData;

-- =============================================================================
-- 검증 쿼리
-- =============================================================================
SELECT
  s.id   AS sale_id,
  st.name AS store_name,
  COUNT(DISTINCT tbl.id)  AS table_cnt,
  COUNT(DISTINCT r.id)    AS receipt_cnt,
  COUNT(DISTINCT o.id)    AS order_cnt,
  SUM(o.status = 'ORDERED')   AS ordered_cnt,
  SUM(o.status = 'RECEIVED')  AS received_cnt,
  SUM(o.status = 'COMPLETED') AS completed_cnt,
  SUM(o.status = 'CANCELED')  AS canceled_cnt
FROM sales s
JOIN stores      st  ON st.id  = s.store_id
JOIN receipts    r   ON r.sale_id = s.id
JOIN tables      tbl ON tbl.id = r.table_id
LEFT JOIN orders o   ON o.receipt_id = r.id
GROUP BY s.id, st.name
ORDER BY s.id;

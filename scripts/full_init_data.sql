USE local_mydb;

-- Clear
DELETE FROM order_menus;
DELETE FROM orders;
DELETE FROM payments;
DELETE FROM receipts;
DELETE FROM tables;
DELETE FROM menus;
DELETE FROM menu_categories;
DELETE FROM sales;
DELETE FROM stores;

-- 1. Store
INSERT INTO stores (id, owner_id, is_open, name, location, university, table_time, table_cost, created_at, updated_at, description, head_image_url)
VALUES (1, 1, 1, 'Test Store', '37.123, 127.123', 'Kumoh Univ', 60, 1000, NOW(), NOW(), 'Premium Store', 'http://img.com/1.jpg');

-- 2. Sale
INSERT INTO sales (id, open_date_time, store_id)
VALUES (1, NOW(), 1);

-- 3. Table
SET @tid = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));
INSERT INTO tables (id, table_number, table_x, table_y, is_active, capacity, store_id, created_at, updated_at)
VALUES (@tid, 1, 100, 100, 1, 4, 1, NOW(), NOW());

-- 4. Menu Category
INSERT INTO menu_categories (id, store_id, name, menu_category_order, created_at, updated_at)
VALUES (1, 1, 'Main', 1, NOW(), NOW());

-- 5. Menu
INSERT INTO menus (id, store_id, name, price, description, is_sold_out, is_recommended, menu_order, menu_category_id, created_at, updated_at)
VALUES (1, 1, 'Steak', 25000, 'Delicious', 0, 1, 1, 1, NOW(), NOW());

-- 6. Receipt
SET @rid = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));
INSERT INTO receipts (id, sale_id, table_id, is_adjustment, created_at, updated_at)
VALUES (@rid, 1, @tid, 0, NOW(), NOW());

-- 7. Dummy Orders and OrderMenus
DROP PROCEDURE IF EXISTS InsertDummyOrders;

DELIMITER //
CREATE PROCEDURE InsertDummyOrders()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE rid BINARY(16);
  SET rid = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));
  
  WHILE i <= 10000 DO
    INSERT INTO orders (id, status, table_price, description, receipt_id, created_at, updated_at)
    VALUES (i, 
      CASE WHEN i % 3 = 0 THEN 'COMPLETED'
           WHEN i % 3 = 1 THEN 'ORDERED'
           ELSE 'RECEIVED' END,
      10000,
      CONCAT('Bulk Order ', i),
      rid,
      NOW(),
      NOW()
    );
    
    -- Add 1 menu per order
    INSERT INTO order_menus (quntity, menu_id, order_id, status, completed_count, created_at, updated_at)
    VALUES (2, 1, i, 'ORDERED', 0, NOW(), NOW());
    
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;

CALL InsertDummyOrders();
DROP PROCEDURE InsertDummyOrders;

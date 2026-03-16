USE local_mydb;

DROP PROCEDURE IF EXISTS InsertDummyOrders;

DELIMITER //
CREATE PROCEDURE InsertDummyOrders()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE rid BINARY(16);
  SET rid = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));
  
  WHILE i <= 1000 DO
    INSERT INTO orders (status, table_price, description, receipt_id, created_at, updated_at)
    VALUES (
      CASE WHEN i % 3 = 0 THEN 'COMPLETED'
           WHEN i % 3 = 1 THEN 'ORDERED'
           ELSE 'RECEIVED' END,
      10000 + (i * 10),
      CONCAT('Bulk Order ', i),
      rid,
      NOW(),
      NOW()
    );
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;

CALL InsertDummyOrders();
DROP PROCEDURE InsertDummyOrders;

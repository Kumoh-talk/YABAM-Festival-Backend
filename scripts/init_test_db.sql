-- 1. Store creation
INSERT IGNORE INTO stores (id, owner_id, is_open, name, latitude, longitude, university, table_time, table_cost, created_at, updated_at)
VALUES (1, 1, 1, 'Test Store', 37.123, 127.123, 'Kumoh Univ', 60, 1000, NOW(), NOW());

-- 2. Sale creation (id=1)
INSERT IGNORE INTO sales (id, open_date_time, store_id)
VALUES (1, NOW(), 1);

-- 3. Table creation
INSERT IGNORE INTO tables (id, table_number, table_x, table_y, is_active, capacity, store_id, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 1, 100, 100, 1, 4, 1, NOW(), NOW());
-- Note: Check if the UUID format is literally string or binary(16) in MySQL.
-- Hibernate UUID usually uses binary(16) or varchar(36).
-- Given @GeneratedUuidV7, it might be binary.

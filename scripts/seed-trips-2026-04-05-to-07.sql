-- =============================================================================
-- Thêm ~10 chuyến xe: ngày 2026-04-05, 2026-04-06, 2026-04-07
-- Chạy trên database bus_ticket (hoặc tên DB bạn đang dùng).
--
-- Cách chạy (MySQL CLI / Workbench):
--   mysql -u root -p bus_ticket < scripts/seed-trips-2026-04-05-to-07.sql
--
-- Script tự chọn bus_id / route_id luân phiên theo danh sách hiện có (ROW_NUMBER).
-- Mỗi chuyến sau khi INSERT sẽ sinh ghế A01..A{n} theo total_seats của xe (như backend).
-- =============================================================================

USE bus_ticket;

-- Bảng số 1..60 (đủ cho mọi xe thường gặp; MySQL 8+)
DROP TEMPORARY TABLE IF EXISTS tmp_nums;
CREATE TEMPORARY TABLE tmp_nums (n INT PRIMARY KEY);
INSERT INTO tmp_nums (n)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 60
)
SELECT n FROM seq;

-- ---------------------------------------------------------------------------
-- Chuyến 1 — 2026-04-05
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-05 06:30:00', 280000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((1 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((1 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 2
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-05 10:15:00', 295000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((2 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((2 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 3
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-05 14:00:00', 310000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((3 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((3 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 4
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-05 20:30:00', 325000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((4 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((4 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 5 — 2026-04-06
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-06 05:45:00', 265000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((5 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((5 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 6
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-06 11:20:00', 305000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((6 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((6 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 7
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-06 16:45:00', 288000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((7 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((7 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 8
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-06 22:00:00', 338000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((8 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((8 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 9 — 2026-04-07
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-07 07:00:00', 290000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((9 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((9 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

-- ---------------------------------------------------------------------------
-- Chuyến 10
-- ---------------------------------------------------------------------------
INSERT INTO trips (bus_id, route_id, departure_time, price)
SELECT b.id, r.id, '2026-04-07 18:30:00', 315000.00
FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM buses) b
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS k FROM routes) r
  ON b.k = ((10 - 1) % (SELECT COUNT(*) FROM buses)) + 1
 AND r.k = ((10 - 1) % (SELECT COUNT(*) FROM routes)) + 1
LIMIT 1;

SET @trip_id = LAST_INSERT_ID();
INSERT INTO seats (trip_id, seat_number, booked)
SELECT @trip_id, CONCAT('A', LPAD(nums.n, 2, '0')), 0
FROM trips t
JOIN buses bus ON bus.id = t.bus_id
JOIN tmp_nums nums ON nums.n >= 1 AND nums.n <= bus.total_seats
WHERE t.id = @trip_id;

DROP TEMPORARY TABLE IF EXISTS tmp_nums;

-- Kiểm tra nhanh
-- SELECT id, bus_id, route_id, departure_time, price FROM trips WHERE departure_time >= '2026-04-05' AND departure_time < '2026-04-08' ORDER BY departure_time;

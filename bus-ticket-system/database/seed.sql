USE bus_ticket;

-- Users are seeded by the Spring Boot `DataInitializer` so passwords are stored with BCrypt.
-- Default accounts after first backend startup:
-- admin / admin123
-- customer@busbooking.com / 123456

INSERT INTO buses (bus_number, bus_type, total_seats)
VALUES
    ('FUTA-01', 'Limousine', 20),
    ('VEX-02', 'Sleeper', 24);

INSERT INTO routes (origin, destination, distance)
VALUES
    ('TP. HCM', 'Da Lat', 310),
    ('TP. HCM', 'Nha Trang', 430),
    ('Da Nang', 'Hue', 95);

INSERT INTO trips (bus_id, route_id, departure_time, price)
VALUES
    (1, 1, '2026-03-13 08:00:00', 300000),
    (2, 2, '2026-03-13 21:30:00', 350000),
    (1, 3, '2026-03-14 09:15:00', 180000),
    -- Trips in the next 3 days from 2026-03-16
    (1, 1, '2026-03-17 08:00:00', 300000),  -- TP. HCM -> Da Lat
    (2, 2, '2026-03-17 21:30:00', 350000),  -- TP. HCM -> Nha Trang
    (1, 1, '2026-03-18 08:00:00', 300000),  -- TP. HCM -> Da Lat
    (2, 2, '2026-03-18 21:30:00', 350000),  -- TP. HCM -> Nha Trang
    (1, 3, '2026-03-19 09:15:00', 180000);  -- Da Nang -> Hue


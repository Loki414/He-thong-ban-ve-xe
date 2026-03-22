CREATE DATABASE IF NOT EXISTS bus_ticket;
USE bus_ticket;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS buses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bus_number VARCHAR(50) NOT NULL UNIQUE,
    bus_type VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL
);

CREATE TABLE IF NOT EXISTS routes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    distance DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS trips (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bus_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    departure_time DATETIME NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_trip_bus FOREIGN KEY (bus_id) REFERENCES buses(id),
    CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES routes(id)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    trip_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    booked BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_seat_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    booking_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_time DATETIME NULL,
    CONSTRAINT fk_payment_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id)
);

CREATE TABLE IF NOT EXISTS seat_locks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seat_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    locked_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    CONSTRAINT fk_seat_lock_seat FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT fk_seat_lock_user FOREIGN KEY (user_id) REFERENCES users(id)
);


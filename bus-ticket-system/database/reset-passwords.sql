-- Gán lại mật khẩu (BCrypt, giống Spring Security) — chạy trong schema bus_ticket
-- admin     → mật khẩu: admin123
-- customer  → mật khẩu: 123456
--
-- Nếu username khác, sửa WHERE hoặc đổi hash (chạy BcryptHashTool trong backend để tạo hash mới).

USE bus_ticket;

-- Workbench: tắt "safe updates" tạm thời (tránh lỗi 1175 khi UPDATE theo username)
SET SQL_SAFE_UPDATES = 0;

UPDATE users SET password = '$2a$10$zl6qNDF0ZjHYsFPFRE858ulBvxFI1XvqgefIpJ4AMaNJ9f85Qc6dm' WHERE username = 'admin';
UPDATE users SET password = '$2a$10$Ai61nIKzxJm7UKqlcFfQN.8AH2o2ouEaOAomI5vwLREAfrCh0cdOS' WHERE username = 'customer';

SET SQL_SAFE_UPDATES = 1;

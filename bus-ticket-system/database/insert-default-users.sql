-- Tạo user admin + customer nếu chưa có; nếu đã có thì chỉ cập nhật mật khẩu (BCrypt)
-- admin     → admin123  |  customer → 123456
-- Chạy trong Workbench sau USE bus_ticket;

USE bus_ticket;

SET SQL_SAFE_UPDATES = 0;

INSERT INTO users (username, email, password, role) VALUES
  ('admin', 'admin@busbooking.com', '$2a$10$zl6qNDF0ZjHYsFPFRE858ulBvxFI1XvqgefIpJ4AMaNJ9f85Qc6dm', 'ROLE_ADMIN'),
  ('customer', 'customer@busbooking.com', '$2a$10$Ai61nIKzxJm7UKqlcFfQN.8AH2o2ouEaOAomI5vwLREAfrCh0cdOS', 'ROLE_USER')
  AS new
ON DUPLICATE KEY UPDATE
  password = new.password;

SET SQL_SAFE_UPDATES = 1;

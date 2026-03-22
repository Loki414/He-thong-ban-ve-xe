# 🚌 BusBooking – Hệ thống đặt vé xe khách

Fullstack monorepo gồm **Spring Boot backend + MySQL** và **Spring Boot frontend** (`../frontend-web`).

---

## Cấu trúc dự án

```
bus-ticket-frontend/
├── bus-ticket-system/
│   ├── backend/        ← Spring Boot API (port 8080)
│   └── database/
│       ├── schema.sql
│       └── seed.sql
└── frontend-web/       ← Spring Boot + Thymeleaf + static (port 8081)
    └── src/main/resources/
        ├── templates/    ← các trang .html
        └── static/       ← css/, js/
```

---

## ⚙️ Cài đặt và chạy Backend

### Yêu cầu
- Java 17+
- Maven 3.8+
- MySQL 8+

### Bước 1 – Tạo database

```sql
CREATE DATABASE bus_ticket;
```

Hoặc chạy file schema:

```bash
mysql -u root -p < database/schema.sql
```

### Bước 2 – Cấu hình kết nối

Mở file `backend/src/main/resources/application.properties` và sửa:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Bước 3 – Chạy backend

```bash
cd backend
mvn spring-boot:run
```

Backend sẽ chạy tại `http://localhost:8080`

Khi khởi động lần đầu, Spring Boot tự tạo bảng và seed sẵn:
- Admin: `admin` / `admin123`
- User: `customer` / `123456`

---

## 🌐 Mở Frontend (Spring Boot)

1. Chạy backend API (port `8080`) như phần trên.
2. Trong thư mục `frontend-web`: `mvn spring-boot:run` (port `8081`).
3. Mở trình duyệt: **http://localhost:8081/** (xem thêm `README.md` ở thư mục gốc repo).

---

## 🔑 Tài khoản mặc định

| Tài khoản | Mật khẩu  | Vai trò |
|-----------|-----------|---------|
| admin     | admin123  | ADMIN   |
| customer  | 123456    | USER    |

---

## 📡 Các API chính

### Auth
| Method | Endpoint              | Mô tả          |
|--------|-----------------------|----------------|
| POST   | /api/auth/register    | Đăng ký        |
| POST   | /api/auth/login       | Đăng nhập → JWT|

### Public
| Method | Endpoint                          | Mô tả                   |
|--------|-----------------------------------|-------------------------|
| GET    | /api/buses                        | Danh sách xe            |
| GET    | /api/routes                       | Danh sách tuyến         |
| GET    | /api/trips                        | Tất cả chuyến           |
| GET    | /api/trips/search?origin=&destination= | Tìm kiếm chuyến    |
| GET    | /api/seats/trip/{tripId}          | Ghế theo chuyến         |

### Authenticated
| Method | Endpoint          | Mô tả                     |
|--------|-------------------|---------------------------|
| POST   | /api/seats/lock   | Khóa ghế 5 phút           |
| POST   | /api/tickets      | Tạo vé (body: seatId, userId) |
| POST   | /api/payments     | Thanh toán (body: ticketId, amount, method) |

### Admin only
| Method | Endpoint                          | Mô tả                   |
|--------|-----------------------------------|-------------------------|
| GET    | /api/users                        | Tất cả user             |
| GET    | /api/tickets                      | Tất cả vé               |
| GET    | /api/payments                     | Tất cả thanh toán       |
| GET    | /api/admin/revenue/today          | Doanh thu hôm nay       |
| GET    | /api/admin/bookings/last30days    | Đặt vé 30 ngày qua      |
| GET    | /api/admin/trips/statistics       | Thống kê chuyến theo tuyến |

---

## 🎯 Flow sử dụng

```
/ → /trips → /seats → /booking → /payment → /success
```

1. Tìm chuyến tại trang chủ `/`
2. Chọn chuyến tại `/trips`
3. Chọn ghế tại `/seats`
4. Điền thông tin hành khách tại `/booking`
5. Thanh toán tại `/payment`
6. Xác nhận vé tại `/success`

Admin vào `/admin/dashboard` (cần login với role ADMIN).

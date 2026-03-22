# Bus Ticket – Spring Boot (API + Web)

Giao diện chỉ còn trong **`frontend-web`**. Không dùng Live Server mở file HTML tại thư mục gốc nữa.

## Chạy local

1. **Backend API** (port `8080`):

   ```bash
   cd bus-ticket-system/backend
   mvn spring-boot:run
   ```

2. **Frontend web** (port `8081`):

   ```bash
   cd frontend-web
   mvn spring-boot:run
   ```

3. Mở trình duyệt: **http://localhost:8081/**

Cấu hình URL API backend cho frontend: `frontend-web/src/main/resources/application.yml`  
(`backend.api.base-url`, mặc định `http://localhost:8080/api`).

## Cấu trúc

- `bus-ticket-system/backend` – REST API + JWT + MySQL  
- `frontend-web` – Spring MVC + Thymeleaf, `templates/` + `static/` (CSS/JS)

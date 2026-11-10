# Tutorial 11 - Consume RESTful API with ReactJS

**Nguyễn Thị Minh Hạnh - 2301140029**  
SE1 | Frontend React + Axios | Backend Spring Boot + JPA | MySQL

## 1. Cấu trúc và yêu cầu

- `backend/`: Spring Boot **4.1.2**, Maven Wrapper **3.10.0**, mức biên dịch **Java 21**.
- `frontend/`: React, Axios, Vite, Lucide; có `package-lock.json` để cài đúng dependency.
- `database/`: SQL tạo schema/tables và truy vấn kiểm tra; dữ liệu mẫu do `DemoData.java` tạo.
- `evidence/`: ảnh và kết quả kiểm tra thực tế của phiên bản này.
- Cần **JDK 21 trở lên tương thích Spring Boot 4.1.2**, **Node.js 22.12+** hoặc Node 24/26, **MySQL 8.0+**. Máy kiểm tra dùng JDK 27, Node 26.5.1, MySQL 8.0.46.

## 2. Chạy backend (terminal thứ nhất)

Cổng mặc định **8080**, kế thừa bản Tut10 của 2301140029. MySQL mặc định `localhost:3306`, schema riêng **demo_2301140029**. Không dùng chung database của bài cũ.

Trong PowerShell, đặt `JAVA_HOME` tới JDK 21 hoặc JDK tương thích mới hơn; kiểm tra `java -version`. Không sử dụng JDK 17 để build mức Java 21.

```powershell
cd backend
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-local-mysql-password'
.\mvnw.cmd spring-boot:run
```

macOS/Linux: `chmod +x mvnw` rồi `./mvnw spring-boot:run`; đặt các biến môi trường tương đương.

Backend dùng `createDatabaseIfNotExist=true` và Hibernate `ddl-auto=update` cho bài thực hành. Nếu tài khoản MySQL không có quyền tạo database, chạy `database/setup.sql` bằng MySQL Workbench trước, rồi cấp quyền cho tài khoản ứng dụng trên schema này. SQL không xóa bảng hay dữ liệu cũ. Schema cần tạo mới để nhận đầy đủ dữ liệu mẫu.

Nếu MySQL dùng cổng khác:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:13306/demo_2301140029?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
.\mvnw.cmd spring-boot:run
```

## 3. Chạy frontend (terminal thứ hai)

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Mở **http://localhost:3000**. Hai server phải chạy đồng thời. `npm start` dùng Vite và cố định cổng 3000 (`strictPort`), không tự chuyển sang cổng khác làm sai CORS.

Frontend mặc định gọi `http://localhost:8080`. Nếu thay backend, sao chép `.env.example` thành `.env.local`, sửa `VITE_API_BASE_URL`, rồi khởi động lại frontend. Nếu thay cổng frontend, sửa đồng thời lệnh chạy và biến `FRONTEND_ORIGINS` của backend.

```powershell
# terminal backend, ví dụ đổi cặp cổng
$env:SERVER_PORT='8082'
$env:FRONTEND_ORIGINS='http://localhost:3002'
# frontend/.env.local: VITE_API_BASE_URL=http://localhost:8082
# terminal frontend: npm.cmd start -- --port 3002
```

Đăng nhập local bằng cùng hostname ở cả hai phía: `localhost` với `localhost`, hoặc `127.0.0.1` với `127.0.0.1`, để cookie session hoạt động đúng.

## 4. Tài khoản và chức năng

| Tài khoản demo | Mật khẩu demo | Quyền |
|---|---|---|
| admin | Tut11@2026 | Xem, thêm, sửa, xóa, upload ảnh |
| user | User11@2026 | Chỉ xem, tìm kiếm, sắp xếp, phân trang |

Trang đăng ký luôn tạo tài khoản **USER**, không cho tự chọn ADMIN. Mật khẩu được lưu bằng BCrypt. Đăng xuất hủy session; mutation sử dụng CSRF token lấy từ `/api/auth/csrf`. React dùng cookie `withCredentials`, không lưu mật khẩu/token đăng nhập vào localStorage.

Dữ liệu đầu tiên: **12 students, 12 employees, 6 companies, 6 jobs**. Sinh viên đầu tiên mang tên của bản nộp này. `DemoData.java` chứa toàn bộ dữ liệu mẫu và tự tạo khi bảng tương ứng trống; không nhân đôi khi khởi động lại với dữ liệu còn tồn tại. Các tài khoản mẫu cũng chỉ được tạo nếu chưa có username.

- Student: danh sách, chi tiết, thêm, sửa, xóa; tên bắt buộc, tuổi ít nhất 18.
- Employee: CRUD, tên 3-30 ký tự, tuổi 18-55, email/địa chỉ/ảnh bắt buộc, chọn một Company và nhiều Job.
- Company: CRUD; tên 3-30 ký tự, địa chỉ 5-58 ký tự, ảnh bắt buộc.
- Job: CRUD; title bắt buộc, description tối đa 2000 ký tự.
- Tất cả module có tìm theo tên/title, sort, phân trang 5 bản ghi, trạng thái tải/rỗng/lỗi, xác nhận xóa.
- Ảnh PNG/JPEG từ máy, tối đa 5 MB, mỗi chiều tối đa 5000 pixel; server đọc và chuyển thành PNG, tên tệp UUID. File lưu trong `backend/uploads/`.
- Company/Job đang gắn với Employee trả **409** khi xóa. Sửa Employee để bỏ liên kết, hoặc xóa Employee trước. Xóa Employee chỉ bỏ các liên kết của chính Employee đó.
- DTO đầu ra không serialize quan hệ hai chiều, không lộ password hay gây vòng lặp JSON.

## 5. API và tính kế thừa

| Resource | Danh sách / thêm | Chi tiết / sửa / xóa |
|---|---|---|
| Student | GET / POST `/api/students` | GET / PUT / DELETE `/api/students/{id}` |
| Employee | GET / POST `/api/employees` | GET / PUT / DELETE `/api/employees/{id}` |
| Company | GET / POST `/api/companies` | GET / PUT / DELETE `/api/companies/{id}` |
| Job | GET / POST `/api/jobs` | GET / PUT / DELETE `/api/jobs/{id}` |

Danh sách nhận `search`, `page` (từ 0), `size` (1-100), `sort` (`id`, `name` hoặc `title`, thêm `age` cho Student/Employee), `direction` (`asc`/`desc`). Response gồm `content`, `page`, `size`, `totalElements`, `totalPages`.

Student JSON: `{"name":"Test Student","age":20}`. Company JSON: `{"name":"Test Company","address":"Hanoi city","image":"/assets/company.svg"}`. Job JSON: `{"title":"Developer","description":"Build products"}`. Employee JSON: `{"name":"Test Employee","age":25,"email":"test@example.com","address":"Hanoi","image":"/assets/person.svg","companyId":1,"jobIds":[1,2]}`.

Giữ nguyên endpoint của bản Hạnh Tut10: **GET `/`**, **GET `/detail/{id}`**, **POST `/add`**, **PUT `/update/{id}`**, **DELETE `/delete/{id}`**. GET `/` vẫn trả mảng, không chuyển sang page object. Các endpoint này hiện yêu cầu đăng nhập và mutation cần ADMIN + CSRF như API mới.

Auth: GET `/api/auth/csrf`, POST `/api/auth/login`, GET `/api/auth/me`, POST `/api/auth/logout`, POST `/api/auth/register`. Upload: POST `/api/uploads`, multipart field `file`; response `{"url":"/uploads/<uuid>.png"}`.

Trạng thái: **201** tạo, **200** đọc/sửa, **204** xóa, **400** dữ liệu sai, **401** chưa đăng nhập, **403** thiếu quyền/CSRF, **404** ID không có, **409** xung đột/liên kết, **413** file quá lớn. Validation trả `message` và `errors` theo field.

## 6. Những điểm đã đồng nhất

1. Lấy Tut10 của **2301140029** làm mốc: cổng backend 8080, Java 21, Spring Boot 4.1.2, Maven Wrapper 3.10.0, package `com.example.demo`, các endpoint Student cũ.
2. Tut9 dùng cổng 8081; bản này đổi về **8080**. React giữ **3000**, CORS cấu hình tường minh trong Spring Security cho GET/POST/PUT/DELETE/OPTIONS.
3. Database Tut10 là `demo`; bản này dùng **demo_2301140029** để không lẫn dữ liệu 3 sinh viên. User/password MySQL nhận qua biến môi trường, không chép mật khẩu cá nhân của Tut10.
4. Sửa thư mục `emtity` thành `entity` để khớp package. Đổi starter web sang `spring-boot-starter-webmvc` phù hợp cấu trúc Spring Boot 4; vẫn giữ phiên bản 4.1.2.
5. Employee, Company, Job và quan hệ kế thừa Tut7-9; bổ sung REST API vì ZIP Tut10 được cung cấp mới có Student. Giữ ý nghĩa chức năng đăng nhập, phân quyền, ảnh, search/sort/pagination; giao diện Thymeleaf được thay bằng React.
6. Đề Tut11 ghi tham chiếu Tutorial 5 nhưng Tut5 trong bộ tài liệu là JUnit/Mockito; backend REST thực tế nằm ở Tut10.
7. React dùng Vite thay công cụ khởi tạo trong ảnh hướng dẫn; vẫn React + Axios, chạy `npm start` ở cổng 3000.
8. Chạy từng bản một vì cùng cặp cổng chuẩn. Nếu chạy đồng thời, đổi SERVER_PORT, VITE_API_BASE_URL và cổng/CORS frontend theo hướng dẫn trên.

## 7. Kiểm tra lại

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd package
# terminal frontend
cd frontend
npm.cmd ci
npm.cmd run build
node tests/browser-smoke.mjs
```

10 bài test Java sử dụng H2 **chỉ trong test**, chạy HTTP thật qua security/controller/repository. Runtime mặc định luôn MySQL. Browser test cần backend và frontend đang chạy, tạo/xóa dữ liệu riêng và đăng ký một USER thử nghiệm; tài khoản thử nghiệm được giữ lại trong database kiểm tra. Mặc định dùng Chrome trên Windows; đặt `PLAYWRIGHT_CHROMIUM_EXECUTABLE` hoặc dùng `npx playwright install chromium` khi cần.

Đổi mật khẩu demo trước lần chạy đầu bằng `DEMO_ADMIN_PASSWORD`, `DEMO_USER_PASSWORD` nếu muốn. Thay đổi các biến này sau khi tài khoản đã tồn tại không tự reset mật khẩu; dòng hướng dẫn demo ở màn hình đăng nhập là mật khẩu mặc định. `SEED_ENABLED=false` tắt initializer (khi đó cần chuẩn bị tài khoản BCrypt và dữ liệu trước).

Phiên bản này: **People & Co**, theme **green**. ZIP chỉ chứa source, lockfile, wrapper, SQL, tài liệu và bằng chứng; không chứa `node_modules`, `target`, `dist`, cache IDE, database files hoặc mật khẩu MySQL cá nhân.

# Tut11 - Ghi chú bàn giao và đồng nhất cấu hình

Ba bản đều lấy cấu hình Tut10 của **2301140029** làm mốc:

| Hạng mục | Cấu hình bàn giao |
|---|---|
| Backend | localhost:8080 |
| Frontend | localhost:3000, React + Axios + Vite, `npm start` |
| MySQL | localhost:3306, cấu hình qua DB_URL |
| Java / Spring Boot | Java 21 / Spring Boot 4.1.2 |
| Maven Wrapper | 3.10.0 |
| Java package | com.example.demo, thư mục entity đã sửa đúng |
| Schema | demo_2301140019 / demo_2301140100 / demo_2301140029 |
| Admin demo | admin / Tut11@2026 |
| User demo | user / User11@2026 |

## Các điều chỉnh so với tài liệu/bài cũ

- Tut9 dùng 8081; đã đồng nhất về 8080 theo bản Hạnh. CORS cho cổng React 3000 được gắn trực tiếp vào Spring Security.
- Giữ Java 21 dù Java mặc định trên máy là 17. Đã kiểm tra bằng JDK 27, xuất bytecode Java 21. Người chạy cần trỏ JAVA_HOME tới JDK tương thích, không phải JDK 17.
- Giữ Boot 4.1.2 của Tut10; đổi starter web sang webmvc theo cấu trúc Boot 4, không hạ phiên bản framework.
- Giữ nguyên 5 endpoint Student cũ; bổ sung namespace /api có phân trang. Các endpoint cũ nay có cùng đăng nhập/phân quyền/CSRF với API mới.
- Database cũ `demo` được tách thành 3 schema. Không sao chép mật khẩu MySQL hay dữ liệu cá nhân từ bài cũ.
- Bổ sung REST API Employee/Company/Job từ mô hình Tut9 vì ZIP Tut10 mới có Student; chuyển các chức năng cần thiết sang React.
- Vite được dùng thay công cụ React trong ảnh đề, vẫn chạy npm start trên cổng 3000.
- Tham chiếu “Tutorial 5” trong Tut11 không khớp bộ tài liệu; dùng Tut10 cho REST API, Tut7-9 cho mô hình và chức năng.

## Khác biệt giữa ba bản

- **2301140019_LuongQuocDuy**: Peoplebase, xanh dương, sidebar sáng; bộ công ty Bluebird/Summit/Orbit.
- **2301140100_CaoThiThanhVan**: Bloom, tím hồng, sidebar tối và thẻ bo tròn; bộ công ty Bloom/Velvet/Meadow.
- **2301140029_NguyenThiMinhHanh**: People & Co, xanh lá, menu ngang và bố cục rộng; bộ công ty Aurora/Lotus/Northstar.
- Từng bản có tên, MSSV, tiêu đề, metadata Maven/npm, schema và dữ liệu mẫu riêng. Chức năng và hợp đồng API được giữ nhất quán.

## Môi trường kiểm tra

- Windows, JDK 27, Node 26.5.1, MySQL 8.0.46.
- Windows không cho bind MySQL 3306 trong phiên này, nên MySQL kiểm tra dùng **13306**. Cấu hình nguồn vẫn mặc định **3306**.
- Bản chức năng ban đầu đã được kiểm tra React:3000 -> backend:8080 -> MySQL:13306.
- Kiểm tra tự động từng bản cá nhân hóa dùng cặp cổng riêng 13000/18080 và schema verification_<MSSV>, qua biến môi trường. Không đổi các cổng mặc định trong source.
- Chi tiết kết quả từng bản và ảnh thực tế nằm trong `evidence/`; xem `VERIFICATION.md` ở thư mục này.

Mỗi ZIP chứa một thư mục gốc đúng MSSV_HoTen, bên trong có backend/, frontend/, database/, evidence/, README.md. ZIP đặt theo cú pháp đề: FullName_StudentID_SE1_Tut11.zip. Không sửa các bài Tut1-10.

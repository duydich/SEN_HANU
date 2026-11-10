# Kết quả kiểm tra Tut11 - Final1

Ngày kiểm tra: **10/11/2026, Asia/Bangkok**.

| Bản nộp | Backend tests | Frontend build | Chrome + MySQL | SQL schema |
|---|---|---|---|---|
| 2301140019 - Lương Quốc Duy | 10 / 10 | PASS | PASS | PASS |
| 2301140100 - Cao Thị Thanh Vân | 10 / 10 | PASS | PASS | PASS |
| 2301140029 - Nguyễn Thị Minh Hạnh | 10 / 10 | PASS | PASS | PASS |

Tổng cộng **30/30 kiểm thử tích hợp backend**, không lỗi; cả 3 bản build frontend thành công và đạt kịch bản trình duyệt với MySQL thật. Bytecode Java đã kiểm tra ở mức Java 21 (major 65), dù JDK dùng để kiểm tra là 27.

Kịch bản trình duyệt thực hiện đăng nhập/đăng xuất, đăng ký, CRUD cả 4 module, xem chi tiết, upload và hiển thị ảnh, gắn Company/Job cho Employee, kiểm tra chặn xóa liên kết, tìm kiếm, sắp xếp, phân trang, quyền USER chỉ xem và bố cục mobile 390px. Mỗi bản có 6 ảnh bằng chứng riêng trong evidence/. Đã kiểm tra trực quan giao diện desktop/mobile, thông tin sinh viên và bố cục của cả ba phiên bản.

Từng file ZIP đã được kiểm tra giải nén/integrity, thư mục gốc, loại bỏ thư mục build/cache/upload thử nghiệm và rà soát các nhãn tạm. Mã SHA-256 ở CHECKSUMS.sha256.

## Cần chú ý khi chạy trên máy khác

- Cấu hình mặc định theo 2301140029: backend **8080**, React **3000**, MySQL **3306**, Java **21**, Spring Boot **4.1.2**.
- Java mặc định trên máy kiểm tra hiện là 17. Cần đặt JAVA_HOME sang JDK 21 hoặc JDK tương thích mới hơn để chạy/build.
- Windows chặn bind MySQL 3306 trong phiên kiểm tra; MySQL thử nghiệm dùng **13306** qua DB_URL. Source mặc định không bị đổi cổng.
- Schema đã tách riêng: demo_2301140019, demo_2301140100, demo_2301140029. Không nhập mật khẩu MySQL cá nhân vào source; đặt DB_PASSWORD ở terminal.
- Cùng cặp cổng nên chạy từng bản một, hoặc đổi đồng bộ SERVER_PORT, VITE_API_BASE_URL, cổng React và FRONTEND_ORIGINS.
- Hướng dẫn đầy đủ trong README.md của từng bản; các khác biệt và điểm kế thừa trong CONFIG_NOTES.md.

Các bài Tut1-10 và file đề gốc được giữ nguyên. Bộ ZIP là source project, cần cài dependency bằng Maven Wrapper/npm ci; không phải ứng dụng đóng gói kèm JDK/MySQL.

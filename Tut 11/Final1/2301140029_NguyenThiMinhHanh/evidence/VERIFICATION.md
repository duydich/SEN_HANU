# Verification - Nguyễn Thị Minh Hạnh (2301140029)

Executed 10 November 2026 (Asia/Bangkok).

- Backend `mvn package`: PASS, 10 integration tests, 0 failures, 0 errors.
- Frontend `npm ci` and `npm run build`: PASS with lockfile.
- Provided SQL schema: imported successfully into isolated MySQL 8.0.46 schema `verification_2301140029`.
- Real Chrome + React + Spring Boot + MySQL browser scenario: PASS.
- Tested: login, registration, logout, Student/Employee/Company/Job create/read/update/delete, image upload and display, relation selection, delete conflict, search, sort, pagination, view-only role, 390px mobile layout, no unhandled browser exceptions.
- MySQL persistence checked after application shutdown; see `mysql-persistence.txt`.
- Screenshots 01-06 show this version's login, desktop tables, detail, mobile and view-only screens.
- Build environment: JDK 27 compiling Java release 21; Node 26.5.1; Chrome; Windows.
- Automated runs override ports to React 13000 / backend 18080 / MySQL 13306. Delivered source defaults remain React 3000 / backend 8080 / MySQL 3306.
- H2 is used only by the repeatable 10-test Java suite. Browser tests use real MySQL.

The full HTTP/browser tests create temporary business records and remove them; the registered view-only test account remains in the isolated verification schema only. No test database files are included in the ZIP.

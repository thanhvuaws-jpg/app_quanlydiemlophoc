# 🏫 ClassManager VAA - Ứng dụng Quản lý Lớp học & Sổ điểm Giáo viên

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![Material Design 3](https://img.shields.io/badge/UI-Material%20Design%203-blue?style=for-the-badge)

**ClassManager VAA** là ứng dụng di động Android chạy offline hoàn toàn (cục bộ trên thiết bị), được thiết kế đặc biệt nhằm hỗ trợ giáo viên và giảng viên quản lý thông tin lớp học, danh sách học sinh/sinh viên, theo dõi quá trình học tập và quản lý bảng điểm (Điểm giữa kỳ, Cuối kỳ, Học kỳ) một cách khoa học, hiệu quả và tiện lợi.

---

## 👩‍💻 Tác giả
*   **Thủy Tiên**

---

## 🚀 Tính năng nổi bật

*   **📊 Dashboard Thống kê thông minh**: Cung cấp số liệu tổng quan về số lớp quản lý, tổng số sinh viên đang giảng dạy và số lượng điểm số đã nhập. Hiển thị nhanh danh sách các lớp học truy cập gần nhất.
*   **📂 Quản lý lớp học linh hoạt**: Cho phép thêm mới lớp học, cập nhật thông tin môn học, mã lớp học và quản lý học sinh theo từng lớp chuyên biệt.
*   **👥 Quản lý danh sách học sinh**:
    *   Xem danh sách học sinh thuộc từng lớp học với thiết kế dạng danh sách trực quan.
    *   Thêm học sinh thủ công bằng form điền thông tin nhanh gọn.
    *   **Nhập danh sách học sinh từ file Excel (.xlsx, .xls) / CSV**: Tiết kiệm thời gian bằng cách tải file danh sách sinh viên lên. Hệ thống cung cấp giao diện **ánh xạ cột thông minh** cho phép giáo viên tự chọn cột chứa *Họ và tên* và cột chứa *Mã số sinh viên (MSSV)*.
*   **📝 Nhập & Xuất bảng điểm chi tiết**:
    *   Nhập điểm Giữa kỳ (GK), Cuối kỳ (CK), lựa chọn Học kỳ cho học sinh trong lớp.
    *   **Xuất bảng điểm ra file CSV**: Dễ dàng xuất bảng điểm của một lớp học bất kỳ thành file CSV tiêu chuẩn, hỗ trợ tính năng chia sẻ trực tiếp qua Zalo, Email, Google Drive... để nộp báo cáo.
    *   **Nhập bảng điểm hàng loạt**: Cho phép dán trực tiếp chuỗi CSV hoặc chọn file CSV bảng điểm bên ngoài. Hệ thống tích hợp thuật toán phân tích thông minh nhận diện đúng các cột điểm số và thông tin học sinh, tự động bỏ qua trùng lặp hoặc cập nhật điểm số mới.
    *   *Tính năng đặc biệt*: Tự động xử lý dấu phẩy thập phân (ví dụ: `8,5` thành `8.5`) khi đọc dữ liệu xuất ra từ Microsoft Excel cấu hình tiếng Việt để tránh lỗi định dạng điểm số.
*   **🔐 Hệ thống xác thực an toàn**: Đăng ký và đăng nhập tài khoản cá nhân của giáo viên để đảm bảo dữ liệu học sinh của mỗi giáo viên được lưu trữ độc lập và bảo mật.
*   **💾 Sao lưu & Khôi phục**: Hỗ trợ xuất file cơ sở dữ liệu SQLite (`.db`) ra bộ nhớ máy và nhập lại để khôi phục trong trường hợp đổi máy hoặc sao lưu định kỳ.
*   **🌓 Chế độ sáng/tối (Dark Mode)**: Giao diện Material Design 3 đẹp mắt hỗ trợ chuyển đổi giao diện Dark Mode mượt mà dựa trên cấu hình hệ thống hoặc tùy chỉnh cá nhân.

---

## 🛠️ Công nghệ & Kiến trúc

*   **Ngôn ngữ**: Native Java
*   **Nền tảng**: Android SDK (Hỗ trợ từ API 24 trở lên)
*   **Cơ sở dữ liệu**: SQLite thuần (Sử dụng `SQLiteOpenHelper` tối ưu cho truy vấn ngoại tuyến)
*   **Giao diện (UI)**: Material Design Components (`MaterialCardView`, `RecyclerView`, `TextInputLayout`, `BottomNavigationView`...)
*   **Mô hình kiến trúc**: **MVC (Model-View-Controller)** kết hợp với mẫu thiết kế **DAO (Data Access Object)** nhằm tách biệt lớp dữ liệu (Database Layer) và lớp hiển thị (UI Layer).
*   **Thư viện bên thứ ba**: `Readable3` (Đọc file Excel dung lượng nhẹ, hiệu năng cao không làm đơ ứng dụng).

---

## 📂 Cấu trúc thư mục mã nguồn

```text
app/src/main/java/vn/edu/vaa/classmanagerdemo/
├── activities/       # Quản lý giao diện (MainActivity, ClassActivity, StudentListActivity, ImportExportActivity, LoginActivity...)
├── adapters/         # Bộ chuyển đổi dữ liệu cho RecyclerView (ClassAdapter, StudentAdapter, ScoreAdapter...)
├── database/         # Định nghĩa bảng dữ liệu & các lớp truy vấn DAO (DatabaseHelper, UserDAO, ClassDAO, StudentDAO, ScoreDAO)
├── models/           # Các lớp thực thể dữ liệu (User, SchoolClass, Student, Score)
├── storage/          # Tiện ích lưu preference (AppPreferenceManager), hỗ trợ import Excel/CSV
├── utils/            # Các lớp hỗ trợ (NavigationHelper, Validator, LoadingHelper...)
└── MyApplication.java# Lớp khởi tạo toàn cục của ứng dụng
```

---

## ⚙️ Hướng dẫn cài đặt & Chạy ứng dụng

### Yêu cầu môi trường
*   **Android Studio** (Bản Flamingo trở lên được khuyến nghị)
*   **Java Development Kit (JDK 17 trở lên)**
*   Thiết bị thật chạy Android hoặc Máy ảo (Emulator) hỗ trợ hệ điều hành Android API Level 24 (Android 7.0) trở lên.

### Các bước cài đặt
1.  **Tải mã nguồn về máy:**
    ```bash
    git clone https://github.com/thanhvuaws-jpg/app_quanlydiemlophoc.git
    ```
2.  **Mở dự án:** Khởi động Android Studio, chọn **Open** và dẫn đường dẫn đến thư mục `ClassManagerDemoV2`.
3.  **Đồng bộ Gradle:** Đợi dự án tải và đồng bộ các thư viện qua Gradle (Gradle Sync).
4.  **Cài đặt ứng dụng:** 
    *   Kết nối điện thoại Android của bạn qua cổng USB (đã bật tùy chọn Gỡ lỗi USB - USB Debugging) hoặc khởi chạy một Máy ảo.
    *   Nhấn nút **Run** (biểu tượng Play màu xanh lá tam giác hoặc phím tắt `Shift + F10`) trong Android Studio.

---

## 📖 Hướng dẫn sử dụng chi tiết

1.  **Đăng ký & Đăng nhập**:
    *   Mở ứng dụng lần đầu, chọn **Đăng ký tài khoản** và tạo tài khoản cá nhân cho bạn.
    *   Sử dụng tài khoản đó để đăng nhập. Bạn có thể chọn "Ghi nhớ đăng nhập" để bỏ qua bước này ở các lần mở app sau.
2.  **Tạo lớp học**:
    *   Tại màn hình chính hoặc màn hình Quản lý lớp, nhấn nút **Thêm lớp học** (dấu cộng).
    *   Nhập Tên lớp học (ví dụ: *Công nghệ phần mềm*) và môn học tương ứng.
3.  **Nhập học sinh vào lớp**:
    *   Chọn vào lớp học vừa tạo từ danh sách lớp.
    *   Nhấn nút Menu (biểu tượng 3 chấm) góc trên bên phải màn hình danh sách học sinh:
        *   Chọn **Nhập từ Excel/CSV**.
        *   Chọn file chứa thông tin học sinh từ bộ nhớ máy.
        *   Chọn đúng cột ánh xạ tương ứng với *Mã học sinh* và *Họ tên* rồi bấm **Bắt đầu Import**.
4.  **Quản lý điểm số**:
    *   Để nhập điểm nhanh hoặc xuất bảng điểm, nhấn vào thẻ **Nhập/Xuất bảng điểm** từ màn hình chính hoặc chọn từ menu.
    *   Chọn lớp học cần thao tác.
    *   Nhập hoặc chỉnh sửa trực tiếp điểm Giữa kỳ, Cuối kỳ và Học kỳ.
    *   Nhấn **Xuất bảng điểm** để lưu file CSV bảng điểm và chia sẻ nhanh qua các nền tảng chat hoặc email.
5.  **Cài đặt & Sao lưu**:
    *   Vào tab **Cài đặt** để bật tắt Chế độ tối (Dark Mode).
    *   Nhấn **Sao lưu & Khôi phục** dữ liệu để sao lưu file cơ sở dữ liệu an toàn.

---

## 🤝 Đóng góp và Phát triển

Mọi đóng góp, báo cáo lỗi (Bug Report) hoặc đề xuất tính năng mới luôn được tác giả chào đón. Vui lòng tạo Issue hoặc Pull Request trực tiếp trên kho chứa GitHub này.

# 🎓 VAA GPA Tracker (Sổ Tay GPA VAA)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![i18n](https://img.shields.io/badge/Bilingual-EN%20%7C%20VI-blue?style=for-the-badge)

**VAA GPA Tracker** là một ứng dụng Android toàn diện hoạt động ngoại tuyến (offline-first), được thiết kế đặc biệt dành cho sinh viên (đặc biệt là sinh viên Học viện Hàng không Việt Nam - VAA) để dễ dàng quản lý điểm số, theo dõi tiến trình GPA, ước lượng điểm tương lai và đánh giá điều kiện học bổng.

---

## 🚀 Tính năng nổi bật

*   **📊 Bảng điều khiển (Dashboard) thông minh**: Cung cấp cái nhìn tổng quan về tình hình học tập. Xem nhanh GPA tích lũy (Hệ 10 và Hệ 4), tổng số tín chỉ tích lũy, và tổng số môn học đã hoàn thành.
*   **🏅 Đánh giá học bổng VAA**: Tự động đánh giá điểm GPA hiện tại của bạn so với tiêu chuẩn học bổng của VAA (Xuất sắc/Giỏi) và đưa ra gợi ý (ví dụ: "Cần thêm 0.15 GPA để đạt học bổng Giỏi").
*   **🔮 Mô phỏng GPA tương lai**: Bạn thắc mắc điểm kỳ tới sẽ ảnh hưởng đến GPA tổng thế nào? Hãy thêm các môn học "giả định" để mô phỏng sự thay đổi của GPA tích lũy trước cả khi thi.
*   **💰 Ước tính học phí**: Tự động tính toán tổng học phí tích lũy ước tính dựa trên số tín chỉ đã lưu.
*   **📥 Nhập & Xuất dữ liệu (CSV)**: Sao lưu toàn bộ lịch sử điểm số ra file CSV an toàn, hoặc nhập điểm có sẵn từ văn bản/file cực kỳ nhanh chóng. Có tích hợp nhận diện trùng lặp thông minh.
*   **📤 Chia sẻ bảng điểm**: Tự động tạo hình ảnh "Bảng vàng thành tích" mang đậm dấu ấn cá nhân để bạn dễ dàng chia sẻ lên mạng xã hội.
*   **🌐 Hỗ trợ Song ngữ (Anh & Việt)**: Chuyển đổi ngôn ngữ mượt mà. Giao diện thay đổi hoàn toàn tương thích với ngôn ngữ trên điện thoại của bạn.
*   **🔒 Cục bộ & Bảo mật**: Toàn bộ dữ liệu của bạn được lưu hoàn toàn trên điện thoại bằng hệ cơ sở dữ liệu SQLite (không cần Internet để sử dụng các tính năng cốt lõi).

---

## 📱 Ảnh chụp màn hình (Screenshots)

<!-- Gợi ý: Bạn có thể đưa các file ảnh vào thư mục `screenshots/` trong project, sau đó thay đổi link bên dưới thành link tương ứng -->
<!-- ![Màn hình chính](screenshots/home.png) -->
*(Thêm ảnh chụp ứng dụng vào đây)*

---

## 🛠️ Công nghệ & Kiến trúc

*   **Ngôn ngữ**: Native Java
*   **Nền tảng**: Android SDK
*   **Database**: SQLite (sử dụng `SQLiteOpenHelper` thuần của Android)
*   **Giao diện (UI)**: Material Design Components (`MaterialCardView`, `TextInputLayout`, v.v.)
*   **Mô hình thiết kế**: MVC (Model-View-Controller) / DAO pattern cho tầng giao tiếp dữ liệu
*   **Đồ họa**: Sử dụng Canvas tùy chỉnh (Custom Canvas rendering) để vẽ ảnh Share GPA

---

## 📂 Cấu trúc thư mục

```text
app/src/main/java/vn/edu/vaa/classmanagerdemo/
├── activities/       # Quản lý giao diện (MainActivity, GradeActivity, v.v.)
├── database/         # Kết nối & xử lý SQLite (UserDAO, ScoreDAO)
├── models/           # Các đối tượng dữ liệu (User, Score)
├── utils/            # Các tiện ích hỗ trợ (GpaShareRenderer, LoadingHelper)
└── views/            # Các View tự custom (GpaChartView)
```

---

## ⚙️ Hướng dẫn cài đặt

### Yêu cầu hệ thống
*   Android Studio (Khuyên dùng bản mới nhất)
*   Java Development Kit (JDK 8 trở lên)
*   Máy thật hoặc máy ảo Android chạy API Level 24+

### Cài đặt
1.  **Clone dự án:**
    ```bash
    git clone https://github.com/thanhvuaws-jpg/app_quanlylophoc.git
    ```
2.  **Mở bằng Android Studio:** Khởi chạy Android Studio, chọn "Open an existing project" và tìm đến thư mục vừa clone về.
3.  **Build và Chạy:** Nhấn nút "Run" (Shift + F10) trong Android Studio để cài đặt app lên thiết bị thật hoặc máy ảo của bạn.

---

## 🌟 Cách sử dụng

1.  **Đăng ký/Đăng nhập:** Tạo một tài khoản lưu cục bộ trên máy.
2.  **Thêm môn học:** Vào tab "Bảng điểm" để thêm các môn học đã qua, số tín chỉ và điểm hệ 10.
3.  **Kiểm tra Dashboard:** Quay lại màn hình Chính (Home) để xem GPA hệ 4 vừa được tính toán tự động và kiểm tra mức học bổng.
4.  **Mô phỏng tương lai:** Nhấn nút "Mô phỏng GPA tương lai" để lên kế hoạch học tập cho kỳ tới.
5.  **Xuất dữ liệu:** Sử dụng tính năng "Nhập/Xuất file" để sao lưu dữ liệu của bạn để tránh mất mát.

---

## 🤝 Đóng góp (Contribution)

Nếu bạn có hứng thú, đừng ngần ngại fork project này, tạo pull requests hoặc báo lỗi (report issues)! Mọi đóng góp đều luôn được chào đón.

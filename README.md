# 🎓 VAA GPA Tracker (Sổ Tay GPA VAA)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)
![i18n](https://img.shields.io/badge/Bilingual-EN%20%7C%20VI-blue?style=for-the-badge)

**VAA GPA Tracker** is a comprehensive, offline-first Android application designed specifically for students (especially those at the Vietnam Aviation Academy - VAA) to effortlessly manage their academic records, monitor GPA progression, predict future outcomes, and evaluate scholarship eligibility.

---

## 🚀 Key Features

*   **📊 Smart Study Dashboard**: Get a bird's-eye view of your academic standing. View your cumulative GPA (System 10 and System 4), total earned credits, and the number of completed subjects at a glance.
*   **🏅 Scholarship Evaluation**: Automatically assesses your current GPA against VAA's scholarship criteria (Excellent/Good) and provides actionable hints (e.g., "Need 0.15 more GPA for Good Scholarship").
*   **🔮 Future GPA Simulator**: Wondering how next semester's grades will affect your overall GPA? Add "mock" subjects to simulate your future cumulative GPA before the exams even begin.
*   **💰 Tuition Estimator**: Calculates your estimated total cumulative tuition fee based on your saved credit hours.
*   **📥 Import & Export (CSV)**: Never lose your data. Back up your entire grade history to a CSV file, or import existing grades from text/files seamlessly. Includes smart duplicate detection.
*   **📤 Share Your Success**: Generate a beautiful, customizable "GPA Achievement Card" image to share your academic milestones with friends and family on social media.
*   **🌐 Fully Bilingual (English & Vietnamese)**: Native support for both English and Vietnamese. The UI adapts perfectly based on your device's language settings.
*   **🔒 Local & Secure**: All data is stored locally on your device using a robust SQLite database (Room/Native). No internet connection is required to use the core features.

---

## 📱 Screenshots

*(Add screenshots here)*

---

## 🛠️ Tech Stack & Architecture

*   **Language**: Native Java
*   **Platform**: Android SDK
*   **Database**: SQLite (using native Android `SQLiteOpenHelper`)
*   **UI Components**: Material Design Components (`MaterialCardView`, `TextInputLayout`, etc.)
*   **Design Pattern**: MVC (Model-View-Controller) / DAO pattern for database abstraction
*   **Charting**: Custom Canvas rendering for the GPA Share Card

---

## 📂 Project Structure

```text
app/src/main/java/vn/edu/vaa/classmanagerdemo/
├── activities/       # UI Controllers (MainActivity, GradeActivity, etc.)
├── database/         # SQLite Handlers & DAOs (UserDAO, ScoreDAO)
├── models/           # Data entities (User, Score)
├── utils/            # Utilities (GpaShareRenderer, LoadingHelper)
└── views/            # Custom UI Components (GpaChartView)
```

---

## ⚙️ Getting Started

### Prerequisites
*   Android Studio (Latest version recommended)
*   Java Development Kit (JDK 8 or higher)
*   Android device or emulator running API Level 24+

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/thanhvuaws-jpg/app_quanlylophoc.git
    ```
2.  **Open the project:** Launch Android Studio, select "Open an existing project", and choose the cloned directory.
3.  **Build and Run:** Click the "Run" button (Shift + F10) in Android Studio to install the app on your connected device or emulator.

---

## 🌟 How to Use

1.  **Register/Login:** Create a local account on your device.
2.  **Add Subjects:** Go to the "Grades" tab to add your completed subjects, credits, and scores (System 10).
3.  **Check Dashboard:** Return to the Home screen to see your calculated System 4 GPA and scholarship status.
4.  **Simulate Future:** Tap the "Simulate Future GPA" button to plan your upcoming semesters.
5.  **Export Data:** Use the "Import/Export" menu to safeguard your data.

---

## 🤝 Contribution

Feel free to fork this project, submit pull requests, or report issues! Contributions are always welcome.

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

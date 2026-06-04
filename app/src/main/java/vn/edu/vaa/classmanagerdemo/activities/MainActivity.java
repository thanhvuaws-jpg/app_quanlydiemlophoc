package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import vn.edu.vaa.classmanagerdemo.views.GpaChartView;

public class MainActivity extends BaseActivity {
    private AppPreferenceManager prefs;
    private UserDAO userDAO;
    private ScoreDAO scoreDAO;

    private TextView tvWelcome;
    private TextView tvStatClasses;  // Credits
    private TextView tvStatStudents; // Cumulative GPA
    private TextView tvStatScores;   // Courses count

    private TextView tvScholarshipStatus;
    private TextView tvTuitionTotal;
    private GpaChartView gpaChartView;
    private android.widget.ProgressBar progressGpa;
    private TextView tvGpaProgressLabel;
    private TextView tvGpaProgressHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_main);
        
        userDAO = new UserDAO(this);
        scoreDAO = new ScoreDAO(this);
        
        initViews();
        initListeners();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs != null && prefs.isLoggedIn()) {
            renderUserInfo();
            loadStats();
            NavigationHelper.setupBottomNavigation(this, R.id.nav_home);
        }
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStatClasses = findViewById(R.id.tvStatClasses);
        tvStatStudents = findViewById(R.id.tvStatStudents);
        tvStatScores = findViewById(R.id.tvStatScores);
        tvScholarshipStatus = findViewById(R.id.tvScholarshipStatus);
        tvTuitionTotal = findViewById(R.id.tvTuitionTotal);
        gpaChartView = findViewById(R.id.gpaChartView);
        progressGpa = findViewById(R.id.progressGpa);
        tvGpaProgressLabel = findViewById(R.id.tvGpaProgressLabel);
        tvGpaProgressHint = findViewById(R.id.tvGpaProgressHint);
    }

    private void initListeners() {
        MaterialCardView cardClasses = findViewById(R.id.cardClasses);
        MaterialCardView cardGrades = findViewById(R.id.cardGrades);
        MaterialCardView cardImportExport = findViewById(R.id.cardImportExport);
        MaterialCardView cardSettings = findViewById(R.id.cardSettings);

        cardClasses.setOnClickListener(v -> startActivity(new Intent(this, GradeActivity.class)));
        cardGrades.setOnClickListener(v -> startActivity(new Intent(this, GradePredictActivity.class)));
        cardImportExport.setOnClickListener(v -> startActivity(new Intent(this, ImportExportActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void renderUserInfo() {
        String name = prefs.getFullName().isEmpty() ? prefs.getUsername() : prefs.getFullName();
        tvWelcome.setText(getString(R.string.hello_user, name));
    }

    private void loadStats() {
        new Thread(() -> {
            int userId = prefs.getCurrentUserId();
            User user = userDAO.findById(userId);
            int trainingPoints = user != null ? user.getTrainingPoints() : 80;

            int totalCredits = scoreDAO.getTotalCreditsByStudentId(userId);
            float cumulativeGpa = scoreDAO.getCumulativeGpaByStudentId(userId);
            int subjectCount = scoreDAO.getSubjectCountByStudentId(userId);

            // Calculate tuition
            long rate = prefs.getTuitionRate();
            long totalTuition = totalCredits * rate;

            // Determine scholarship eligibility based on VAA rules
            // Xuất sắc: GPA >= 3.6 & ĐRL >= 90
            // Giỏi: GPA >= 3.2 & ĐRL >= 80
            // Khá: GPA >= 2.5 & ĐRL >= 70
            String scholarshipText;
            if (cumulativeGpa >= 3.6f && trainingPoints >= 90) {
                scholarshipText = getString(R.string.scholarship_excellent, cumulativeGpa, trainingPoints);
            } else if (cumulativeGpa >= 3.2f && trainingPoints >= 80) {
                scholarshipText = getString(R.string.scholarship_good, cumulativeGpa, trainingPoints);
            } else if (cumulativeGpa >= 2.5f && trainingPoints >= 70) {
                scholarshipText = getString(R.string.scholarship_fair, cumulativeGpa, trainingPoints);
            } else {
                scholarshipText = getString(R.string.scholarship_none);
            }

            // Gather and calculate GPA progress per semester
            List<Score> scores = scoreDAO.getByStudentId(userId);
            Map<String, List<Score>> semMap = new TreeMap<>(); // sorted semesters
            for (Score s : scores) {
                String sem = s.getSemester();
                if (sem == null || sem.trim().isEmpty()) {
                    sem = getString(R.string.uncategorized_semester);
                }
                if (!semMap.containsKey(sem)) {
                    semMap.put(sem, new ArrayList<>());
                }
                semMap.get(sem).add(s);
            }

            List<String> semestersList = new ArrayList<>();
            List<Float> gpaList = new ArrayList<>();

            for (Map.Entry<String, List<Score>> entry : semMap.entrySet()) {
                String semName = entry.getKey();
                List<Score> semScores = entry.getValue();

                float totalPoints = 0f;
                int semCredits = 0;
                for (Score s : semScores) {
                    totalPoints += s.getGrade4() * s.getCredits();
                    semCredits += s.getCredits();
                }
                float semGpa = semCredits > 0 ? (totalPoints / semCredits) : 0f;
                semGpa = Math.round(semGpa * 100f) / 100f; // 2 decimal places

                semestersList.add(semName);
                gpaList.add(semGpa);
            }

            runOnUiThread(() -> {
                tvStatClasses.setText(String.valueOf(totalCredits));
                tvStatStudents.setText(String.format(Locale.US, "%.2f", cumulativeGpa));
                tvStatScores.setText(getString(R.string.courses_count_format, subjectCount));

                tvScholarshipStatus.setText(scholarshipText);
                tvTuitionTotal.setText(getString(R.string.tuition_format, totalTuition, totalCredits, rate));

                // Progress bar GPA tiến tới học bổng
                // Mục tiêu: GPA 3.20 (học bổng Giỏi) là mốc phổ biến nhất
                float targetGpa = 3.20f;
                float progressPercent = targetGpa > 0 ? Math.min(cumulativeGpa / targetGpa * 100f, 100f) : 0f;
                int progressInt = (int) progressPercent;

                if (progressGpa != null) {
                    progressGpa.setProgress(progressInt);
                    // Đổi màu thanh theo mức GPA
                    String barColor;
                    if (cumulativeGpa >= 3.6f)      barColor = "#10B981"; // xanh lá — Xuất sắc
                    else if (cumulativeGpa >= 3.2f) barColor = "#3B82F6"; // xanh dương — Giỏi
                    else if (cumulativeGpa >= 2.5f) barColor = "#6366F1"; // indigo — Khá
                    else if (cumulativeGpa >= 2.0f) barColor = "#F59E0B"; // vàng — TB
                    else                             barColor = "#EF4444"; // đỏ — Yếu
                    android.graphics.drawable.LayerDrawable ld =
                            (android.graphics.drawable.LayerDrawable) progressGpa.getProgressDrawable();
                    android.graphics.drawable.Drawable progressLayer =
                            ld.findDrawableByLayerId(android.R.id.progress);
                    if (progressLayer != null) {
                        progressLayer.setColorFilter(
                                android.graphics.Color.parseColor(barColor),
                                android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
                if (tvGpaProgressLabel != null) {
                    tvGpaProgressLabel.setText(progressInt + "%");
                    String labelColor;
                    if (cumulativeGpa >= 3.2f)      labelColor = "#10B981";
                    else if (cumulativeGpa >= 2.5f) labelColor = "#6366F1";
                    else                             labelColor = "#F59E0B";
                    tvGpaProgressLabel.setTextColor(android.graphics.Color.parseColor(labelColor));
                }
                if (tvGpaProgressHint != null) {
                    String hint;
                    if (cumulativeGpa >= 3.6f) {
                        hint = "🏆 Đã đạt học bổng Xuất sắc (GPA ≥ 3.60)";
                    } else if (cumulativeGpa >= 3.2f) {
                        hint = "🏅 Đã đạt học bổng Giỏi (GPA ≥ 3.20)";
                    } else if (cumulativeGpa >= 2.5f) {
                        float need = 3.20f - cumulativeGpa;
                        hint = String.format(java.util.Locale.US,
                                "Cần thêm %.2f GPA để đạt học bổng Giỏi", need);
                    } else {
                        hint = "Cần GPA ≥ 3.20 để đạt học bổng Giỏi";
                    }
                    tvGpaProgressHint.setText(hint);
                }

                // Bind chart data
                gpaChartView.setData(semestersList, gpaList);
            });
        }).start();
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

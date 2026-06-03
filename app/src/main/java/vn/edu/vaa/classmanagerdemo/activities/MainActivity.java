package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class MainActivity extends AppCompatActivity {
    private AppPreferenceManager prefs;
    private ActionLogger logger;
    private TextView tvWelcome, tvStatClasses, tvStatStudents, tvStatScores;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_main);
        logger = new ActionLogger(this);
        classDAO = new ClassDAO(this);
        studentDAO = new StudentDAO(this);
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
    }

    private void initListeners() {
        // New feature cards
        MaterialCardView cardClasses = findViewById(R.id.cardClasses);
        MaterialCardView cardGrades = findViewById(R.id.cardGrades);
        MaterialCardView cardImport = findViewById(R.id.cardImport);

        cardClasses.setOnClickListener(v -> startActivity(new Intent(this, ClassListActivity.class)));
        cardGrades.setOnClickListener(v -> startActivity(new Intent(this, GradeActivity.class)));
        cardImport.setOnClickListener(v -> startActivity(new Intent(this, ImportStudentActivity.class)));

        // Existing features
        findViewById(R.id.cardStudents).setOnClickListener(v -> startActivity(new Intent(this, StudentActivity.class)));
        findViewById(R.id.cardTodos).setOnClickListener(v -> startActivity(new Intent(this, TodoActivity.class)));
        findViewById(R.id.cardNoteLog).setOnClickListener(v -> startActivity(new Intent(this, NoteLogActivity.class)));
        findViewById(R.id.cardImportExport).setOnClickListener(v -> startActivity(new Intent(this, ImportExportActivity.class)));
        findViewById(R.id.cardSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void renderUserInfo() {
        String name = prefs.getFullName().isEmpty() ? prefs.getUsername() : prefs.getFullName();
        tvWelcome.setText("Xin chào, " + name);
    }

    private void loadStats() {
        int classCount = classDAO.getAll().size();
        int studentCount = studentDAO.getAll().size();
        int scoreCount = scoreDAO.getTotalCount();
        tvStatClasses.setText(String.valueOf(classCount));
        tvStatStudents.setText(String.valueOf(studentCount));
        tvStatScores.setText(String.valueOf(scoreCount));
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    logger.log("Logout: " + prefs.getUsername());
                    prefs.clearLoginSession();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    goLogin();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

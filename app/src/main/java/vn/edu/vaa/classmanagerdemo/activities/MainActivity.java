package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.models.SchoolClass;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class MainActivity extends BaseActivity {
    private AppPreferenceManager prefs;
    private ClassDAO classDAO;

    private TextView tvWelcome;
    private TextView tvStatClasses;
    private TextView tvStatStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_main);
        
        classDAO = new ClassDAO(this);
        
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
    }

    private void initListeners() {
        MaterialCardView cardClasses = findViewById(R.id.cardClasses);
        MaterialCardView cardSettings = findViewById(R.id.cardSettings);

        cardClasses.setOnClickListener(v -> startActivity(new Intent(this, ClassListActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void renderUserInfo() {
        String name = prefs.getFullName().isEmpty() ? prefs.getUsername() : prefs.getFullName();
        tvWelcome.setText(getString(R.string.hello_user, name));
    }

    private void loadStats() {
        new Thread(() -> {
            int teacherId = prefs.getCurrentUserId();
            List<SchoolClass> classes = classDAO.getByTeacherId(teacherId);
            
            int totalClasses = classes.size();
            int totalStudents = 0;
            for (SchoolClass c : classes) {
                totalStudents += c.getStudentCount();
            }

            final int fTotalClasses = totalClasses;
            final int fTotalStudents = totalStudents;

            runOnUiThread(() -> {
                if (tvStatClasses != null) tvStatClasses.setText(String.valueOf(fTotalClasses));
                if (tvStatStudents != null) tvStatStudents.setText(String.valueOf(fTotalStudents));
            });
        }).start();
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

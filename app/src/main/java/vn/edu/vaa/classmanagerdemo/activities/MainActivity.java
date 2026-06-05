package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ClassAdapter;
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
    private TextView tvStatScores;
    private RecyclerView recyclerRecentClasses;
    private ClassAdapter recentClassAdapter;
    private List<SchoolClass> recentClassList = new ArrayList<>();

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
        tvStatScores = findViewById(R.id.tvStatScores);

        recyclerRecentClasses = findViewById(R.id.recyclerRecentClasses);
        recentClassAdapter = new ClassAdapter(recentClassList,
            cls -> {
                Intent i = new Intent(this, StudentListActivity.class);
                i.putExtra("class_id", cls.getId());
                i.putExtra("class_name", cls.getClassName());
                i.putExtra("class_subject", cls.getSubject());
                startActivity(i);
            },
            cls -> {}, cls -> {}
        );
        recyclerRecentClasses.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentClasses.setAdapter(recentClassAdapter);
    }

    private void initListeners() {
        MaterialCardView cardClasses = findViewById(R.id.cardClasses);
        MaterialCardView cardSettings = findViewById(R.id.cardSettings);
        MaterialCardView cardQuickScore = findViewById(R.id.cardQuickScore);

        cardClasses.setOnClickListener(v -> startActivity(new Intent(this, ClassListActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        cardQuickScore.setOnClickListener(v -> startActivity(new Intent(this, ImportExportActivity.class)));
    }

    private void renderUserInfo() {
        String name = prefs.getFullName().isEmpty() ? prefs.getUsername() : prefs.getFullName();
        if (tvWelcome != null) tvWelcome.setText(getString(R.string.hello_user, name));
    }

    private void loadStats() {
        new Thread(() -> {
            int teacherId = prefs.getCurrentUserId();
            List<SchoolClass> classes = classDAO.getByTeacherId(teacherId);
            int totalClasses = classes.size();
            int totalStudents = 0;
            for (SchoolClass c : classes) totalStudents += c.getStudentCount();
            int totalScores = classDAO.getTotalScoreCount(teacherId);
            List<SchoolClass> recent = classes.size() > 3 ? classes.subList(0, 3) : new ArrayList<>(classes);

            final int fTotalClasses = totalClasses;
            final int fTotalStudents = totalStudents;
            final int fTotalScores = totalScores;

            runOnUiThread(() -> {
                if (tvStatClasses != null) tvStatClasses.setText(String.valueOf(fTotalClasses));
                if (tvStatStudents != null) tvStatStudents.setText(String.valueOf(fTotalStudents));
                if (tvStatScores != null) tvStatScores.setText(String.valueOf(fTotalScores));
                recentClassList.clear();
                recentClassList.addAll(recent);
                recentClassAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class SettingsActivity extends BaseActivity {
    private SwitchMaterial swDarkMode;
    private TextInputEditText spLanguage;
    private TextInputEditText edtTrainingPoints;
    private TextInputEditText edtTuitionRate;
    private TextView txtAccountInfo;
    private AppPreferenceManager prefs;
    private UserDAO userDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            goLogin();
            return;
        }
        setContentView(R.layout.activity_settings);
        userDAO = new UserDAO(this);
        currentUser = userDAO.findById(prefs.getCurrentUserId());

        initViews();
        initSpinner();
        initListeners();
        readAndRender();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_settings);
    }

    private void initViews() {
        txtAccountInfo = findViewById(R.id.txtAccountInfo);
        swDarkMode = findViewById(R.id.swDarkMode);
        spLanguage = findViewById(R.id.spLanguage);
        edtTrainingPoints = findViewById(R.id.edtTrainingPoints);
        edtTuitionRate = findViewById(R.id.edtTuitionRate);
    }

    private void initSpinner() {
        String[] langs = {"vi", "en"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, langs);
        spLanguage.setOnClickListener(v -> {
            android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(this);
            popup.setAdapter(adapter);
            popup.setAnchorView(spLanguage);
            popup.setOnItemClickListener((parent, view, pos, id) -> {
                spLanguage.setText(langs[pos]);
                popup.dismiss();
            });
            popup.show();
        });
    }

    private void initListeners() {
        Button btnSave = findViewById(R.id.btnSavePrefs);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnSave.setOnClickListener(v -> handleSavePrefs());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void readAndRender() {
        if (currentUser == null) {
            currentUser = userDAO.findById(prefs.getCurrentUserId());
        }
        int pts = currentUser != null ? currentUser.getTrainingPoints() : 80;
        
        txtAccountInfo.setText("Họ tên: " + prefs.getFullName() + "\n" +
                "Username: " + prefs.getUsername() + "\n" +
                "Email: " + prefs.getEmail() + "\n" +
                "SĐT: " + prefs.getPhone() + "\n" +
                "Giữ đăng nhập: " + (prefs.isRememberLogin() ? "Có" : "Không"));
        
        swDarkMode.setChecked(prefs.isDarkMode());
        spLanguage.setText(prefs.getLanguage());
        edtTrainingPoints.setText(String.valueOf(pts));
        edtTuitionRate.setText(String.valueOf(prefs.getTuitionRate()));
    }

    private void handleSavePrefs() {
        boolean darkMode = swDarkMode.isChecked();
        String language = spLanguage.getText() != null ? spLanguage.getText().toString().trim() : "vi";
        if (language.isEmpty()) language = "vi";
        
        String strPoints = edtTrainingPoints.getText() != null ? edtTrainingPoints.getText().toString().trim() : "80";
        String strRate = edtTuitionRate.getText() != null ? edtTuitionRate.getText().toString().trim() : "400000";
        
        int points = 80;
        try {
            points = Integer.parseInt(strPoints);
            if (points < 0 || points > 100) {
                edtTrainingPoints.setError(getString(R.string.error_invalid_points));
                edtTrainingPoints.requestFocus();
                return;
            }
        } catch (Exception e) {
            edtTrainingPoints.setError(getString(R.string.error_parsing_number));
            edtTrainingPoints.requestFocus();
            return;
        }

        long tuitionRate = 400000L;
        try {
            tuitionRate = Long.parseLong(strRate);
            if (tuitionRate < 0) {
                edtTuitionRate.setError(getString(R.string.error_tuition_negative));
                edtTuitionRate.requestFocus();
                return;
            }
        } catch (Exception e) {
            edtTuitionRate.setError(getString(R.string.error_invalid_tuition));
            edtTuitionRate.requestFocus();
            return;
        }

        String oldLang = prefs.getLanguage();

        // Save preferences and database
        prefs.saveAppSettings(darkMode, language);
        prefs.saveTuitionRate(tuitionRate);
        
        if (currentUser != null) {
            userDAO.updateTrainingPoints(currentUser.getId(), points);
            currentUser.setTrainingPoints(points);
        }

        // Apply dark mode immediately
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();

        if (!oldLang.equals(language)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            readAndRender();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                    prefs.clearLoginSession();
                    // Reset to light mode upon logout
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    goLogin();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

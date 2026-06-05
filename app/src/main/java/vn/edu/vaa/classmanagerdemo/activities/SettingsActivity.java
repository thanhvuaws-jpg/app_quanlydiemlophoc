package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class SettingsActivity extends BaseActivity {
    private SwitchMaterial swDarkMode;
    private TextView tvProfileInitials;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private AppPreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            goLogin();
            return;
        }
        setContentView(R.layout.activity_settings);

        initViews();
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
        swDarkMode = findViewById(R.id.swDarkMode);
        tvProfileInitials = findViewById(R.id.tvProfileInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
    }

    private void initListeners() {
        Button btnSave = findViewById(R.id.btnSavePrefs);
        Button btnLogout = findViewById(R.id.btnLogout);
        android.view.View btnBackupRestore = findViewById(R.id.btnBackupRestore);

        btnSave.setOnClickListener(v -> handleSavePrefs());
        btnLogout.setOnClickListener(v -> confirmLogout());
        if (btnBackupRestore != null) {
            btnBackupRestore.setOnClickListener(v -> {
                Intent intent = new Intent(this, BackupRestoreActivity.class);
                startActivity(intent);
            });
        }
    }

    private void readAndRender() {
        String name = prefs.getFullName();
        String email = prefs.getEmail();
        String phone = prefs.getPhone();

        if (name == null || name.trim().isEmpty()) name = "Giáo viên";
        if (email == null || email.trim().isEmpty()) email = "gv@school.edu.vn";
        if (phone == null || phone.trim().isEmpty()) phone = "Chưa cập nhật SĐT";

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfilePhone.setText(phone);

        // Generate initials
        String initials = "GV";
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            if (parts.length > 1) {
                initials = (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            } else {
                initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            }
        }
        tvProfileInitials.setText(initials);

        swDarkMode.setChecked(prefs.isDarkMode());
    }

    private void handleSavePrefs() {
        boolean darkMode = swDarkMode.isChecked();
        prefs.saveAppSettings(darkMode, prefs.getLanguage());

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
        readAndRender();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                    prefs.clearLoginSession();
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

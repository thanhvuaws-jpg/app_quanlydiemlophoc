package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class SettingsActivity extends BaseActivity {
    private SwitchMaterial swDarkMode;
    private TextInputEditText spLanguage;
    private TextView txtAccountInfo;
    private AppPreferenceManager prefs;
    private UserDAO userDAO;

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
        txtAccountInfo.setText("Họ tên: " + prefs.getFullName() + "\n" +
                "Username: " + prefs.getUsername() + "\n" +
                "Email: " + prefs.getEmail() + "\n" +
                "SĐT: " + prefs.getPhone() + "\n" +
                "Giữ đăng nhập: " + (prefs.isRememberLogin() ? "Có" : "Không"));

        swDarkMode.setChecked(prefs.isDarkMode());
        spLanguage.setText(prefs.getLanguage());
    }

    private void handleSavePrefs() {
        boolean darkMode = swDarkMode.isChecked();
        String language = spLanguage.getText() != null ? spLanguage.getText().toString().trim() : "vi";
        if (language.isEmpty()) language = "vi";

        String oldLang = prefs.getLanguage();
        prefs.saveAppSettings(darkMode, language);

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

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
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class SettingsActivity extends AppCompatActivity {
    private SwitchMaterial swDarkMode;
    private TextInputEditText spLanguage;
    private TextView txtAccountInfo, txtResult, txtExplanation;
    private AppPreferenceManager prefs;
    private ActionLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            goLogin();
            return;
        }
        setContentView(R.layout.activity_settings);
        logger = new ActionLogger(this);
        initViews();
        initSpinner();
        initListeners();
        readAndRender();
        showInitialExplanation();
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
        txtResult = findViewById(R.id.txtResult);
        txtExplanation = findViewById(R.id.txtExplanation);
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
        Button btnRead = findViewById(R.id.btnReadPrefs);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnSave.setOnClickListener(v -> handleSavePrefs());
        btnRead.setOnClickListener(v -> handleReadPrefs());
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
        prefs.saveAppSettings(darkMode, language);
        
        // Apply dark mode immediately
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        logger.log("Save settings: darkMode=" + darkMode + ", language=" + language);
        txtResult.setText("Đã lưu cấu hình: darkMode=" + darkMode + ", language=" + language);
        Toast.makeText(this, "Đã lưu cấu hình", Toast.LENGTH_SHORT).show();
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Lưu cấu hình\"",
                "darkMode lấy từ Switch; language lấy từ Spinner.",
                "Không cần validate vì đây là 2 lựa chọn có sẵn trên giao diện.",
                "Gọi AppPreferenceManager.saveAppSettings(darkMode, language).",
                "Dữ liệu lưu vào SharedPreferences APP_PREFS nhưng không ghi đè session đăng nhập.",
                "Lần sau mở màn hình Tài khoản & Cấu hình, app đọc lại đúng darkMode và language."
        ));
    }

    private void handleReadPrefs() {
        readAndRender();
        txtResult.setText("Đã đọc lại cấu hình từ SharedPreferences.");
        logger.log("Read settings");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Đọc lại cấu hình\"",
                "Không lấy dữ liệu từ form; app đọc session và cấu hình đã lưu.",
                "Nếu key chưa tồn tại thì dùng giá trị mặc định.",
                "Đọc fullName, username, email, phone, rememberLogin, darkMode, language từ AppPreferenceManager.",
                "SharedPreferences APP_PREFS lưu dữ liệu tài khoản đăng nhập hiện tại và cấu hình app.",
                "Giao diện được cập nhật theo dữ liệu đã lưu."
        ));
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Đăng xuất sẽ xóa session và tắt giữ đăng nhập. Dữ liệu sinh viên, Todo, file vẫn giữ nguyên.")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logger.log("Logout from settings: " + prefs.getUsername());
                    prefs.clearLoginSession();
                    // Reset to light mode upon logout
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    goLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showInitialExplanation() {
        txtExplanation.setText("Màn hình này hoạt động như phần tài khoản/cấu hình của một app quản lý bình thường.\n\n" +
                "Session đăng nhập được tạo ở LoginActivity. Ở đây chỉ đọc thông tin tài khoản và lưu cấu hình app như darkMode, language. Đăng xuất sẽ xóa session để lần sau phải đăng nhập lại.");
    }

    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

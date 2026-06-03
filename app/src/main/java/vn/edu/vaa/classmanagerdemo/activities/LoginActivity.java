package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private CheckBox cbRemember;
    private TextView txtResult, txtExplanation;
    private AppPreferenceManager prefs;
    private UserDAO userDAO;
    private ActionLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (prefs.shouldAutoLogin()) {
            openMainAndFinish("Tự động đăng nhập do đã bật Ghi nhớ đăng nhập");
            return;
        }
        setContentView(R.layout.activity_login);
        userDAO = new UserDAO(this);
        logger = new ActionLogger(this);
        initViews();
        initListeners();
        showInitialState();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtLoginUsername);
        edtPassword = findViewById(R.id.edtLoginPassword);
        cbRemember = findViewById(R.id.cbRememberLogin);
        txtResult = findViewById(R.id.txtLoginResult);
        txtExplanation = findViewById(R.id.txtExplanation);
    }

    private void initListeners() {
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnOpenRegister);
        Button btnFillDemo = findViewById(R.id.btnFillDemoAccount);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnFillDemo.setOnClickListener(v -> {
            edtUsername.setText("admin");
            edtPassword.setText("123456");
            cbRemember.setChecked(true);
            txtResult.setText("Đã điền tài khoản mẫu: admin / 123456");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Điền tài khoản mẫu\"",
                    "Không đọc từ SQLite; gán sẵn username=admin và password=123456 vào form.",
                    "Không cần validate vì đây chỉ là thao tác hỗ trợ demo.",
                    "SetText cho 2 EditText và setChecked(true) cho CheckBox ghi nhớ đăng nhập.",
                    "Chưa lưu gì xuống SharedPreferences hoặc SQLite ở bước này.",
                    "Người dùng có thể nhấn Đăng nhập để kiểm tra thật với bảng users trong SQLite."
            ));
        });
    }

    private void showInitialState() {
        String lastUser = prefs.getUsername();
        if (!lastUser.isEmpty()) edtUsername.setText(lastUser);
        txtResult.setText("Tài khoản mẫu: admin / 123456 hoặc teacher / 123456");
        txtExplanation.setText("Màn hình đăng nhập thật của app quản lý.\n\n" +
                "Luồng xử lý: nhập username/password → kiểm tra bảng users trong SQLite → nếu đúng thì lưu session vào SharedPreferences → chuyển vào Dashboard.\n\n" +
                "Nếu chọn Ghi nhớ đăng nhập, lần sau mở app sẽ tự động vào Dashboard.");
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        boolean remember = cbRemember.isChecked();

        if (username.isEmpty()) {
            edtUsername.setError("Vui lòng nhập tên đăng nhập");
            edtUsername.requestFocus();
            txtResult.setText("Đăng nhập thất bại: username rỗng.");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            txtResult.setText("Đăng nhập thất bại: password rỗng.");
            return;
        }

        User user = userDAO.login(username, password);
        if (user == null) {
            txtResult.setText("Sai tên đăng nhập hoặc mật khẩu.");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Đăng nhập\"",
                    "username và password từ form đăng nhập.",
                    "Username/password không được rỗng.",
                    "Gọi UserDAO.login(username, password). SQLite truy vấn bảng users bằng WHERE username=? AND password=?.",
                    "Đọc dữ liệu từ SQLite database class_manager.db, bảng users. Không lưu session vì đăng nhập sai.",
                    "Không chuyển màn hình; hiển thị thông báo sai tài khoản hoặc mật khẩu."
            ));
            return;
        }

        prefs.saveLoginSession(user, remember);
        
        // Apply user-specific theme immediately upon login
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        logger.log("Login: " + user.getUsername() + ", remember=" + remember);
        txtResult.setText("Đăng nhập thành công: " + user.getFullName());
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Đăng nhập\"",
                "username, password từ EditText; rememberLogin từ CheckBox.",
                "Kiểm tra username/password không rỗng; sau đó kiểm tra đúng/sai bằng SQLite.",
                "UserDAO.login() trả về User hợp lệ. AppPreferenceManager.saveLoginSession(user, remember) lưu phiên đăng nhập.",
                "SQLite bảng users xác thực tài khoản; SharedPreferences APP_PREFS lưu loggedIn, rememberLogin, currentUserId, username, fullName.",
                remember ? "Đăng nhập thành công và bật giữ đăng nhập: lần sau mở app sẽ tự vào Dashboard." : "Đăng nhập thành công nhưng không giữ đăng nhập: lần sau mở app sẽ yêu cầu login lại."
        ));
        Toast.makeText(this, "Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
        openMainAndFinish("login_success");
    }

    private void openMainAndFinish(String reason) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("login_reason", reason);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

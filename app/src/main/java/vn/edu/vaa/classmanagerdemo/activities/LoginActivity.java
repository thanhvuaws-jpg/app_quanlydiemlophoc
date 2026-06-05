package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;

public class LoginActivity extends BaseActivity {
    private EditText edtUsername, edtPassword;
    private CheckBox cbRemember;
    private AppPreferenceManager prefs;
    private UserDAO userDAO;

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
        initViews();
        initListeners();
        String lastUser = prefs.getUsername();
        if (!lastUser.isEmpty()) edtUsername.setText(lastUser);
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtLoginUsername);
        edtPassword = findViewById(R.id.edtLoginPassword);
        cbRemember = findViewById(R.id.cbRememberLogin);
    }

    private void initListeners() {
        Button btnLogin = findViewById(R.id.btnLogin);
        android.view.View btnRegister = findViewById(R.id.btnOpenRegister);
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        boolean remember = cbRemember.isChecked();

        if (username.isEmpty()) {
            edtUsername.setError(getString(R.string.error_empty_username));
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError(getString(R.string.error_empty_password));
            edtPassword.requestFocus();
            return;
        }

        User user = userDAO.login(username, password);
        if (user == null) {
            Toast.makeText(this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.saveLoginSession(user, remember);

        // Apply user-specific theme immediately upon login
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toast.makeText(this, getString(R.string.hello_user, user.getFullName()), Toast.LENGTH_SHORT).show();
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

package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.utils.Validator;

public class RegisterActivity extends BaseActivity {
    private EditText edtFullName, edtUsername, edtPassword, edtConfirmPassword, edtEmail, edtPhone;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userDAO = new UserDAO(this);
        initViews();
        initListeners();
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtRegisterFullName);
        edtUsername = findViewById(R.id.edtRegisterUsername);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtRegisterConfirmPassword);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPhone = findViewById(R.id.edtRegisterPhone);
    }

    private void initListeners() {
        android.view.View btnRegister = findViewById(R.id.btnRegisterAccount);
        android.view.View btnBackLogin = findViewById(R.id.btnBackLogin);
        btnRegister.setOnClickListener(v -> handleRegister());
        btnBackLogin.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {
        if (!Validator.require(edtFullName, getString(R.string.error_empty_name))) return false;
        if (!Validator.require(edtUsername, getString(R.string.error_empty_username))) return false;
        String username = edtUsername.getText().toString().trim();
        if (username.length() < 4) {
            edtUsername.setError(getString(R.string.error_short_username));
            edtUsername.requestFocus();
            return false;
        }
        if (userDAO.usernameExists(username)) {
            edtUsername.setError(getString(R.string.error_username_exists));
            edtUsername.requestFocus();
            return false;
        }
        String password = edtPassword.getText().toString();
        if (password.length() < 6) {
            edtPassword.setError(getString(R.string.error_short_password));
            edtPassword.requestFocus();
            return false;
        }
        String confirm = edtConfirmPassword.getText().toString();
        if (!password.equals(confirm)) {
            edtConfirmPassword.setError(getString(R.string.error_password_mismatch));
            edtConfirmPassword.requestFocus();
            return false;
        }
        return Validator.optionalEmail(edtEmail, getString(R.string.error_invalid_email))
                && Validator.optionalPhone(edtPhone, getString(R.string.error_invalid_phone));
    }

    private void handleRegister() {
        if (!validateForm()) {
            return;
        }

        User user = new User(
                edtFullName.getText().toString().trim(),
                edtUsername.getText().toString().trim(),
                edtPassword.getText().toString(),
                edtEmail.getText().toString().trim(),
                edtPhone.getText().toString().trim()
        );
        long id = userDAO.register(user);
        if (id == -1) {
            Toast.makeText(this, getString(R.string.error_register_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show();
        // Delay nhỏ để Toast hiển thị rồi mới finish
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::finish, 1200);
    }
}

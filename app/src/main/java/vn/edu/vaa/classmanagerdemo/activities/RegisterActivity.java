package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.utils.Validator;

public class RegisterActivity extends AppCompatActivity {
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
        Button btnRegister = findViewById(R.id.btnRegisterAccount);
        Button btnBackLogin = findViewById(R.id.btnBackLogin);
        btnRegister.setOnClickListener(v -> handleRegister());
        btnBackLogin.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {
        if (!Validator.require(edtFullName, "Họ tên không được rỗng")) return false;
        if (!Validator.require(edtUsername, "Tên đăng nhập không được rỗng")) return false;
        String username = edtUsername.getText().toString().trim();
        if (username.length() < 4) {
            edtUsername.setError("Tên đăng nhập tối thiểu 4 ký tự");
            edtUsername.requestFocus();
            return false;
        }
        if (userDAO.usernameExists(username)) {
            edtUsername.setError("Tên đăng nhập đã tồn tại");
            edtUsername.requestFocus();
            return false;
        }
        String password = edtPassword.getText().toString();
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }
        String confirm = edtConfirmPassword.getText().toString();
        if (!password.equals(confirm)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmPassword.requestFocus();
            return false;
        }
        return Validator.optionalEmail(edtEmail, "Email không hợp lệ")
                && Validator.optionalPhone(edtPhone, "Số điện thoại phải gồm 10-11 chữ số");
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
            Toast.makeText(this, "Đăng ký thất bại. Có thể username đã tồn tại.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        // Delay nhỏ để Toast hiển thị rồi mới finish
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::finish, 1200);
    }
}

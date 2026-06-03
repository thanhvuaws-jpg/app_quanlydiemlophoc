package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.UserDAO;
import vn.edu.vaa.classmanagerdemo.models.User;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.Validator;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtUsername, edtPassword, edtConfirmPassword, edtEmail, edtPhone;
    private TextView txtResult, txtExplanation;
    private UserDAO userDAO;
    private ActionLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userDAO = new UserDAO(this);
        logger = new ActionLogger(this);
        initViews();
        initListeners();
        txtExplanation.setText("Màn hình đăng ký tài khoản quản lý.\n\n" +
                "Dữ liệu đăng ký được lưu vào bảng users trong SQLite. Sau khi đăng ký thành công, người dùng quay lại màn hình Login để đăng nhập.");
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtRegisterFullName);
        edtUsername = findViewById(R.id.edtRegisterUsername);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtRegisterConfirmPassword);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPhone = findViewById(R.id.edtRegisterPhone);
        txtResult = findViewById(R.id.txtRegisterResult);
        txtExplanation = findViewById(R.id.txtExplanation);
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
            txtResult.setText("Không đăng ký: dữ liệu chưa hợp lệ.");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Đăng ký tài khoản\"",
                    "fullName, username, password, confirmPassword, email, phone từ form đăng ký.",
                    "Kiểm tra rỗng, username tối thiểu 4 ký tự, username không trùng, password tối thiểu 6 ký tự, xác nhận mật khẩu khớp, email/phone hợp lệ.",
                    "Nếu validate lỗi thì dừng, không insert vào SQLite.",
                    "Chưa ghi dữ liệu vì form chưa hợp lệ.",
                    "Hiển thị lỗi ngay tại EditText tương ứng."
            ));
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
            txtResult.setText("Đăng ký thất bại. Có thể username đã tồn tại.");
            return;
        }
        logger.log("Register user: " + user.getUsername());
        txtResult.setText("Đăng ký thành công. ID tài khoản = " + id + ". Hãy quay lại đăng nhập.");
        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Đăng ký tài khoản\"",
                "Lấy fullName, username, password, email, phone từ form đăng ký.",
                "Tất cả điều kiện validate đã đạt: username không trùng, password hợp lệ, email/phone hợp lệ nếu có nhập.",
                "Tạo object User và gọi UserDAO.register(user). SQLite insert vào bảng users.",
                "Dữ liệu được lưu vào SQLite database class_manager.db, bảng users. Mật khẩu được hash SHA-256 trước khi lưu — không thể đọc ngược lại plaintext.",
                "Tài khoản được tạo. Người dùng quay lại Login và đăng nhập bằng tài khoản vừa đăng ký."
        ));
    }
}

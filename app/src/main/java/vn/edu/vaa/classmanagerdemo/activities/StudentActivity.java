package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.StudentAdapter;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.Validator;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class StudentActivity extends AppCompatActivity {
    private EditText edtName, edtClass, edtEmail, edtPhone, edtStudentCode;
    private TextView txtResult, txtExplanation;
    private RecyclerView recyclerView;
    private StudentDAO dao;
    private ActionLogger logger;
    private final List<Student> students = new ArrayList<>();
    private StudentAdapter adapter;
    private int selectedStudentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferenceManager prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_student);
        dao = new StudentDAO(this);
        logger = new ActionLogger(this);
        initViews();
        initRecyclerView();
        initListeners();
        loadStudents("");
        txtExplanation.setText("Chức năng quản lý sinh viên dùng SQLite CRUD.\n\n" +
                "Thêm, sửa, xóa, tìm kiếm sinh viên. Sinh viên thêm từ đây tự động liên kết vào bảng lớp.");
        NavigationHelper.setupBottomNavigation(this, R.id.nav_students);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_students);
    }

    private void initViews() {
        edtStudentCode = findViewById(R.id.edtStudentCode);
        edtName = findViewById(R.id.edtName);
        edtClass = findViewById(R.id.edtClass);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        txtResult = findViewById(R.id.txtResult);
        txtExplanation = findViewById(R.id.txtExplanation);
        recyclerView = findViewById(R.id.recyclerStudents);
    }

    private void initRecyclerView() {
        adapter = new StudentAdapter(students, new StudentAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(Student student, int position) {
                selectedStudentId = student.getId();
                edtStudentCode.setText(student.getStudentCode());
                edtName.setText(student.getName());
                edtClass.setText(student.getClassName());
                edtEmail.setText(student.getEmail());
                edtPhone.setText(student.getPhone());
                txtResult.setText("Đã chọn sinh viên id=" + student.getId());
                txtExplanation.setText(ExplanationBuilder.build(
                        "Click vào item sinh viên trong RecyclerView",
                        "Lấy object Student tại vị trí được click: id, studentCode, name, className, email, phone.",
                        "Không cần validate vì đây là dữ liệu đã có trong danh sách.",
                        "Đưa dữ liệu Student lên form và lưu selectedStudentId để chuẩn bị cập nhật.",
                        "Dữ liệu đang tồn tại trong SQLite bảng students; form chỉ hiển thị bản sao để sửa.",
                        "Người dùng có thể chỉnh sửa form rồi nhấn Cập nhật sinh viên."
                ));
            }

            @Override
            public void onStudentLongClick(Student student, int position) {
                showDeleteDialog(student);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initListeners() {
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnClear = findViewById(R.id.btnClear);
        SearchView searchView = findViewById(R.id.searchView);

        btnAdd.setOnClickListener(v -> handleAddStudent());
        btnUpdate.setOnClickListener(v -> handleUpdateStudent());
        btnClear.setOnClickListener(v -> clearForm());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { loadStudents(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { loadStudents(newText); return true; }
        });
    }

    private boolean validateForm() {
        return Validator.require(edtName, "Họ tên không được rỗng")
                && Validator.require(edtClass, "Lớp không được rỗng")
                && Validator.optionalEmail(edtEmail, "Email không hợp lệ")
                && Validator.optionalPhone(edtPhone, "Số điện thoại phải gồm 10-11 chữ số");
    }

    private Student getStudentFromForm() {
        Student s = new Student(
                selectedStudentId,
                edtName.getText().toString().trim(),
                edtClass.getText().toString().trim(),
                edtEmail.getText().toString().trim(),
                edtPhone.getText().toString().trim()
        );
        s.setStudentCode(edtStudentCode.getText().toString().trim());
        // Tự động tạo/lấy classId từ tên lớp để liên kết với bảng classes
        String className = edtClass.getText().toString().trim();
        if (!className.isEmpty()) {
            ClassDAO classDAO = new ClassDAO(this);
            ClassRoom cr = classDAO.getOrCreate(className, "");
            s.setClassId(cr.getId());
        }
        return s;
    }

    private void handleAddStudent() {
        if (!validateForm()) {
            txtResult.setText("Không thêm: dữ liệu form chưa hợp lệ.");
            return;
        }
        Student s = getStudentFromForm();
        s.setId(0);
        long id = dao.insertFull(s);
        logger.log("Insert Student: " + s.getName());
        loadStudents("");
        clearFormOnly();
        Toast.makeText(this, id == -1 ? "Thêm thất bại" : "Thêm thành công", Toast.LENGTH_SHORT).show();
        txtResult.setText("Insert SQLite trả về id=" + id);
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Thêm sinh viên\"",
                "Lấy studentCode, name, className, email, phone từ các EditText.",
                "Kiểm tra name/className không rỗng; email đúng định dạng nếu có nhập; phone gồm 10-11 chữ số nếu có nhập.",
                "Tạo object Student, gọi ClassDAO.getOrCreate() để lấy classId, rồi gọi StudentDAO.insertFull().",
                "Dữ liệu được ghi vào SQLite database class_manager.db, bảng students với đầy đủ student_code và class_id.",
                "RecyclerView hiển thị sinh viên mới; sinh viên xuất hiện trong ClassDetailActivity của lớp tương ứng."
        ));
    }

    private void handleUpdateStudent() {
        if (selectedStudentId <= 0) {
            Toast.makeText(this, "Hãy click chọn một sinh viên trước", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateForm()) return;
        Student s = getStudentFromForm();
        int rows = dao.update(s);
        logger.log("Update Student id=" + s.getId() + ": " + s.getName());
        loadStudents("");
        clearFormOnly();
        txtResult.setText("Update SQLite: số dòng bị ảnh hưởng = " + rows);
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Cập nhật sinh viên đã chọn\"",
                "Lấy dữ liệu mới từ form và selectedStudentId đã có từ lần click item.",
                "Validate giống thao tác thêm.",
                "Gọi StudentDAO.update(student) với điều kiện WHERE id=? — cập nhật đủ 6 field bao gồm student_code và class_id.",
                "SQLite cập nhật bản ghi trong bảng students.",
                "Danh sách được load lại và form được làm mới."
        ));
    }

    private void showDeleteDialog(Student student) {
        txtExplanation.setText(ExplanationBuilder.build(
                "Long click vào item sinh viên",
                "Lấy id và name của sinh viên được nhấn giữ.",
                "Không xóa ngay; yêu cầu người dùng xác nhận bằng AlertDialog.",
                "Nếu chọn Xóa, gọi StudentDAO.delete(student.getId()), cập nhật RecyclerView và ghi log.",
                "Xóa bản ghi trong SQLite bảng students.",
                "Nếu xác nhận, sinh viên biến mất khỏi danh sách; nếu Hủy, dữ liệu giữ nguyên."
        ));
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa sinh viên \"" + student.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    int rows = dao.delete(student.getId());
                    logger.log("Delete Student id=" + student.getId() + ": " + student.getName());
                    loadStudents("");
                    Toast.makeText(this, "Đã xóa " + rows + " dòng", Toast.LENGTH_SHORT).show();
                    txtResult.setText("Delete SQLite: số dòng bị ảnh hưởng = " + rows);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadStudents(String keyword) {
        students.clear();
        students.addAll(dao.search(keyword));
        adapter.notifyDataSetChanged();
        if (keyword != null && !keyword.trim().isEmpty()) {
            txtResult.setText("Tìm kiếm: \"" + keyword + "\" → " + students.size() + " kết quả");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Nhập từ khóa trong SearchView",
                    "keyword = nội dung người dùng đang nhập.",
                    "Nếu keyword rỗng thì lấy toàn bộ danh sách; nếu không rỗng thì tìm theo name, className hoặc student_code.",
                    "Gọi StudentDAO.search(keyword), dùng câu SQL LIKE ?.",
                    "Đọc dữ liệu từ SQLite bảng students.",
                    "RecyclerView chỉ hiển thị các sinh viên khớp từ khóa."
            ));
        }
    }

    private void clearForm() {
        clearFormOnly();
        txtResult.setText("Đã làm mới form.");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Làm mới form\"",
                "Không lấy dữ liệu để lưu.",
                "Không cần validate.",
                "Xóa nội dung các EditText và đặt selectedStudentId = -1.",
                "Không thay đổi SQLite.",
                "Form trống, sẵn sàng thêm sinh viên mới."
        ));
    }

    private void clearFormOnly() {
        selectedStudentId = -1;
        edtStudentCode.setText("");
        edtName.setText("");
        edtClass.setText("");
        edtEmail.setText("");
        edtPhone.setText("");
    }
}

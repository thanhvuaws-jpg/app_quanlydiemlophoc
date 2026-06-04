package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import vn.edu.vaa.classmanagerdemo.adapters.StudentAdapter;

public class StudentListActivity extends BaseActivity {

    private StudentDAO studentDAO;
    private RecyclerView recyclerStudents;
    private List<Student> studentList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private int classId;
    private String className;
    private String classSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        classId = getIntent().getIntExtra("class_id", -1);
        className = getIntent().getStringExtra("class_name");
        classSubject = getIntent().getStringExtra("class_subject");

        Toolbar toolbar = findViewById(R.id.toolbarStudents);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(className);
        }

        studentDAO = new StudentDAO(this);
        recyclerStudents = findViewById(R.id.recyclerStudents);

        studentAdapter = new StudentAdapter(studentList,
            student -> {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("student_id", student.getId());
                intent.putExtra("student_name", student.getFullName());
                intent.putExtra("student_code", student.getStudentCode());
                intent.putExtra("class_id", classId);
                intent.putExtra("class_subject", classSubject);
                startActivity(intent);
            },
            student -> confirmDeleteStudent(student)
        );
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(studentAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddStudent);
        fab.setOnClickListener(DebounceClickListener.wrap(v -> showAddStudentDialog()));

        loadStudents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }

    private void loadStudents() {
        List<Student> list = studentDAO.getByClassId(classId);
        studentList.clear();
        studentList.addAll(list);
        studentAdapter.notifyDataSetChanged();

        TextView tvCount = findViewById(R.id.tvStudentCount);
        if (tvCount != null) tvCount.setText(list.size() + " học sinh");
    }

    private void showAddStudentDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        EditText edtCode = view.findViewById(R.id.edtStudentCode);
        EditText edtName = view.findViewById(R.id.edtStudentName);

        new AlertDialog.Builder(this)
            .setTitle("Thêm học sinh")
            .setView(view)
            .setPositiveButton("Lưu", (d, w) -> {
                String code = edtCode.getText().toString().trim();
                String name = edtName.getText().toString().trim();
                if (code.isEmpty()) { Toast.makeText(this, "Nhập mã học sinh", Toast.LENGTH_SHORT).show(); return; }
                if (name.isEmpty()) { Toast.makeText(this, "Nhập họ tên", Toast.LENGTH_SHORT).show(); return; }
                if (studentDAO.existsByCode(classId, code)) {
                    Toast.makeText(this, "Mã học sinh đã tồn tại trong lớp", Toast.LENGTH_SHORT).show(); return;
                }
                studentDAO.insert(new Student(classId, code, name));
                loadStudents();
                Toast.makeText(this, "Đã thêm " + name, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void confirmDeleteStudent(Student student) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa học sinh")
            .setMessage("Xóa \"" + student.getFullName() + "\" và toàn bộ điểm?")
            .setPositiveButton("Xóa", (d, w) -> {
                studentDAO.deleteById(student.getId());
                loadStudents();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { NavigationHelper.finishWithSlide(this); return true; }
        return super.onOptionsItemSelected(item);
    }
}

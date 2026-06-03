package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ScoreAdapter;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.Student;
import android.util.Log;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.LoadingHelper;

public class GradeActivity extends AppCompatActivity {

    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;

    private Spinner spinnerClass, spinnerStudent, spinnerSemester;
    private EditText edtSubject, edtScoreValue;
    private TextView tvAverage, tvScoreCount;
    private RecyclerView recyclerScores;

    private static final String TAG = "GradeActivity";
    private final LoadingHelper loading = new LoadingHelper();
    private List<ClassRoom> classList = new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();
    private final List<Score> scoreList = new ArrayList<>();
    private ScoreAdapter scoreAdapter;

    private int preselectedStudentId = -1;
    private int preselectedClassId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new AppPreferenceManager(this).isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_grade);

        Toolbar toolbar = findViewById(R.id.toolbarGrade);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preselectedStudentId = getIntent().getIntExtra("studentId", -1);
        preselectedClassId = getIntent().getIntExtra("classId", -1);
        String studentName = getIntent().getStringExtra("studentName");
        if (getSupportActionBar() != null && studentName != null) {
            getSupportActionBar().setTitle("Điểm: " + studentName);
        }

        classDAO = new ClassDAO(this);
        studentDAO = new StudentDAO(this);
        scoreDAO = new ScoreDAO(this);

        spinnerClass = findViewById(R.id.spinnerGradeClass);
        spinnerStudent = findViewById(R.id.spinnerGradeStudent);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        edtSubject = findViewById(R.id.edtSubject);
        edtScoreValue = findViewById(R.id.edtScoreValue);
        tvAverage = findViewById(R.id.tvScoreAverage);
        tvScoreCount = findViewById(R.id.tvScoreCount);
        recyclerScores = findViewById(R.id.recyclerScores);

        scoreAdapter = new ScoreAdapter(scoreList, (score, position) -> confirmDeleteScore(score, position));
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        recyclerScores.setAdapter(scoreAdapter);

        setupSemesters();
        loadClasses();

        Button btnSave = findViewById(R.id.btnSaveScore);
        btnSave.setOnClickListener(DebounceClickListener.wrap(v -> handleSaveScore()));
    }

    private void setupSemesters() {
        String[] semesters = {"HK1 2024-2025", "HK2 2024-2025", "HK3 2024-2025",
                "HK1 2025-2026", "HK2 2025-2026", "HK3 2025-2026"};
        spinnerSemester.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters));
    }

    private void loadClasses() {
        try {
            loading.show(this, "Đang tải danh sách lớp...");
            classList = classDAO.getAll();
        } catch (Exception e) {
            loading.dismiss();
            String err = "Lỗi tải lớp: " + e.getMessage();
            Log.e(TAG, err, e);
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
            return;
        }
        loading.dismiss();
        if (classList.isEmpty()) {
            Toast.makeText(this, "Chưa có lớp nào. Hãy tạo lớp trước.", Toast.LENGTH_LONG).show();
            return;
        }
        ArrayAdapter<ClassRoom> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);

        spinnerClass.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) {
                loadStudentsForClass(classList.get(pos).getId());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Pre-select class if coming from ClassDetail
        if (preselectedClassId > 0) {
            for (int i = 0; i < classList.size(); i++) {
                if (classList.get(i).getId() == preselectedClassId) {
                    spinnerClass.setSelection(i);
                    break;
                }
            }
        } else {
            loadStudentsForClass(classList.get(0).getId());
        }
    }

    private void loadStudentsForClass(int classId) {
        studentList = studentDAO.getByClassId(classId);
        if (studentList.isEmpty()) {
            spinnerStudent.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{"(Chưa có sinh viên)"}));
            scoreList.clear();
            scoreAdapter.notifyDataSetChanged();
            updateStats(-1);
            return;
        }
        ArrayAdapter<Student> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, studentList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudent.setAdapter(adapter);

        spinnerStudent.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) {
                loadScoresForStudent(studentList.get(pos).getId());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        if (preselectedStudentId > 0) {
            for (int i = 0; i < studentList.size(); i++) {
                if (studentList.get(i).getId() == preselectedStudentId) {
                    spinnerStudent.setSelection(i);
                    break;
                }
            }
        } else {
            loadScoresForStudent(studentList.get(0).getId());
        }
    }

    private void loadScoresForStudent(int studentId) {
        scoreList.clear();
        scoreList.addAll(scoreDAO.getByStudentId(studentId));
        scoreAdapter.notifyDataSetChanged();
        updateStats(studentId);
    }

    private void updateStats(int studentId) {
        if (studentId < 0) {
            tvAverage.setText("ĐTB: --");
            tvScoreCount.setText("0 môn");
            return;
        }
        float avg = scoreDAO.getAverageByStudentId(studentId);
        tvScoreCount.setText(scoreList.size() + " môn");
        if (scoreList.isEmpty()) {
            tvAverage.setText("ĐTB: chưa có điểm");
        } else {
            String grade = new Score(0, studentId, "", avg, "").getGradeLabel();
            tvAverage.setText(String.format("ĐTB: %.2f  •  %s", avg, grade));
        }
    }

    private void handleSaveScore() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "Hãy chọn sinh viên", Toast.LENGTH_SHORT).show();
            return;
        }
        String subject = edtSubject.getText().toString().trim();
        String scoreStr = edtScoreValue.getText().toString().trim();
        if (subject.isEmpty()) { edtSubject.setError("Nhập tên môn học"); return; }
        if (scoreStr.isEmpty()) { edtScoreValue.setError("Nhập điểm"); return; }
        float scoreVal;
        try {
            scoreVal = Float.parseFloat(scoreStr);
        } catch (NumberFormatException e) { edtScoreValue.setError("Điểm không hợp lệ"); return; }
        if (scoreVal < 0 || scoreVal > 10) { edtScoreValue.setError("Điểm từ 0 đến 10"); return; }

        int selectedPos = spinnerStudent.getSelectedItemPosition();
        if (selectedPos < 0 || selectedPos >= studentList.size()) {
            Toast.makeText(this, "Không xác định được sinh viên — thử chọn lại lớp", Toast.LENGTH_SHORT).show();
            return;
        }
        int studentId = studentList.get(selectedPos).getId();
        String semester = spinnerSemester.getSelectedItem() != null
                ? spinnerSemester.getSelectedItem().toString() : "";

        try {
            loading.show(this, "Đang lưu điểm...");
            scoreDAO.insert(new Score(studentId, subject, scoreVal, semester));
            Log.d(TAG, "Lưu điểm SV id=" + studentId + " môn=" + subject + " điểm=" + scoreVal);
            edtSubject.setText("");
            edtScoreValue.setText("");
            loadScoresForStudent(studentId);
            Toast.makeText(this, "Đã lưu điểm " + subject + ": " + scoreVal, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String err = "Lỗi lưu điểm: " + e.getMessage();
            Log.e(TAG, err, e);
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        } finally {
            loading.dismiss();
        }
    }

    private void confirmDeleteScore(Score score, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa điểm")
                .setMessage("Xóa điểm môn \"" + score.getSubject() + "\": " + score.getScore() + "?")
                .setPositiveButton("Xóa", (d, w) -> {
                    scoreDAO.deleteById(score.getId());
                    if (position >= 0 && position < scoreList.size()) {
                        scoreList.remove(position);
                        scoreAdapter.notifyItemRemoved(position);
                    }
                    int studentId = -1;
                    int selectedPos = spinnerStudent.getSelectedItemPosition();
                    if (!studentList.isEmpty() && selectedPos >= 0 && selectedPos < studentList.size()) {
                        studentId = studentList.get(selectedPos).getId();
                    }
                    updateStats(studentId);
                })
                .setNegativeButton("Hủy", null).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

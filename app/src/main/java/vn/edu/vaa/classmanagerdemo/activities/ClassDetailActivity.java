package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ScoreAdapter;
import vn.edu.vaa.classmanagerdemo.adapters.StudentAdapter;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.Student;

public class ClassDetailActivity extends AppCompatActivity {

    private int classId;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;

    private TextView tvDetailClassName, tvDetailYear, tvDetailCount, tvNoStudents, tvNoScores;
    private final List<Student> students = new ArrayList<>();
    private final List<Score> scores = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        classId = getIntent().getIntExtra("classId", -1);
        String className = getIntent().getStringExtra("className");

        Toolbar toolbar = findViewById(R.id.toolbarClassDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(className != null ? className : "Chi tiết lớp");
        }

        classDAO = new ClassDAO(this);
        studentDAO = new StudentDAO(this);
        scoreDAO = new ScoreDAO(this);

        tvDetailClassName = findViewById(R.id.tvDetailClassName);
        tvDetailYear = findViewById(R.id.tvDetailYear);
        tvDetailCount = findViewById(R.id.tvDetailCount);
        tvNoStudents = findViewById(R.id.tvNoStudents);
        tvNoScores = findViewById(R.id.tvNoScores);

        RecyclerView rvStudents = findViewById(R.id.recyclerClassStudents);
        studentAdapter = new StudentAdapter(students, new StudentAdapter.OnStudentClickListener() {
            @Override public void onStudentClick(Student s, int pos) {
                Intent i = new Intent(ClassDetailActivity.this, GradeActivity.class);
                i.putExtra("studentId", s.getId());
                i.putExtra("studentName", s.getName());
                i.putExtra("classId", classId);
                startActivity(i);
            }
            @Override public void onStudentLongClick(Student s, int pos) {}
        });
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(studentAdapter);

        RecyclerView rvScores = findViewById(R.id.recyclerClassScores);
        scoreAdapter = new ScoreAdapter(scores, null);
        rvScores.setLayoutManager(new LinearLayoutManager(this));
        rvScores.setAdapter(scoreAdapter);

        FloatingActionButton fab = findViewById(R.id.fabImportClass);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, ImportStudentActivity.class);
            i.putExtra("classId", classId);
            startActivity(i);
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (classId < 0) return;
        ClassRoom cr = classDAO.getById(classId);
        if (cr != null) {
            tvDetailClassName.setText(cr.getName());
            tvDetailYear.setText(cr.getSchoolYear() != null && !cr.getSchoolYear().isEmpty()
                    ? "Năm học: " + cr.getSchoolYear() : "");
        }

        students.clear();
        students.addAll(studentDAO.getByClassId(classId));
        studentAdapter.notifyDataSetChanged();
        tvDetailCount.setText(students.size() + " sinh viên");
        tvNoStudents.setVisibility(students.isEmpty()
                ? android.view.View.VISIBLE : android.view.View.GONE);

        scores.clear();
        scores.addAll(scoreDAO.getByClassId(classId));
        scoreAdapter.notifyDataSetChanged();
        tvNoScores.setVisibility(scores.isEmpty()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}

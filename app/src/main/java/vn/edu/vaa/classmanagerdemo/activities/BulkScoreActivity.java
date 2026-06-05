package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class BulkScoreActivity extends BaseActivity {

    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;
    private List<Student> studentList = new ArrayList<>();
    private int classId;
    private String className, classSubject;
    private EditText edtWQT, edtWGK, edtWCK, edtBulkSemester;
    private RecyclerView recyclerBulk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_score);

        classId = getIntent().getIntExtra("class_id", -1);
        className = getIntent().getStringExtra("class_name");
        classSubject = getIntent().getStringExtra("class_subject");

        Toolbar toolbar = findViewById(R.id.toolbarBulk);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nhập điểm: " + className);
        }

        studentDAO = new StudentDAO(this);
        scoreDAO = new ScoreDAO(this);

        edtWQT = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.edtBulkWQT));
        edtWGK = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.edtBulkWGK));
        edtWCK = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.edtBulkWCK));
        edtBulkSemester = findViewById(R.id.edtBulkSemester);
        recyclerBulk = findViewById(R.id.recyclerBulkScore);

        studentList.addAll(studentDAO.getByClassId(classId));
        recyclerBulk.setLayoutManager(new LinearLayoutManager(this));
        recyclerBulk.setAdapter(new BulkAdapter());

        MaterialButton btnSave = findViewById(R.id.btnSaveAllScores);
        btnSave.setOnClickListener(DebounceClickListener.wrap(v -> saveAll()));
    }

    private void saveAll() {
        int wqt, wgk, wck;
        try {
            String wqtStr = edtWQT.getText().toString().trim();
            wqt = wqtStr.isEmpty() ? 0 : Integer.parseInt(wqtStr);
            wgk = Integer.parseInt(edtWGK.getText().toString().trim());
            wck = Integer.parseInt(edtWCK.getText().toString().trim());
            if (wqt + wgk + wck != 100) {
                Toast.makeText(this, "Tổng tỉ lệ phải bằng 100%", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nhập tỉ lệ hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int saved = 0;
        int count = recyclerBulk.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = recyclerBulk.getChildAt(i);
            if (child == null) continue;
            EditText eQT = child.findViewById(R.id.edtBulkQT);
            EditText eGK = child.findViewById(R.id.edtBulkGK);
            EditText eCK = child.findViewById(R.id.edtBulkCK);
            if (eQT == null || eGK == null || eCK == null) continue;
            String sQT = eQT.getText().toString().trim();
            String sGK = eGK.getText().toString().trim();
            String sCK = eCK.getText().toString().trim();
            if (sGK.isEmpty() || sCK.isEmpty()) continue;
            try {
                float qt = sQT.isEmpty() ? 0f : Float.parseFloat(sQT);
                float gk = Float.parseFloat(sGK);
                float ck = Float.parseFloat(sCK);
                Student student = studentList.get(recyclerBulk.getChildAdapterPosition(child));
                String semesterStr = edtBulkSemester.getText().toString().trim();
                if (semesterStr.isEmpty()) {
                    semesterStr = "HK " + java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                }
                Score score = new Score(student.getId(), classId, qt, wqt, gk, wgk, ck, wck, semesterStr);
                score.setSubject(classSubject);
                scoreDAO.insert(score);
                saved++;
            } catch (NumberFormatException ignored) {}
        }
        Toast.makeText(this, "Đã lưu " + saved + " điểm", Toast.LENGTH_SHORT).show();
        if (saved > 0) NavigationHelper.finishWithSlide(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { NavigationHelper.finishWithSlide(this); return true; }
        return super.onOptionsItemSelected(item);
    }

    class BulkAdapter extends RecyclerView.Adapter<BulkAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_bulk_score, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Student s = studentList.get(pos);
            h.tvInitials.setText(s.getInitials());
            h.tvName.setText(s.getFullName());
            h.tvCode.setText(s.getStudentCode());

            TextWatcher tw = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence cs, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence cs, int a, int b, int c) {
                    try {
                        String qtStr = h.edtQT.getText().toString().trim();
                        float qt = qtStr.isEmpty() ? 0f : Float.parseFloat(qtStr);
                        float gk = Float.parseFloat(h.edtGK.getText().toString().trim());
                        float ck = Float.parseFloat(h.edtCK.getText().toString().trim());
                        
                        String wqtStr = edtWQT.getText().toString().trim();
                        int wqtV = wqtStr.isEmpty() ? 0 : Integer.parseInt(wqtStr);
                        int wgkV = Integer.parseInt(edtWGK.getText().toString().trim());
                        int wckV = Integer.parseInt(edtWCK.getText().toString().trim());
                        if (wqtV + wgkV + wckV != 100) { h.tvPreview.setText("!"); return; }
                        float result = Math.round((qt * wqtV + gk * wgkV + ck * wckV) / 100f * 10f) / 10f;
                        h.tvPreview.setText(String.format(Locale.US, "%.1f", result));
                    } catch (Exception e) { h.tvPreview.setText("--"); }
                }
                @Override public void afterTextChanged(Editable e) {}
            };
            h.edtQT.addTextChangedListener(tw);
            h.edtGK.addTextChangedListener(tw);
            h.edtCK.addTextChangedListener(tw);
        }

        @Override public int getItemCount() { return studentList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvInitials, tvName, tvCode, tvPreview;
            EditText edtQT, edtGK, edtCK;
            VH(View v) {
                super(v);
                tvInitials = v.findViewById(R.id.tvBulkInitials);
                tvName = v.findViewById(R.id.tvBulkName);
                tvCode = v.findViewById(R.id.tvBulkCode);
                tvPreview = v.findViewById(R.id.tvBulkPreview);
                edtQT = v.findViewById(R.id.edtBulkQT);
                edtGK = v.findViewById(R.id.edtBulkGK);
                edtCK = v.findViewById(R.id.edtBulkCK);
            }
        }
    }
}

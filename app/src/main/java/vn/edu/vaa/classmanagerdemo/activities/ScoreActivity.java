package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import vn.edu.vaa.classmanagerdemo.adapters.ScoreAdapter;

public class ScoreActivity extends BaseActivity {

    private ScoreDAO scoreDAO;
    private vn.edu.vaa.classmanagerdemo.database.TemplateDAO templateDAO;
    private RecyclerView recyclerScores;
    private List<Score> scoreList = new ArrayList<>();
    private ScoreAdapter scoreAdapter;
    private int studentId, classId;
    private String studentName, studentCode, classSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        studentId = getIntent().getIntExtra("student_id", -1);
        studentName = getIntent().getStringExtra("student_name");
        studentCode = getIntent().getStringExtra("student_code");
        classId = getIntent().getIntExtra("class_id", -1);
        classSubject = getIntent().getStringExtra("class_subject");

        Toolbar toolbar = findViewById(R.id.toolbarScore);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(studentName);
        }

        TextView tvInfo = findViewById(R.id.tvStudentInfo);
        if (tvInfo != null) tvInfo.setText("Mã: " + studentCode + "  •  Môn: " + classSubject);

        scoreDAO = new ScoreDAO(this);
        templateDAO = new vn.edu.vaa.classmanagerdemo.database.TemplateDAO(this);
        recyclerScores = findViewById(R.id.recyclerScores);

        scoreAdapter = new ScoreAdapter(scoreList, new ScoreAdapter.OnScoreEditListener() {
            @Override
            public void onEdit(Score score, int position) { showEditScoreDialog(score); }
            @Override
            public void onDelete(Score score, int position) { confirmDelete(score); }
        });
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        recyclerScores.setAdapter(scoreAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddScore);
        fab.setOnClickListener(DebounceClickListener.wrap(v -> showAddScoreDialog()));

        loadScores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScores();
    }

    private void loadScores() {
        List<Score> list = scoreDAO.getByStudentAndClass(studentId, classId);
        scoreList.clear();
        scoreList.addAll(list);
        scoreAdapter.notifyDataSetChanged();

        // Tính điểm TB và hiện lên header
        if (!list.isEmpty()) {
            float sum = 0;
            for (Score s : list) sum += s.getScore();
            float avg = sum / list.size();
            TextView tvAvg = findViewById(R.id.tvScoreAverage);
            if (tvAvg != null) tvAvg.setText(String.format(Locale.US, "TB: %.1f", avg));
        }
    }

    private void showAddScoreDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_score, null);
        dialog.setContentView(view);

        EditText edtQT = view.findViewById(R.id.edtScoreQT);
        EditText edtWQT = view.findViewById(R.id.edtWeightQT);
        EditText edtGK = view.findViewById(R.id.edtScoreGK);
        EditText edtWGK = view.findViewById(R.id.edtWeightGK);
        EditText edtCK = view.findViewById(R.id.edtScoreCK);
        EditText edtWCK = view.findViewById(R.id.edtWeightCK);
        EditText edtSemester = view.findViewById(R.id.edtSemester);
        TextView tvPreview = view.findViewById(R.id.tvScorePreview);
        android.view.View btnUseTemplate = view.findViewById(R.id.btnUseTemplate);
        if (btnUseTemplate != null) {
            btnUseTemplate.setOnClickListener(v -> showTemplateSelector(edtWQT, edtWGK, edtWCK));
        }

        edtWQT.setText("20"); edtWGK.setText("30"); edtWCK.setText("50");
        edtSemester.setText("HK1 2024-2025");

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { updatePreview(edtQT, edtWQT, edtGK, edtWGK, edtCK, edtWCK, tvPreview); }
            @Override public void afterTextChanged(Editable s) {}
        };
        edtQT.addTextChangedListener(watcher);
        edtGK.addTextChangedListener(watcher);
        edtCK.addTextChangedListener(watcher);

        view.findViewById(R.id.btnSaveScore).setOnClickListener(DebounceClickListener.wrap(v -> {
            try {
                float qt = Float.parseFloat(edtQT.getText().toString().trim());
                int wqt = Integer.parseInt(edtWQT.getText().toString().trim());
                float gk = Float.parseFloat(edtGK.getText().toString().trim());
                int wgk = Integer.parseInt(edtWGK.getText().toString().trim());
                float ck = Float.parseFloat(edtCK.getText().toString().trim());
                int wck = Integer.parseInt(edtWCK.getText().toString().trim());
                String semester = edtSemester.getText().toString().trim();

                if (wqt + wgk + wck != 100) {
                    Toast.makeText(this, "Tổng tỉ lệ phải bằng 100%", Toast.LENGTH_SHORT).show(); return;
                }
                Score score = new Score(studentId, classId, qt, wqt, gk, wgk, ck, wck, semester);
                score.setSubject(classSubject);
                scoreDAO.insert(score);
                loadScores();
                dialog.dismiss();
                Toast.makeText(this, "Đã lưu điểm", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
    }

    private void showEditScoreDialog(Score score) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_score, null);
        dialog.setContentView(view);

        EditText edtQT = view.findViewById(R.id.edtScoreQT);
        EditText edtWQT = view.findViewById(R.id.edtWeightQT);
        EditText edtGK = view.findViewById(R.id.edtScoreGK);
        EditText edtWGK = view.findViewById(R.id.edtWeightGK);
        EditText edtCK = view.findViewById(R.id.edtScoreCK);
        EditText edtWCK = view.findViewById(R.id.edtWeightCK);
        EditText edtSemester = view.findViewById(R.id.edtSemester);
        TextView tvPreview = view.findViewById(R.id.tvScorePreview);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) tvTitle.setText("Chỉnh sửa điểm");
        android.view.View btnUseTemplate = view.findViewById(R.id.btnUseTemplate);
        if (btnUseTemplate != null) {
            btnUseTemplate.setOnClickListener(v -> showTemplateSelector(edtWQT, edtWGK, edtWCK));
        }

        edtQT.setText(String.valueOf(score.getScoreQT()));
        edtWQT.setText(String.valueOf(score.getWeightQT()));
        edtGK.setText(String.valueOf(score.getScoreGK()));
        edtWGK.setText(String.valueOf(score.getWeightGK()));
        edtCK.setText(String.valueOf(score.getScoreCK()));
        edtWCK.setText(String.valueOf(score.getWeightCK()));
        edtSemester.setText(score.getSemester());

        updatePreview(edtQT, edtWQT, edtGK, edtWGK, edtCK, edtWCK, tvPreview);

        view.findViewById(R.id.btnSaveScore).setOnClickListener(DebounceClickListener.wrap(v -> {
            try {
                score.setScoreQT(Float.parseFloat(edtQT.getText().toString().trim()));
                score.setWeightQT(Integer.parseInt(edtWQT.getText().toString().trim()));
                score.setScoreGK(Float.parseFloat(edtGK.getText().toString().trim()));
                score.setWeightGK(Integer.parseInt(edtWGK.getText().toString().trim()));
                score.setScoreCK(Float.parseFloat(edtCK.getText().toString().trim()));
                score.setWeightCK(Integer.parseInt(edtWCK.getText().toString().trim()));
                score.setSemester(edtSemester.getText().toString().trim());
                scoreDAO.update(score);
                loadScores();
                dialog.dismiss();
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
    }

    private void updatePreview(EditText edtQT, EditText edtWQT, EditText edtGK, EditText edtWGK,
                               EditText edtCK, EditText edtWCK, TextView tvPreview) {
        try {
            float qt = Float.parseFloat(edtQT.getText().toString().trim());
            int wqt = Integer.parseInt(edtWQT.getText().toString().trim());
            float gk = Float.parseFloat(edtGK.getText().toString().trim());
            int wgk = Integer.parseInt(edtWGK.getText().toString().trim());
            float ck = Float.parseFloat(edtCK.getText().toString().trim());
            int wck = Integer.parseInt(edtWCK.getText().toString().trim());
            if (wqt + wgk + wck != 100) { tvPreview.setText("Tổng tỉ lệ ≠ 100"); return; }
            float result = Math.round((qt * wqt + gk * wgk + ck * wck) / 100f * 10f) / 10f;
            tvPreview.setText(String.format(Locale.US, "%.1f", result));
        } catch (NumberFormatException e) {
            tvPreview.setText("--");
        }
    }

    private void confirmDelete(Score score) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa điểm")
            .setMessage("Xóa điểm học kỳ " + score.getSemester() + "?")
            .setPositiveButton("Xóa", (d, w) -> {
                scoreDAO.deleteById(score.getId());
                loadScores();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { NavigationHelper.finishWithSlide(this); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showTemplateSelector(EditText edtWQT, EditText edtWGK, EditText edtWCK) {
        int teacherId = new vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager(this).getCurrentUserId();
        List<vn.edu.vaa.classmanagerdemo.models.ScoreTemplate> templates = templateDAO.getByTeacher(teacherId);
        
        List<String> items = new ArrayList<>();
        for (vn.edu.vaa.classmanagerdemo.models.ScoreTemplate t : templates) {
            items.add(t.getTemplateName() + " (" + t.getWeightQt() + ":" + t.getWeightGk() + ":" + t.getWeightCk() + ")");
        }
        items.add("+ Lưu tỉ lệ hiện tại làm template mới");
        
        new AlertDialog.Builder(this)
            .setTitle("Template tỉ lệ điểm")
            .setItems(items.toArray(new String[0]), (dialog, which) -> {
                if (which == templates.size()) {
                    showSaveTemplateDialog(teacherId, edtWQT, edtWGK, edtWCK);
                } else {
                    vn.edu.vaa.classmanagerdemo.models.ScoreTemplate selected = templates.get(which);
                    edtWQT.setText(String.valueOf(selected.getWeightQt()));
                    edtWGK.setText(String.valueOf(selected.getWeightGk()));
                    edtWCK.setText(String.valueOf(selected.getWeightCk()));
                    Toast.makeText(this, "Đã áp dụng template: " + selected.getTemplateName(), Toast.LENGTH_SHORT).show();
                }
            })
            .setNeutralButton("Xóa bớt template", (dialog, which) -> {
                showDeleteTemplatesDialog(templates);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showSaveTemplateDialog(int teacherId, EditText edtWQT, EditText edtWGK, EditText edtWCK) {
        try {
            int wqt = Integer.parseInt(edtWQT.getText().toString().trim());
            int wgk = Integer.parseInt(edtWGK.getText().toString().trim());
            int wck = Integer.parseInt(edtWCK.getText().toString().trim());
            if (wqt + wgk + wck != 100) {
                Toast.makeText(this, "Tổng tỉ lệ hiện tại phải bằng 100% để lưu", Toast.LENGTH_SHORT).show();
                return;
            }
            EditText input = new EditText(this);
            input.setHint("Tên template (VD: Lý thuyết, Thực hành)");
            new AlertDialog.Builder(this)
                .setTitle("Lưu template")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Mẫu " + wqt + "-" + wgk + "-" + wck;
                    vn.edu.vaa.classmanagerdemo.models.ScoreTemplate t = 
                        new vn.edu.vaa.classmanagerdemo.models.ScoreTemplate(0, teacherId, name, wqt, wgk, wck);
                    templateDAO.insert(t);
                    Toast.makeText(this, "Đã lưu template thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đủ tỉ lệ QT, GK, CK hợp lệ trước khi lưu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteTemplatesDialog(List<vn.edu.vaa.classmanagerdemo.models.ScoreTemplate> templates) {
        if (templates.isEmpty()) {
            Toast.makeText(this, "Không có template nào để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> items = new ArrayList<>();
        for (vn.edu.vaa.classmanagerdemo.models.ScoreTemplate t : templates) {
            items.add(t.getTemplateName());
        }
        new AlertDialog.Builder(this)
            .setTitle("Chọn template cần xóa")
            .setItems(items.toArray(new String[0]), (dialog, which) -> {
                vn.edu.vaa.classmanagerdemo.models.ScoreTemplate selected = templates.get(which);
                templateDAO.delete(selected.getId());
                Toast.makeText(this, "Đã xóa template", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}

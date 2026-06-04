package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ScoreAdapter;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class GradeActivity extends AppCompatActivity {

    private ScoreDAO scoreDAO;
    private AppPreferenceManager prefs;

    private TextView tvAverage, tvScoreCount;
    private Spinner spFilterSemester;
    private RecyclerView recyclerScores;

    private final List<Score> fullScoreList = new ArrayList<>();
    private final List<Score> scoreList = new ArrayList<>();
    private ScoreAdapter scoreAdapter;
    private int currentStudentId = -1;
    private String selectedFilterSemester = "Tất cả học kỳ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_grade);

        Toolbar toolbar = findViewById(R.id.toolbarGrade);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        NavigationHelper.setupBottomNavigation(this, R.id.nav_grades);

        scoreDAO = new ScoreDAO(this);
        currentStudentId = prefs.getCurrentUserId();

        initViews();
        
        scoreAdapter = new ScoreAdapter(scoreList, (score, position) -> confirmDeleteScore(score, position));
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        recyclerScores.setAdapter(scoreAdapter);
 
        loadScoresForStudent(currentStudentId);
        setupFilterSpinnerListener();
 
        findViewById(R.id.fabAddScore).setOnClickListener(v -> showAddScoreDialog());
    }

    private void initViews() {
        tvAverage        = findViewById(R.id.tvScoreAverage);
        tvScoreCount     = findViewById(R.id.tvScoreCount);
        spFilterSemester = findViewById(R.id.spFilterSemester);
        recyclerScores   = findViewById(R.id.recyclerScores);
    }

    private void loadScoresForStudent(int studentId) {
        fullScoreList.clear();
        fullScoreList.addAll(scoreDAO.getByStudentId(studentId));
        
        // Rebuild semester list for filter
        List<String> semestersList = new ArrayList<>();
        semestersList.add("Tất cả học kỳ");
        for (Score s : fullScoreList) {
            String sem = s.getSemester();
            if (sem != null && !sem.trim().isEmpty() && !semestersList.contains(sem)) {
                semestersList.add(sem);
            }
        }
        
        // Sort semester list excluding "Tất cả học kỳ"
        if (semestersList.size() > 2) {
            List<String> subList = semestersList.subList(1, semestersList.size());
            Collections.sort(subList);
        }

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semestersList);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterSemester.setAdapter(filterAdapter);

        // Restore previous selection if possible
        int prevPos = semestersList.indexOf(selectedFilterSemester);
        if (prevPos >= 0) {
            spFilterSemester.setSelection(prevPos);
        } else {
            spFilterSemester.setSelection(0);
            selectedFilterSemester = "Tất cả học kỳ";
        }

        applySemesterFilter();
    }

    private void setupFilterSpinnerListener() {
        spFilterSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilterSemester = parent.getItemAtPosition(position).toString();
                applySemesterFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void applySemesterFilter() {
        scoreList.clear();
        if (selectedFilterSemester.equals("Tất cả học kỳ")) {
            scoreList.addAll(fullScoreList);
        } else {
            for (Score s : fullScoreList) {
                if (selectedFilterSemester.equals(s.getSemester())) {
                    scoreList.add(s);
                }
            }
        }
        scoreAdapter.notifyDataSetChanged();
        updateStats();
    }

    private void updateStats() {
        int totalCredits = 0;
        float weightedGpaSum = 0;
        for (Score s : scoreList) {
            totalCredits += s.getCredits();
            weightedGpaSum += s.getGrade4() * s.getCredits();
        }
        float avgGpa = totalCredits > 0 ? (weightedGpaSum / totalCredits) : 0f;

        String labelPrefix = selectedFilterSemester.equals("Tất cả học kỳ") ? "GPA tích lũy" : "GPA Học kỳ";
        tvScoreCount.setText(scoreList.size() + " môn (" + totalCredits + " tín chỉ)");
        if (scoreList.isEmpty()) {
            tvAverage.setText(labelPrefix + ": --");
        } else {
            tvAverage.setText(String.format(Locale.US, labelPrefix + ": %.2f", avgGpa));
        }
    }

    private void showAddScoreDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_grade, null);
        dialog.setContentView(view);

        AutoCompleteTextView dialogActvSemester = view.findViewById(R.id.actvSemester);
        EditText dialogEdtSubject = view.findViewById(R.id.edtSubject);
        EditText dialogEdtCredits = view.findViewById(R.id.edtCredits);
        EditText dialogEdtScoreQT = view.findViewById(R.id.edtScoreQT);
        EditText dialogEdtWeightQT = view.findViewById(R.id.edtWeightQT);
        EditText dialogEdtScoreCK = view.findViewById(R.id.edtScoreCK);
        EditText dialogEdtWeightCK = view.findViewById(R.id.edtWeightCK);

        // Set up semesters dropdown
        String[] semesters = {
                "HK1 2024-2025", "HK2 2024-2025", "HK3 2024-2025",
                "HK1 2025-2026", "HK2 2025-2026", "HK3 2025-2026"
        };
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, semesters);
        dialogActvSemester.setAdapter(semAdapter);

        // Default to selected semester filter if not "Tất cả học kỳ"
        String defaultSemester = semesters[0];
        if (selectedFilterSemester != null && !selectedFilterSemester.equals("Tất cả học kỳ")) {
            for (String sem : semesters) {
                if (sem.equals(selectedFilterSemester)) {
                    defaultSemester = sem;
                    break;
                }
            }
        }
        dialogActvSemester.setText(defaultSemester, false);

        // Set up weights watchers
        dialogEdtWeightQT.addTextChangedListener(new TextWatcher() {
            private boolean isBusy = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isBusy) return;
                isBusy = true;
                try {
                    int val = Integer.parseInt(s.toString().trim());
                    if (val >= 0 && val <= 100) {
                        dialogEdtWeightCK.setText(String.valueOf(100 - val));
                    }
                } catch (NumberFormatException e) {
                    dialogEdtWeightCK.setText("");
                }
                isBusy = false;
            }
        });

        dialogEdtWeightCK.addTextChangedListener(new TextWatcher() {
            private boolean isBusy = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (isBusy) return;
                isBusy = true;
                try {
                    int val = Integer.parseInt(s.toString().trim());
                    if (val >= 0 && val <= 100) {
                        dialogEdtWeightQT.setText(String.valueOf(100 - val));
                    }
                } catch (NumberFormatException e) {
                    dialogEdtWeightQT.setText("");
                }
                isBusy = false;
            }
        });

        view.findViewById(R.id.btnSaveScore).setOnClickListener(
            DebounceClickListener.wrap(v -> {
                if (currentStudentId <= 0) {
                    Toast.makeText(this, "Sinh viên không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                String subject = dialogEdtSubject.getText().toString().trim();
                String creditsStr = dialogEdtCredits.getText().toString().trim();
                String scoreQTStr = dialogEdtScoreQT.getText().toString().trim();
                String weightQTStr = dialogEdtWeightQT.getText().toString().trim();
                String scoreCKStr = dialogEdtScoreCK.getText().toString().trim();
                String weightCKStr = dialogEdtWeightCK.getText().toString().trim();

                if (subject.isEmpty()) { dialogEdtSubject.setError("Nhập tên môn học"); return; }
                if (creditsStr.isEmpty()) { dialogEdtCredits.setError("Nhập số tín chỉ"); return; }
                if (scoreQTStr.isEmpty()) { dialogEdtScoreQT.setError("Nhập điểm QT"); return; }
                if (weightQTStr.isEmpty()) { dialogEdtWeightQT.setError("Nhập tỷ lệ QT"); return; }
                if (scoreCKStr.isEmpty()) { dialogEdtScoreCK.setError("Nhập điểm CK"); return; }
                if (weightCKStr.isEmpty()) { dialogEdtWeightCK.setError("Nhập tỷ lệ CK"); return; }

                try {
                    int credits = Integer.parseInt(creditsStr);
                    float scoreQT = Float.parseFloat(scoreQTStr);
                    int weightQT = Integer.parseInt(weightQTStr);
                    float scoreCK = Float.parseFloat(scoreCKStr);
                    int weightCK = Integer.parseInt(weightCKStr);

                    if (credits <= 0) { dialogEdtCredits.setError("Số tín chỉ phải > 0"); return; }
                    if (scoreQT < 0 || scoreQT > 10) { dialogEdtScoreQT.setError("Điểm từ 0 đến 10"); return; }
                    if (scoreCK < 0 || scoreCK > 10) { dialogEdtScoreCK.setError("Điểm từ 0 đến 10"); return; }
                    if (weightQT + weightCK != 100) {
                        Toast.makeText(this, "Tổng tỷ lệ phần trăm phải bằng 100%", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String semester = dialogActvSemester.getText() != null ? dialogActvSemester.getText().toString() : "";
                    Score score = new Score(currentStudentId, subject, credits, scoreQT, weightQT, scoreCK, weightCK, semester);
                    scoreDAO.insert(score);

                    // Save the semester added as selected filter
                    selectedFilterSemester = semester;
                    loadScoresForStudent(currentStudentId);
                    Toast.makeText(this, "Đã lưu điểm môn " + subject, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Dữ liệu nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            })
        );

        dialog.show();
    }

    private void confirmDeleteScore(Score score, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa môn học")
                .setMessage("Bạn có chắc chắn muốn xóa môn học \"" + score.getSubject() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    scoreDAO.deleteById(score.getId());
                    loadScoresForStudent(currentStudentId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavigationHelper.finishWithSlide(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ScoreAdapter;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

public class GradeActivity extends BaseActivity {

    private ScoreDAO scoreDAO;
    private AppPreferenceManager prefs;

    private TextView tvAverage, tvScoreCount;
    private AutoCompleteTextView spFilterSemester;
    private TextView tvSemesterRank;
    private com.google.android.material.button.MaterialButton btnSimulate;
    private com.google.android.material.button.MaterialButton btnShareGpa;
    private RecyclerView recyclerScores;

    // Tab view components
    private TabLayout tabLayout;
    private View layoutTabSubjects, layoutTabSemesterStats;
    private TextView tvCumulativeGpa10, tvCumulativeGpa4, tvCumulativeCredits, tvCumulativeClassification;
    private RecyclerView recyclerSemesterSummaries;
    private SemesterSummaryAdapter semesterSummaryAdapter;
    private final List<SemesterSummary> semesterSummaryList = new ArrayList<>();

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
        
        scoreAdapter = new ScoreAdapter(scoreList, new ScoreAdapter.OnScoreEditListener() {
            @Override
            public void onEdit(Score score, int position) {
                showEditScoreDialog(score);
            }
            @Override
            public void onDelete(Score score, int position) {
                confirmDeleteScore(score, position);
            }
        });
        recyclerScores.setLayoutManager(new LinearLayoutManager(this));
        recyclerScores.setAdapter(scoreAdapter);

        // Setup Semester Summaries RecyclerView
        semesterSummaryAdapter = new SemesterSummaryAdapter(semesterSummaryList);
        recyclerSemesterSummaries.setLayoutManager(new LinearLayoutManager(this));
        recyclerSemesterSummaries.setAdapter(semesterSummaryAdapter);
 
        loadScoresForStudent(currentStudentId);
        setupFilterSpinnerListener();
        setupTabLayout();
 
        findViewById(R.id.fabAddScore).setOnClickListener(v -> showAddScoreDialog());

        btnSimulate = findViewById(R.id.btnSimulate);
        btnSimulate.setOnClickListener(v -> showSimulateDialog());

        btnShareGpa = findViewById(R.id.btnShareGpa);
        btnShareGpa.setOnClickListener(v -> handleShareGpa());
    }

    private void initViews() {
        tvAverage        = findViewById(R.id.tvScoreAverage);
        tvScoreCount     = findViewById(R.id.tvScoreCount);
        spFilterSemester = findViewById(R.id.spFilterSemester);
        tvSemesterRank   = findViewById(R.id.tvSemesterRank);
        recyclerScores   = findViewById(R.id.recyclerScores);

        tabLayout        = findViewById(R.id.tabLayout);
        layoutTabSubjects = findViewById(R.id.layoutTabSubjects);
        layoutTabSemesterStats = findViewById(R.id.layoutTabSemesterStats);

        tvCumulativeGpa10 = findViewById(R.id.tvCumulativeGpa10);
        tvCumulativeGpa4 = findViewById(R.id.tvCumulativeGpa4);
        tvCumulativeCredits = findViewById(R.id.tvCumulativeCredits);
        tvCumulativeClassification = findViewById(R.id.tvCumulativeClassification);
        recyclerSemesterSummaries = findViewById(R.id.recyclerSemesterSummaries);
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
                android.R.layout.simple_dropdown_item_1line, semestersList);
        spFilterSemester.setAdapter(filterAdapter);
        if (semestersList.contains(selectedFilterSemester)) {
            spFilterSemester.setText(selectedFilterSemester, false);
        } else {
            spFilterSemester.setText("Tất cả học kỳ", false);
            selectedFilterSemester = "Tất cả học kỳ";
        }

        applySemesterFilter();
        calculateAndShowSemesterStats();
    }

    private void setupFilterSpinnerListener() {
        spFilterSemester.setOnItemClickListener((parent, view, position, id) -> {
            selectedFilterSemester = parent.getItemAtPosition(position).toString();
            applySemesterFilter();
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

        // Xếp loại học kỳ
        if (tvSemesterRank != null) {
            if (scoreList.isEmpty() || totalCredits == 0) {
                tvSemesterRank.setVisibility(android.view.View.GONE);
            } else {
                String rank; String color; String emoji;
                if (avgGpa >= 3.6f) { rank = "Xuất sắc"; color = "#10B981"; emoji = "🏆"; }
                else if (avgGpa >= 3.2f) { rank = "Giỏi"; color = "#3B82F6"; emoji = "🏅"; }
                else if (avgGpa >= 2.5f) { rank = "Khá"; color = "#6366F1"; emoji = "⭐"; }
                else if (avgGpa >= 2.0f) { rank = "Trung bình"; color = "#F59E0B"; emoji = "📚"; }
                else { rank = "Yếu — cần cải thiện"; color = "#EF4444"; emoji = "⚠️"; }
                String label = selectedFilterSemester.equals("Tất cả học kỳ")
                        ? "Học lực tích lũy" : "Học lực " + selectedFilterSemester;
                tvSemesterRank.setText(emoji + "  " + label + ": " + rank
                        + "  (GPA " + String.format(java.util.Locale.US, "%.2f", avgGpa) + ")");
                tvSemesterRank.setTextColor(android.graphics.Color.parseColor(color));
                tvSemesterRank.setVisibility(android.view.View.VISIBLE);
            }
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

        // Suggest already used semesters
        List<String> suggestions = new ArrayList<>();
        for (Score s : fullScoreList) {
            String sem = s.getSemester();
            if (sem != null && !sem.trim().isEmpty() && !suggestions.contains(sem)) {
                suggestions.add(sem);
            }
        }
        // Fallback options if suggestion list is empty
        if (suggestions.isEmpty()) {
            suggestions.add("HK1 2024-2025");
            suggestions.add("HK2 2024-2025");
            suggestions.add("HK3 2024-2025");
            suggestions.add("HK1 2025-2026");
            suggestions.add("HK2 2025-2026");
        }
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        dialogActvSemester.setAdapter(semAdapter);

        // Default to selected semester filter if not "Tất cả học kỳ"
        String defaultSemester = "HK1 2024-2025";
        if (selectedFilterSemester != null && !selectedFilterSemester.equals("Tất cả học kỳ")) {
            defaultSemester = selectedFilterSemester;
        } else if (!suggestions.isEmpty()) {
            defaultSemester = suggestions.get(0);
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

        android.widget.TextView tvPreviewScore = view.findViewById(R.id.tvPreviewScore);
        android.widget.TextView tvPreviewGrade = view.findViewById(R.id.tvPreviewGrade);

        android.text.TextWatcher previewWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateScorePreview(tvPreviewScore, tvPreviewGrade,
                        dialogEdtScoreQT, dialogEdtWeightQT, dialogEdtScoreCK, dialogEdtWeightCK);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        dialogEdtScoreQT.addTextChangedListener(previewWatcher);
        dialogEdtScoreCK.addTextChangedListener(previewWatcher);
        dialogEdtWeightQT.addTextChangedListener(previewWatcher);
        dialogEdtWeightCK.addTextChangedListener(previewWatcher);

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
                    if (scoreQT < 0 || scoreQT > 10) { dialogEdtScoreQT.setError(getString(R.string.error_score_range)); return; }
                    if (scoreCK < 0 || scoreCK > 10) { dialogEdtScoreCK.setError(getString(R.string.error_score_range)); return; }
                    if (weightQT + weightCK != 100) {
                        Toast.makeText(this, "Tổng tỷ lệ phần trăm phải bằng 100%", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String semester = dialogActvSemester.getText() != null ? dialogActvSemester.getText().toString().trim() : "";
                    if (semester.isEmpty()) {
                        dialogActvSemester.setError("Vui lòng nhập học kỳ");
                        return;
                    }
                    Score score = new Score(currentStudentId, subject, credits, scoreQT, weightQT, scoreCK, weightCK, semester);
                    scoreDAO.insert(score);

                    // Save the semester added as selected filter
                    selectedFilterSemester = semester;
                    loadScoresForStudent(currentStudentId);
                    Toast.makeText(this, getString(R.string.msg_saved_course, subject), Toast.LENGTH_SHORT).show();
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
                .setNegativeButton(getString(R.string.btn_cancel), null)
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

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Môn học"));
        tabLayout.addTab(tabLayout.newTab().setText("Tổng kết"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutTabSubjects.setVisibility(View.VISIBLE);
                    layoutTabSemesterStats.setVisibility(View.GONE);
                    findViewById(R.id.fabAddScore).setVisibility(View.VISIBLE);
                } else {
                    layoutTabSubjects.setVisibility(View.GONE);
                    layoutTabSemesterStats.setVisibility(View.VISIBLE);
                    findViewById(R.id.fabAddScore).setVisibility(View.GONE);
                    calculateAndShowSemesterStats();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void calculateAndShowSemesterStats() {
        semesterSummaryList.clear();
        if (fullScoreList.isEmpty()) {
            tvCumulativeGpa10.setText("--");
            tvCumulativeGpa4.setText("--");
            tvCumulativeCredits.setText("0 TC");
            tvCumulativeClassification.setText("Chưa xếp loại");
            tvCumulativeClassification.setTextColor(getResources().getColor(R.color.text_secondary));
            semesterSummaryAdapter.notifyDataSetChanged();
            return;
        }

        // Group scores by semester using TreeMap for natural sort
        Map<String, List<Score>> groups = new TreeMap<>();
        int totalCumulativeCredits = 0;
        float weightedCumulativeGpa10 = 0f;
        float weightedCumulativeGpa4 = 0f;

        for (Score score : fullScoreList) {
            String sem = score.getSemester();
            if (sem == null || sem.trim().isEmpty()) {
                sem = "Chưa phân loại";
            }
            if (!groups.containsKey(sem)) {
                groups.put(sem, new ArrayList<>());
            }
            groups.get(sem).add(score);

            totalCumulativeCredits += score.getCredits();
            weightedCumulativeGpa10 += score.getScore() * score.getCredits();
            weightedCumulativeGpa4 += score.getGrade4() * score.getCredits();
        }

        // Calculate semester stats
        for (Map.Entry<String, List<Score>> entry : groups.entrySet()) {
            String semName = entry.getKey();
            List<Score> scores = entry.getValue();

            int semCredits = 0;
            float semWeightedGpa10 = 0f;
            float semWeightedGpa4 = 0f;

            for (Score s : scores) {
                semCredits += s.getCredits();
                semWeightedGpa10 += s.getScore() * s.getCredits();
                semWeightedGpa4 += s.getGrade4() * s.getCredits();
            }

            float semGpa10 = semCredits > 0 ? (semWeightedGpa10 / semCredits) : 0f;
            float semGpa4 = semCredits > 0 ? (semWeightedGpa4 / semCredits) : 0f;

            semesterSummaryList.add(new SemesterSummary(semName, semGpa10, semGpa4, semCredits, scores.size()));
        }

        // Overall stats
        float overallGpa10 = totalCumulativeCredits > 0 ? (weightedCumulativeGpa10 / totalCumulativeCredits) : 0f;
        float overallGpa4 = totalCumulativeCredits > 0 ? (weightedCumulativeGpa4 / totalCumulativeCredits) : 0f;

        tvCumulativeGpa10.setText(String.format(Locale.US, "%.2f", overallGpa10));
        tvCumulativeGpa4.setText(String.format(Locale.US, "%.2f", overallGpa4));
        tvCumulativeCredits.setText(totalCumulativeCredits + " TC");

        // Determine cumulative classification
        String classification;
        int colorRes;
        if (overallGpa4 >= 3.6f) {
            classification = "Xuất sắc";
            colorRes = R.color.grade_gioi;
        } else if (overallGpa4 >= 3.2f) {
            classification = "Giỏi";
            colorRes = R.color.grade_gioi;
        } else if (overallGpa4 >= 2.5f) {
            classification = "Khá";
            colorRes = R.color.grade_kha;
        } else if (overallGpa4 >= 2.0f) {
            classification = "Trung bình";
            colorRes = R.color.grade_tb;
        } else if (overallGpa4 >= 1.0f) {
            classification = "Yếu";
            colorRes = R.color.grade_yeu;
        } else {
            classification = "Kém";
            colorRes = R.color.grade_kem;
        }
        tvCumulativeClassification.setText(classification);
        tvCumulativeClassification.setTextColor(getResources().getColor(colorRes));

        semesterSummaryAdapter.notifyDataSetChanged();
    }

    private static class SemesterSummary {
        private final String semesterName;
        private final float gpa10;
        private final float gpa4;
        private final int totalCredits;
        private final int subjectCount;

        public SemesterSummary(String semesterName, float gpa10, float gpa4, int totalCredits, int subjectCount) {
            this.semesterName = semesterName;
            this.gpa10 = gpa10;
            this.gpa4 = gpa4;
            this.totalCredits = totalCredits;
            this.subjectCount = subjectCount;
        }

        public String getSemesterName() { return semesterName; }
        public float getGpa10() { return gpa10; }
        public float getGpa4() { return gpa4; }
        public int getTotalCredits() { return totalCredits; }
        public int getSubjectCount() { return subjectCount; }
    }

    private static class SemesterSummaryAdapter extends RecyclerView.Adapter<SemesterSummaryAdapter.ViewHolder> {
        private final List<SemesterSummary> summaries;

        public SemesterSummaryAdapter(List<SemesterSummary> summaries) {
            this.summaries = summaries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SemesterSummary summary = summaries.get(position);
            holder.tvSemName.setText(summary.getSemesterName());
            holder.tvSemGpa10.setText(String.format(Locale.US, "%.2f", summary.getGpa10()));
            holder.tvSemGpa4.setText(String.format(Locale.US, "%.2f", summary.getGpa4()));
            holder.tvSemCredits.setText(summary.getTotalCredits() + " TC (" + summary.getSubjectCount() + " môn)");

            // Determine classification
            float gpa4 = summary.getGpa4();
            String classification;
            int colorRes;
            if (gpa4 >= 3.6f) {
                classification = "Xuất sắc";
                colorRes = R.color.grade_gioi;
            } else if (gpa4 >= 3.2f) {
                classification = "Giỏi";
                colorRes = R.color.grade_gioi;
            } else if (gpa4 >= 2.5f) {
                classification = "Khá";
                colorRes = R.color.grade_kha;
            } else if (gpa4 >= 2.0f) {
                classification = "Trung bình";
                colorRes = R.color.grade_tb;
            } else if (gpa4 >= 1.0f) {
                classification = "Yếu";
                colorRes = R.color.grade_yeu;
            } else {
                classification = "Kém";
                colorRes = R.color.grade_kem;
            }
            holder.tvSemClassification.setText(classification);
            holder.tvSemClassification.setTextColor(holder.itemView.getContext().getResources().getColor(colorRes));
        }

        @Override
        public int getItemCount() {
            return summaries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSemName, tvSemClassification, tvSemGpa10, tvSemGpa4, tvSemCredits;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSemName = itemView.findViewById(R.id.tvSemName);
                tvSemClassification = itemView.findViewById(R.id.tvSemClassification);
                tvSemGpa10 = itemView.findViewById(R.id.tvSemGpa10);
                tvSemGpa4 = itemView.findViewById(R.id.tvSemGpa4);
                tvSemCredits = itemView.findViewById(R.id.tvSemCredits);
            }
        }
    }

    private void showEditScoreDialog(Score score) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_grade, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) {
            tvTitle.setText("Chỉnh sửa điểm môn học");
        }

        AutoCompleteTextView dialogActvSemester = view.findViewById(R.id.actvSemester);
        EditText dialogEdtSubject = view.findViewById(R.id.edtSubject);
        EditText dialogEdtCredits = view.findViewById(R.id.edtCredits);
        EditText dialogEdtScoreQT = view.findViewById(R.id.edtScoreQT);
        EditText dialogEdtWeightQT = view.findViewById(R.id.edtWeightQT);
        EditText dialogEdtScoreCK = view.findViewById(R.id.edtScoreCK);
        EditText dialogEdtWeightCK = view.findViewById(R.id.edtWeightCK);

        // Prepopulate fields
        dialogActvSemester.setText(score.getSemester(), false);
        dialogEdtSubject.setText(score.getSubject());
        dialogEdtCredits.setText(String.valueOf(score.getCredits()));
        dialogEdtScoreQT.setText(String.valueOf(score.getScoreQT()));
        dialogEdtWeightQT.setText(String.valueOf(score.getWeightQT()));
        dialogEdtScoreCK.setText(String.valueOf(score.getScoreCK()));
        dialogEdtWeightCK.setText(String.valueOf(score.getWeightCK()));

        // Suggest already used semesters
        List<String> suggestions = new ArrayList<>();
        for (Score s : fullScoreList) {
            String sem = s.getSemester();
            if (sem != null && !sem.trim().isEmpty() && !suggestions.contains(sem)) {
                suggestions.add(sem);
            }
        }
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        dialogActvSemester.setAdapter(semAdapter);

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

        android.widget.TextView tvPreviewScore = view.findViewById(R.id.tvPreviewScore);
        android.widget.TextView tvPreviewGrade = view.findViewById(R.id.tvPreviewGrade);

        android.text.TextWatcher previewWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateScorePreview(tvPreviewScore, tvPreviewGrade,
                        dialogEdtScoreQT, dialogEdtWeightQT, dialogEdtScoreCK, dialogEdtWeightCK);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        dialogEdtScoreQT.addTextChangedListener(previewWatcher);
        dialogEdtScoreCK.addTextChangedListener(previewWatcher);
        dialogEdtWeightQT.addTextChangedListener(previewWatcher);
        dialogEdtWeightCK.addTextChangedListener(previewWatcher);

        // Initial preview calculation
        updateScorePreview(tvPreviewScore, tvPreviewGrade,
                dialogEdtScoreQT, dialogEdtWeightQT, dialogEdtScoreCK, dialogEdtWeightCK);

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
                    if (scoreQT < 0 || scoreQT > 10) { dialogEdtScoreQT.setError(getString(R.string.error_score_range)); return; }
                    if (scoreCK < 0 || scoreCK > 10) { dialogEdtScoreCK.setError(getString(R.string.error_score_range)); return; }
                    if (weightQT + weightCK != 100) {
                        Toast.makeText(this, "Tổng tỷ lệ phần trăm phải bằng 100%", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String semester = dialogActvSemester.getText() != null ? dialogActvSemester.getText().toString().trim() : "";
                    if (semester.isEmpty()) {
                        dialogActvSemester.setError("Vui lòng nhập học kỳ");
                        return;
                    }

                    score.setSubject(subject);
                    score.setCredits(credits);
                    score.setScoreQT(scoreQT);
                    score.setWeightQT(weightQT);
                    score.setScoreCK(scoreCK);
                    score.setWeightCK(weightCK);
                    score.setSemester(semester);

                    scoreDAO.update(score);

                    selectedFilterSemester = semester;
                    loadScoresForStudent(currentStudentId);
                    Toast.makeText(this, getString(R.string.msg_updated_course, subject), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Dữ liệu nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            })
        );

        dialog.show();
    }

    private void showSimulateDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_simulate_gpa, null);
        dialog.setContentView(view);

        TextView tvSimCurrentGpa = view.findViewById(R.id.tvSimCurrentGpa);
        TextView tvSimProjectedGpa = view.findViewById(R.id.tvSimProjectedGpa);
        TextView tvSimTrendIcon = view.findViewById(R.id.tvSimTrendIcon);
        TextView tvSimTrendText = view.findViewById(R.id.tvSimTrendText);
        TextView tvSimProjectedCredits = view.findViewById(R.id.tvSimProjectedCredits);

        EditText edtSimSubjectName = view.findViewById(R.id.edtSimSubjectName);
        EditText edtSimCredits = view.findViewById(R.id.edtSimCredits);
        EditText edtSimScore = view.findViewById(R.id.edtSimScore);

        LinearLayout containerSimulatedCourses = view.findViewById(R.id.containerSimulatedCourses);

        List<vn.edu.vaa.classmanagerdemo.models.SimulatedCourse> simulatedList = new ArrayList<>();

        // Calculate current real GPA values
        int tempRealCredits = 0;
        float tempRealWeightedGpa4 = 0f;
        for (Score s : fullScoreList) {
            tempRealCredits += s.getCredits();
            tempRealWeightedGpa4 += s.getGrade4() * s.getCredits();
        }
        final int currentRealCredits = tempRealCredits;
        final float currentRealWeightedGpa4 = tempRealWeightedGpa4;
        final float currentGpa4 = currentRealCredits > 0 ? (currentRealWeightedGpa4 / currentRealCredits) : 0f;

        tvSimCurrentGpa.setText(String.format(Locale.US, "%.2f", currentGpa4));

        // Helper to update calculations
        Runnable updateSimulationStats = () -> {
            int totalCredits = currentRealCredits;
            float totalWeightedGpa4 = currentRealWeightedGpa4;
            for (vn.edu.vaa.classmanagerdemo.models.SimulatedCourse c : simulatedList) {
                totalCredits += c.getCredits();
                totalWeightedGpa4 += c.getGrade4() * c.getCredits();
            }
            float projectedGpa4 = totalCredits > 0 ? (totalWeightedGpa4 / totalCredits) : 0f;
            tvSimProjectedGpa.setText(String.format(Locale.US, "%.2f", projectedGpa4));
            tvSimProjectedCredits.setText("Tổng số tín chỉ dự kiến: " + totalCredits + " TC");

            if (simulatedList.isEmpty()) {
                tvSimTrendIcon.setText("➖");
                tvSimTrendText.setText("Chưa có môn học giả định thêm vào.");
                tvSimTrendText.setTextColor(Color.parseColor("#64748B"));
            } else if (projectedGpa4 > currentGpa4 + 0.005f) {
                tvSimTrendIcon.setText("📈");
                tvSimTrendText.setText("GPA có xu hướng tăng (+ " + String.format(Locale.US, "%.2f", projectedGpa4 - currentGpa4) + ")");
                tvSimTrendText.setTextColor(Color.parseColor("#10B981"));
            } else if (projectedGpa4 < currentGpa4 - 0.005f) {
                tvSimTrendIcon.setText("📉");
                tvSimTrendText.setText("GPA có xu hướng giảm (- " + String.format(Locale.US, "%.2f", currentGpa4 - projectedGpa4) + ")");
                tvSimTrendText.setTextColor(Color.parseColor("#EF4444"));
            } else {
                tvSimTrendIcon.setText("➖");
                tvSimTrendText.setText("GPA không thay đổi đáng kể.");
                tvSimTrendText.setTextColor(Color.parseColor("#64748B"));
            }
        };

        // Helper to redraw list of simulated courses
        Runnable renderSimulatedList = new Runnable() {
            @Override
            public void run() {
                containerSimulatedCourses.removeAllViews();
                for (int i = 0; i < simulatedList.size(); i++) {
                    final int idx = i;
                    vn.edu.vaa.classmanagerdemo.models.SimulatedCourse c = simulatedList.get(idx);
                    View row = getLayoutInflater().inflate(R.layout.item_simulated_course, containerSimulatedCourses, false);
                    TextView tvName = row.findViewById(R.id.tvSimItemSubject);
                    TextView tvCredits = row.findViewById(R.id.tvSimItemCredits);
                    TextView tvScoreVal = row.findViewById(R.id.tvSimItemScore);
                    TextView btnRemove = row.findViewById(R.id.btnRemoveSimItem);

                    tvName.setText(c.getSubjectName());
                    tvCredits.setText(c.getCredits() + " tín chỉ  •  GPA hệ 4: " + String.format(Locale.US, "%.1f", c.getGrade4()));
                    tvScoreVal.setText(getString(R.string.score_label, String.format(Locale.US, "%.1f", c.getScore10())));
                    btnRemove.setOnClickListener(v -> {
                        simulatedList.remove(idx);
                        run(); // Re-render
                    });
                    containerSimulatedCourses.addView(row);
                }
                updateSimulationStats.run();
            }
        };

        // Init stats
        updateSimulationStats.run();

        view.findViewById(R.id.btnSimAddCourse).setOnClickListener(v -> {
            String name = edtSimSubjectName.getText().toString().trim();
            String creditsStr = edtSimCredits.getText().toString().trim();
            String scoreStr = edtSimScore.getText().toString().trim();

            if (name.isEmpty()) { edtSimSubjectName.setError("Nhập tên môn học"); return; }
            if (creditsStr.isEmpty()) { edtSimCredits.setError("Nhập số tín chỉ"); return; }
            if (scoreStr.isEmpty()) { edtSimScore.setError("Nhập điểm tổng kết"); return; }

            try {
                int credits = Integer.parseInt(creditsStr);
                float score10 = Float.parseFloat(scoreStr);
                if (credits <= 0) { edtSimCredits.setError("Số tín chỉ phải > 0"); return; }
                if (score10 < 0 || score10 > 10) { edtSimScore.setError(getString(R.string.error_score_range)); return; }

                simulatedList.add(new vn.edu.vaa.classmanagerdemo.models.SimulatedCourse(name, credits, score10));
                edtSimSubjectName.setText("");
                edtSimScore.setText("");
                renderSimulatedList.run();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Dữ liệu nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnSimClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void handleShareGpa() {
        if (fullScoreList.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu điểm để chia sẻ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch user info
        vn.edu.vaa.classmanagerdemo.database.UserDAO userDAO = new vn.edu.vaa.classmanagerdemo.database.UserDAO(this);
        vn.edu.vaa.classmanagerdemo.models.User user = userDAO.findById(currentStudentId);
        String studentName = user != null ? user.getFullName() : "Sinh viên";
        String studentCode = user != null ? user.getUsername() : "N/A";

        // Calculate overall stats
        int totalCredits = 0;
        float weightedGpaSum10 = 0;
        float weightedGpaSum4 = 0;
        for (Score s : fullScoreList) {
            totalCredits += s.getCredits();
            weightedGpaSum10 += s.getScore() * s.getCredits();
            weightedGpaSum4 += s.getGrade4() * s.getCredits();
        }
        float overallGpa10 = totalCredits > 0 ? (weightedGpaSum10 / totalCredits) : 0f;
        float overallGpa4 = totalCredits > 0 ? (weightedGpaSum4 / totalCredits) : 0f;

        // Classification
        String rank;
        if (overallGpa4 >= 3.6f) rank = "Xuất sắc";
        else if (overallGpa4 >= 3.2f) rank = "Giỏi";
        else if (overallGpa4 >= 2.5f) rank = "Khá";
        else if (overallGpa4 >= 2.0f) rank = "Trung bình";
        else if (overallGpa4 >= 1.0f) rank = "Yếu";
        else rank = "Kém";

        // Render card bitmap
        Bitmap bitmap = vn.edu.vaa.classmanagerdemo.utils.GpaShareRenderer.generateGpaCard(
                this, studentName, studentCode, overallGpa4, overallGpa10, totalCredits, rank);

        // Save temporary file in cache directory
        try {
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs(); // create directories if needed
            java.io.File file = new java.io.File(cachePath, "vaa_gpa_card.png");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Share Intent
            android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ bảng điểm"));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể lưu/chia sẻ ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateScorePreview(android.widget.TextView tvScore, android.widget.TextView tvGrade,
            android.widget.EditText edtQt, android.widget.EditText edtWqt,
            android.widget.EditText edtCk, android.widget.EditText edtWck) {
        try {
            float qt = Float.parseFloat(edtQt.getText().toString().trim());
            int wqt = Integer.parseInt(edtWqt.getText().toString().trim());
            float ck = Float.parseFloat(edtCk.getText().toString().trim());
            int wck = Integer.parseInt(edtWck.getText().toString().trim());
            if (wqt + wck != 100) { tvScore.setText("--"); tvGrade.setText(""); return; }
            float total = Math.round((qt * wqt + ck * wck) / 100f * 10f) / 10f;
            tvScore.setText(String.format(java.util.Locale.US, "%.1f", total));
            // Xếp loại chữ
            String letter;
            String color;
            if (total >= 8.5f) { letter = "A  •  Giỏi"; color = "#10B981"; }
            else if (total >= 8.0f) { letter = "B+  •  Khá+"; color = "#3B82F6"; }
            else if (total >= 7.0f) { letter = "B  •  Khá"; color = "#3B82F6"; }
            else if (total >= 6.5f) { letter = "C+  •  TB+"; color = "#F59E0B"; }
            else if (total >= 5.5f) { letter = "C  •  Trung bình"; color = "#F59E0B"; }
            else if (total >= 5.0f) { letter = "D+  •  Yếu+"; color = "#F97316"; }
            else if (total >= 4.0f) { letter = "D  •  Yếu"; color = "#F97316"; }
            else { letter = "F  •  Kém"; color = "#EF4444"; }
            tvScore.setTextColor(android.graphics.Color.parseColor(color));
            tvGrade.setText(letter);
            tvGrade.setTextColor(android.graphics.Color.parseColor(color));
        } catch (NumberFormatException e) {
            tvScore.setText("--");
            tvGrade.setText("");
            tvScore.setTextColor(android.graphics.Color.parseColor("#4F46E5"));
        }
    }
}

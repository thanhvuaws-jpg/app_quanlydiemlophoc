package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.FileProvider;
import android.net.Uri;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.StudentAdapter;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Cell;

public class StudentListActivity extends BaseActivity {
    private static final int RC_IMPORT_CSV = 1001;

    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;
    private RecyclerView recyclerStudents;
    private List<Student> studentList = new ArrayList<>();
    private List<Student> fullStudentList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private int classId;
    private String className;
    private String classSubject;

    // Tab views
    private TabLayout tabLayoutStudents;
    private View layoutTabStudents, layoutTabStats;

    // Stats views
    private TextView tvCountA, tvCountB, tvCountC, tvCountD, tvCountF;
    private TextView tvClassAvg, tvClassMax, tvClassMin;
    private android.widget.ProgressBar progressLoading;

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
        scoreDAO = new ScoreDAO(this);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        progressLoading = findViewById(R.id.progressLoading);

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
            student -> confirmDeleteStudent(student),
            student -> showEditStudentDialog(student),
            studentId -> scoreDAO.getAverageByStudentAndClass(studentId, classId)
        );
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(studentAdapter);

        // Search
        EditText edtSearch = findViewById(R.id.edtSearchStudent);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    filterStudents(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Tab layout
        tabLayoutStudents = findViewById(R.id.tabLayoutStudents);
        layoutTabStudents = findViewById(R.id.layoutTabStudents);
        layoutTabStats = findViewById(R.id.layoutTabStats);

        if (tabLayoutStudents != null) {
            tabLayoutStudents.addTab(tabLayoutStudents.newTab().setText("Học sinh"));
            tabLayoutStudents.addTab(tabLayoutStudents.newTab().setText("Thống kê"));
            tabLayoutStudents.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        layoutTabStudents.setVisibility(View.VISIBLE);
                        layoutTabStats.setVisibility(View.GONE);
                    } else {
                        layoutTabStudents.setVisibility(View.GONE);
                        layoutTabStats.setVisibility(View.VISIBLE);
                        loadClassStats();
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        // Stat views
        tvCountA = findViewById(R.id.tvCountA); tvCountB = findViewById(R.id.tvCountB);
        tvCountC = findViewById(R.id.tvCountC); tvCountD = findViewById(R.id.tvCountD);
        tvCountF = findViewById(R.id.tvCountF);
        tvClassAvg = findViewById(R.id.tvClassAvg);
        tvClassMax = findViewById(R.id.tvClassMax);
        tvClassMin = findViewById(R.id.tvClassMin);

        // Export CSV button
        View btnExport = findViewById(R.id.btnExportClass);
        if (btnExport != null) btnExport.setOnClickListener(v -> exportClassToCsv());

        View btnShareImage = findViewById(R.id.btnShareImage);
        if (btnShareImage != null) btnShareImage.setOnClickListener(v -> shareClassScoreImage());

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
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Student> list = studentDAO.getByClassId(classId);
            runOnUiThread(() -> {
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);
                fullStudentList.clear();
                fullStudentList.addAll(list);
                studentList.clear();
                studentList.addAll(list);
                studentAdapter.notifyDataSetChanged();

                TextView tvCount = findViewById(R.id.tvStudentCount);
                if (tvCount != null) tvCount.setText(list.size() + " học sinh");

                View emptyLayout = findViewById(R.id.layoutEmptyStudents);
                if (emptyLayout != null) {
                    emptyLayout.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerStudents.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            });
        }).start();
    }

    private void filterStudents(String query) {
        studentList.clear();
        if (query.isEmpty()) {
            studentList.addAll(fullStudentList);
        } else {
            String lower = query.toLowerCase();
            for (Student s : fullStudentList) {
                if (s.getFullName().toLowerCase().contains(lower) ||
                    s.getStudentCode().toLowerCase().contains(lower)) {
                    studentList.add(s);
                }
            }
        }
        studentAdapter.notifyDataSetChanged();
    }

    private void loadClassStats() {
        new Thread(() -> {
            List<Score> scores = scoreDAO.getByClassId(classId);
            if (scores.isEmpty()) return;
            int cntA = 0, cntB = 0, cntC = 0, cntD = 0, cntF = 0;
            float sum = 0, max = 0, min = 10;
            for (Score s : scores) {
                float sc = s.getScore();
                sum += sc;
                if (sc > max) max = sc;
                if (sc < min) min = sc;
                switch (s.getGradeLetter()) {
                    case "A": cntA++; break;
                    case "B": cntB++; break;
                    case "C": cntC++; break;
                    case "D": cntD++; break;
                    default: cntF++; break;
                }
            }
            float avg = sum / scores.size();
            final float fAvg = avg, fMax = max, fMin = min;
            final int fA = cntA, fB = cntB, fC = cntC, fD = cntD, fF = cntF;

            // Ranking
            List<Score> ranked = scoreDAO.getRankedByClass(classId);

            runOnUiThread(() -> {
                if (tvCountA != null) tvCountA.setText(String.valueOf(fA));
                if (tvCountB != null) tvCountB.setText(String.valueOf(fB));
                if (tvCountC != null) tvCountC.setText(String.valueOf(fC));
                if (tvCountD != null) tvCountD.setText(String.valueOf(fD));
                if (tvCountF != null) tvCountF.setText(String.valueOf(fF));
                if (tvClassAvg != null) tvClassAvg.setText(String.format(java.util.Locale.US, "%.1f", fAvg));
                if (tvClassMax != null) tvClassMax.setText(String.format(java.util.Locale.US, "%.1f", fMax));
                if (tvClassMin != null) tvClassMin.setText(String.format(java.util.Locale.US, "%.1f", fMin));

                RecyclerView rvRanking = findViewById(R.id.recyclerRanking);
                if (rvRanking != null) {
                    rvRanking.setLayoutManager(new LinearLayoutManager(this));
                    rvRanking.setAdapter(new RankingAdapter(ranked));
                }
            });
        }).start();
    }

    private void exportClassToCsv() {
        new Thread(() -> {
            List<Score> scores = scoreDAO.getByClassId(classId);
            if (scores.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Lớp chưa có điểm nào", Toast.LENGTH_SHORT).show());
                return;
            }
            try {
                File dir = new File(getCacheDir(), "exports");
                dir.mkdirs();
                String safeClass = className != null ? className.replaceAll("[^a-zA-Z0-9]", "_") : "class";
                File file = new File(dir, "diem_" + safeClass + ".csv");
                FileWriter fw = new FileWriter(file);
                fw.write('\uFEFF'); // UTF-8 BOM
                fw.write("Mã HS,Họ tên,GK,CK,Điểm TK,Xếp loại\n");
                for (Score s : scores) {
                    fw.write(String.format(java.util.Locale.US, "%s,%s,%.1f,%.1f,%.1f,%s\n",
                        s.getStudentCode() != null ? s.getStudentCode() : "",
                        s.getStudentName() != null ? s.getStudentName() : "",
                        s.getScoreGK(), s.getScoreCK(),
                        s.getScore(), s.getGradeLabel()));
                }
                fw.close();
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Điểm lớp " + className);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(intent, "Chia sẻ file điểm")));
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void shareClassScoreImage() {
        new Thread(() -> {
            List<Score> scores = scoreDAO.getByClassId(classId);
            if (scores.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Lớp chưa có điểm nào để vẽ", Toast.LENGTH_SHORT).show());
                return;
            }
            try {
                File dir = new File(getCacheDir(), "shares");
                if (!dir.exists()) dir.mkdirs();
                File imageFile = vn.edu.vaa.classmanagerdemo.utils.ClassScoreRenderer.renderToPng(dir, className, classSubject, scores);
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bảng điểm lớp " + className);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(Intent.createChooser(intent, "Chia sẻ bảng điểm bằng ảnh")));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi tạo ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showAddStudentDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        EditText edtCode = view.findViewById(R.id.edtStudentCode);
        EditText edtName = view.findViewById(R.id.edtStudentName);
        EditText edtNote = view.findViewById(R.id.edtStudentNote);

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(view);

        view.findViewById(R.id.btnSaveStudent).setOnClickListener(DebounceClickListener.wrap(v -> {
            String code = edtCode.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            if (code.isEmpty()) { Toast.makeText(this, "Nhập mã học sinh", Toast.LENGTH_SHORT).show(); return; }
            if (name.isEmpty()) { Toast.makeText(this, "Nhập họ tên", Toast.LENGTH_SHORT).show(); return; }
            if (studentDAO.existsByCode(classId, code)) {
                Toast.makeText(this, "Mã học sinh đã tồn tại trong lớp", Toast.LENGTH_SHORT).show(); return;
            }
            Student s = new Student(classId, code, name);
            if (edtNote != null) s.setNote(edtNote.getText().toString().trim());
            studentDAO.insert(s);
            loadStudents();
            dialog.dismiss();
            Toast.makeText(this, "Đã thêm " + name, Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void showEditStudentDialog(Student student) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        EditText edtCode = view.findViewById(R.id.edtStudentCode);
        EditText edtName = view.findViewById(R.id.edtStudentName);
        EditText edtNote = view.findViewById(R.id.edtStudentNote);
        edtCode.setText(student.getStudentCode());
        edtName.setText(student.getFullName());
        if (edtNote != null) edtNote.setText(student.getNote());

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(view);

        view.findViewById(R.id.btnSaveStudent).setOnClickListener(DebounceClickListener.wrap(v -> {
            student.setStudentCode(edtCode.getText().toString().trim());
            student.setFullName(edtName.getText().toString().trim());
            if (edtNote != null) student.setNote(edtNote.getText().toString().trim());
            studentDAO.update(student);
            loadStudents();
            dialog.dismiss();
            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
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
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavigationHelper.finishWithSlide(this);
            return true;
        } else if (item.getItemId() == R.id.action_bulk_score) {
            Intent i = new Intent(this, BulkScoreActivity.class);
            i.putExtra("class_id", classId);
            i.putExtra("class_name", className);
            i.putExtra("class_subject", classSubject);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.action_import_csv) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimeTypes = {
                "text/comma-separated-values",
                "text/csv",
                "text/plain",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Chọn file CSV hoặc Excel"), RC_IMPORT_CSV);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_IMPORT_CSV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = getFileName(uri);
                if (fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
                    processXlsxFile(uri);
                } else {
                    processCsvFile(uri);
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (idx != -1) result = cursor.getString(idx);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void processXlsxFile(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            try (ReadableWorkbook wb = new ReadableWorkbook(inputStream)) {
                Sheet sheet = wb.getFirstSheet();
                List<Row> rows = sheet.read();
                
                if (rows == null || rows.isEmpty()) {
                    Toast.makeText(this, "File Excel trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Row headerRow = rows.get(0);
                List<String> headers = new ArrayList<>();
                for (int i = 0; i < headerRow.getCellCount(); i++) {
                    Cell c = headerRow.getCell(i);
                    headers.add(c != null ? c.getText().trim() : "");
                }
                
                if (headers.size() < 2) {
                    Toast.makeText(this, "File Excel phải có ít nhất 2 cột dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                showXlsxMappingDialog(rows, headers);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc file Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showXlsxMappingDialog(List<Row> rows, List<String> headers) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);
        
        TextView tvNameHint = new TextView(this);
        tvNameHint.setText("Chọn cột chứa Họ và tên:");
        tvNameHint.setTextColor(getResources().getColor(R.color.text_secondary));
        tvNameHint.setPadding(0, 10, 0, 10);
        layout.addView(tvNameHint);
        
        android.widget.Spinner spinnerName = new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, headers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerName.setAdapter(adapter);
        layout.addView(spinnerName);
        
        TextView tvCodeHint = new TextView(this);
        tvCodeHint.setText("Chọn cột chứa Mã học sinh / MSSV:");
        tvCodeHint.setTextColor(getResources().getColor(R.color.text_secondary));
        tvCodeHint.setPadding(0, 20, 0, 10);
        layout.addView(tvCodeHint);
        
        android.widget.Spinner spinnerCode = new android.widget.Spinner(this);
        spinnerCode.setAdapter(adapter);
        layout.addView(spinnerCode);
        
        // Auto select best match
        int nameIndex = findNameColumn(headers);
        int codeIndex = findCodeColumn(headers);
        if (nameIndex == codeIndex && headers.size() > 1) {
            codeIndex = (nameIndex + 1) % headers.size();
        }
        spinnerName.setSelection(nameIndex);
        spinnerCode.setSelection(codeIndex);
        
        new AlertDialog.Builder(this)
            .setTitle("Ánh xạ cột từ Excel")
            .setView(layout)
            .setPositiveButton("Bắt đầu Import", (dialog, which) -> {
                int selectedNameCol = spinnerName.getSelectedItemPosition();
                int selectedCodeCol = spinnerCode.getSelectedItemPosition();
                importXlsxData(rows, selectedNameCol, selectedCodeCol);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void importXlsxData(List<Row> rows, int nameCol, int codeCol) {
        new Thread(() -> {
            int success = 0;
            int duplicate = 0;
            for (int i = 1; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row == null) continue;
                if (row.getCellCount() <= Math.max(nameCol, codeCol)) continue;
                
                Cell nameCell = row.getCell(nameCol);
                Cell codeCell = row.getCell(codeCol);
                
                String name = nameCell != null ? nameCell.getText().trim() : "";
                String code = codeCell != null ? codeCell.getText().trim() : "";
                
                if (name.isEmpty() || code.isEmpty()) continue;
                
                if (studentDAO.existsByCode(classId, code)) {
                    duplicate++;
                } else {
                    Student s = new Student(classId, code, name);
                    studentDAO.insert(s);
                    success++;
                }
            }
            
            final int fSuccess = success;
            final int fDuplicate = duplicate;
            runOnUiThread(() -> {
                loadStudents();
                Toast.makeText(this, 
                    "Đã thêm thành công " + fSuccess + " học sinh từ file Excel. Trùng lặp: " + fDuplicate, 
                    Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private void processCsvFile(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, "UTF-8"));
            
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (lines.isEmpty() && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
            reader.close();
            inputStream.close();
            
            if (lines.isEmpty()) {
                Toast.makeText(this, "File CSV trống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String firstLine = lines.get(0);
            String delimiter = ",";
            if (firstLine.contains(";")) {
                int commaCount = firstLine.length() - firstLine.replace(",", "").length();
                int semiCount = firstLine.length() - firstLine.replace(";", "").length();
                if (semiCount > commaCount) {
                    delimiter = ";";
                }
            }
            
            List<String> headers = parseCsvLine(firstLine, delimiter);
            if (headers.size() < 2) {
                Toast.makeText(this, "File CSV phải có ít nhất 2 cột dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showCsvMappingDialog(lines, headers, delimiter);
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private List<String> parseCsvLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (String.valueOf(c).equals(delimiter) && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }

    private void showCsvMappingDialog(List<String> lines, List<String> headers, String delimiter) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);
        
        TextView tvNameHint = new TextView(this);
        tvNameHint.setText("Chọn cột chứa Họ và tên:");
        tvNameHint.setTextColor(getResources().getColor(R.color.text_secondary));
        tvNameHint.setPadding(0, 10, 0, 10);
        layout.addView(tvNameHint);
        
        android.widget.Spinner spinnerName = new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, headers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerName.setAdapter(adapter);
        layout.addView(spinnerName);
        
        TextView tvCodeHint = new TextView(this);
        tvCodeHint.setText("Chọn cột chứa Mã học sinh / MSSV:");
        tvCodeHint.setTextColor(getResources().getColor(R.color.text_secondary));
        tvCodeHint.setPadding(0, 20, 0, 10);
        layout.addView(tvCodeHint);
        
        android.widget.Spinner spinnerCode = new android.widget.Spinner(this);
        spinnerCode.setAdapter(adapter);
        layout.addView(spinnerCode);
        
        // Auto select best match
        int nameIndex = findNameColumn(headers);
        int codeIndex = findCodeColumn(headers);
        if (nameIndex == codeIndex && headers.size() > 1) {
            codeIndex = (nameIndex + 1) % headers.size();
        }
        spinnerName.setSelection(nameIndex);
        spinnerCode.setSelection(codeIndex);
        
        new AlertDialog.Builder(this)
            .setTitle("Ánh xạ cột từ CSV")
            .setView(layout)
            .setPositiveButton("Bắt đầu Import", (dialog, which) -> {
                int selectedNameCol = spinnerName.getSelectedItemPosition();
                int selectedCodeCol = spinnerCode.getSelectedItemPosition();
                importCsvData(lines, selectedNameCol, selectedCodeCol, delimiter);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void importCsvData(List<String> lines, int nameCol, int codeCol, String delimiter) {
        new Thread(() -> {
            int success = 0;
            int duplicate = 0;
            for (int i = 1; i < lines.size(); i++) {
                List<String> cols = parseCsvLine(lines.get(i), delimiter);
                if (cols.size() <= Math.max(nameCol, codeCol)) continue;
                
                String name = cols.get(nameCol);
                String code = cols.get(codeCol);
                
                if (name.isEmpty() || code.isEmpty()) continue;
                
                if (studentDAO.existsByCode(classId, code)) {
                    duplicate++;
                } else {
                    Student s = new Student(classId, code, name);
                    studentDAO.insert(s);
                    success++;
                }
            }
            
            final int fSuccess = success;
            final int fDuplicate = duplicate;
            runOnUiThread(() -> {
                loadStudents();
                Toast.makeText(this, 
                    "Đã thêm thành công " + fSuccess + " học sinh. Trùng lặp: " + fDuplicate, 
                    Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    // ── Ranking Adapter (inline) ──────────────────────────────────────────────
    class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {
        private final List<Score> list;
        RankingAdapter(List<Score> list) { this.list = list; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(StudentListActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 20, 32, 20);
            row.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Score s = list.get(pos);
            String medal = pos == 0 ? "🥇" : pos == 1 ? "🥈" : pos == 2 ? "🥉" : "#" + (pos + 1);
            String name = s.getStudentName() != null ? s.getStudentName() : "?";
            String scoreStr = String.format(java.util.Locale.US, "%.1f  %s", s.getScore(), s.getGradeLetter());
            h.tvRank.setText(medal);
            h.tvName.setText(name);
            h.tvScore.setText(scoreStr);
            try { h.tvScore.setTextColor(android.graphics.Color.parseColor(s.getGradeColor())); }
            catch (Exception ignored) {}
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvScore;
            VH(LinearLayout row) {
                super(row);
                tvRank = new TextView(StudentListActivity.this);
                tvRank.setLayoutParams(new LinearLayout.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT));
                tvRank.setTextSize(14); tvRank.setGravity(Gravity.CENTER);
                row.addView(tvRank);

                tvName = new TextView(StudentListActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                tvName.setLayoutParams(lp);
                tvName.setTextSize(13);
                try { tvName.setTextColor(android.graphics.Color.parseColor("#0F172A")); } catch (Exception ignored) {}
                row.addView(tvName);

                tvScore = new TextView(StudentListActivity.this);
                tvScore.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
                tvScore.setTextSize(14); tvScore.setTypeface(null, android.graphics.Typeface.BOLD);
                tvScore.setGravity(Gravity.END);
                row.addView(tvScore);
            }
        }
    }

    private int findCodeColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("mã hs") || h.equals("ma hs") || h.equals("mã số") || h.equals("ma so") || 
                h.equals("mssv") || h.equals("mã sinh viên") || h.equals("ma sinh vien") ||
                h.equals("mã học sinh") || h.equals("ma hoc sinh") || h.equals("student code") || 
                h.equals("student id") || h.equals("code") || h.equals("id")) {
                return i;
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.contains("mssv") || h.contains("code") || h.contains("mã số") || h.contains("ma so") ||
                h.equals("mã") || h.equals("ma")) {
                return i;
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if ((h.contains("mã") || h.contains("ma")) && !h.contains("tên") && !h.contains("ten")) {
                return i;
            }
        }
        return 0;
    }

    private int findNameColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("họ tên") || h.equals("ho ten") || h.equals("họ và tên") || h.equals("ho va ten") || 
                h.equals("tên") || h.equals("ten") || h.equals("name") || h.equals("full name") || h.equals("fullname")) {
                return i;
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if ((h.contains("tên") || h.contains("ten") || h.contains("name")) && 
                !h.contains("mã") && !h.contains("code") && !h.contains("lớp") && !h.contains("lop")) {
                return i;
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if ((h.contains("họ") || h.contains("ho")) && !h.contains("học") && !h.contains("hoc") && !h.contains("code")) {
                return i;
            }
        }
        return 1 < headers.size() ? 1 : 0;
    }
}

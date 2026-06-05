package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.SchoolClass;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;

public class ImportExportActivity extends BaseActivity {
    private static final int RC_CHOOSE_CSV = 2001;

    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private ScoreDAO scoreDAO;
    private AppPreferenceManager prefs;

    private Spinner spinnerClasses;
    private TextView txtCsvContent;
    private List<SchoolClass> classList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        Toolbar toolbar = findViewById(R.id.toolbarImportExport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nhập & Xuất Bảng Điểm");
        }

        classDAO = new ClassDAO(this);
        studentDAO = new StudentDAO(this);
        scoreDAO = new ScoreDAO(this);
        prefs = new AppPreferenceManager(this);

        spinnerClasses = findViewById(R.id.spinnerClasses);
        txtCsvContent = findViewById(R.id.txtCsvContent);

        loadClasses();

        findViewById(R.id.btnExportCsv).setOnClickListener(v -> exportToCsv(false));
        findViewById(R.id.btnShareCsv).setOnClickListener(v -> exportToCsv(true));
        findViewById(R.id.btnImportCsvFile).setOnClickListener(v -> chooseCsvFile());
        findViewById(R.id.btnImportCsvText).setOnClickListener(v -> showPasteCsvDialog());
    }

    private void loadClasses() {
        int teacherId = prefs.getCurrentUserId();
        classList = classDAO.getByTeacherId(teacherId);

        List<String> spinnerItems = new ArrayList<>();
        for (SchoolClass c : classList) {
            spinnerItems.add(c.getClassName() + " (" + c.getSubject() + ")");
        }

        if (spinnerItems.isEmpty()) {
            spinnerItems.add("(Chưa có lớp học nào)");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasses.setAdapter(spinnerAdapter);
    }

    private SchoolClass getSelectedClass() {
        int pos = spinnerClasses.getSelectedItemPosition();
        if (pos >= 0 && pos < classList.size()) {
            return classList.get(pos);
        }
        Toast.makeText(this, "Vui lòng tạo lớp học trước", Toast.LENGTH_SHORT).show();
        return null;
    }

    private void exportToCsv(boolean shareImmediately) {
        SchoolClass selectedClass = getSelectedClass();
        if (selectedClass == null) return;

        new Thread(() -> {
            int classId = selectedClass.getId();
            List<Student> students = studentDAO.getByClassId(classId);
            List<Score> scores = scoreDAO.getByClassId(classId);

            Map<Integer, Score> scoreMap = new HashMap<>();
            for (Score s : scores) {
                scoreMap.put(s.getStudentId(), s);
            }

            try {
                File dir = new File(getCacheDir(), "exports");
                dir.mkdirs();
                String safeName = selectedClass.getClassName().replaceAll("[^a-zA-Z0-9]", "_");
                File file = new File(dir, "BangDiem_" + safeName + ".csv");
                FileWriter fw = new FileWriter(file);
                fw.write('\uFEFF'); // UTF-8 BOM
                fw.write("Mã Học Sinh,Họ và Tên,Điểm Giữa Kỳ,Điểm Cuối Kỳ,Học Kỳ,Ghi Chú\n");

                for (Student student : students) {
                    Score s = scoreMap.get(student.getId());
                    float gk = s != null ? s.getScoreGK() : 0.0f;
                    float ck = s != null ? s.getScoreCK() : 0.0f;
                    String sem = s != null && s.getSemester() != null ? s.getSemester() : "Học kỳ I";
                    String note = student.getNote() != null ? student.getNote() : "";

                    fw.write(String.format(java.util.Locale.US, "%s,%s,%.1f,%.1f,%s,%s\n",
                        student.getStudentCode(),
                        student.getFullName(),
                        gk, ck, sem, note));
                }
                fw.close();

                runOnUiThread(() -> {
                    try {
                        String previewText = readTextFile(file);
                        txtCsvContent.setText(previewText);
                    } catch (Exception ignored) {}

                    if (shareImmediately) {
                        shareCsvFile(file, selectedClass.getClassName());
                    } else {
                        Toast.makeText(this, "Đã xuất bảng điểm thành công: " + file.getName(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi xuất file: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void shareCsvFile(File file, String className) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bảng điểm lớp " + className);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Chia sẻ file bảng điểm"));
    }

    private void chooseCsvFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/comma-separated-values", "text/csv", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn file CSV"), RC_CHOOSE_CSV);
    }

    private void showPasteCsvDialog() {
        EditText edtPaste = new EditText(this);
        edtPaste.setHint("Mã Học Sinh,Họ và Tên,Điểm Giữa Kỳ,Điểm Cuối Kỳ,Học Kỳ,Ghi Chú\n251101,Nguyễn Văn An,8.5,9.0,Học kỳ I,Học khá tốt");
        edtPaste.setGravity(Gravity.TOP);
        edtPaste.setMinLines(6);

        new AlertDialog.Builder(this)
            .setTitle("Dán dữ liệu CSV")
            .setView(edtPaste)
            .setPositiveButton("Nhập dữ liệu", (dialog, which) -> {
                String text = edtPaste.getText().toString().trim();
                if (!text.isEmpty()) {
                    importCsvContent(text);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CHOOSE_CSV && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    String content = readUriContent(uri);
                    importCsvContent(content);
                } catch (IOException e) {
                    Toast.makeText(this, "Lỗi đọc file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void importCsvContent(String csvText) {
        SchoolClass selectedClass = getSelectedClass();
        if (selectedClass == null) return;

        new Thread(() -> {
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(csvText));
                String line;
                List<String> lines = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (lines.isEmpty() && line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                    if (!line.trim().isEmpty()) {
                        lines.add(line);
                    }
                }
                reader.close();

                if (lines.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Dữ liệu CSV trống", Toast.LENGTH_SHORT).show());
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

                int codeCol = findCodeColumn(headers);
                int nameCol = findNameColumn(headers);
                int gkCol = findGkColumn(headers);
                int ckCol = findCkColumn(headers);
                int semCol = findSemesterColumn(headers);
                int noteCol = findNoteColumn(headers);

                int classId = selectedClass.getId();
                List<Student> students = studentDAO.getByClassId(classId);
                Map<String, Student> studentMap = new HashMap<>();
                for (Student s : students) {
                    studentMap.put(s.getStudentCode(), s);
                }

                int successCount = 0;

                for (int i = 1; i < lines.size(); i++) {
                    List<String> cols = parseCsvLine(lines.get(i), delimiter);
                    if (cols.size() <= codeCol) continue;

                    String studentCode = cols.get(codeCol).trim();
                    if (studentCode.isEmpty()) continue;

                    String studentName = nameCol != -1 && cols.size() > nameCol ? cols.get(nameCol).trim() : studentCode;
                    if (studentName.isEmpty()) studentName = studentCode;

                    float gk = 0f;
                    if (gkCol != -1 && cols.size() > gkCol) {
                        gk = parseFloatSafe(cols.get(gkCol));
                    }

                    float ck = 0f;
                    if (ckCol != -1 && cols.size() > ckCol) {
                        ck = parseFloatSafe(cols.get(ckCol));
                    }

                    String semester = "Học kỳ I";
                    if (semCol != -1 && cols.size() > semCol) {
                        String sVal = cols.get(semCol).trim();
                        if (!sVal.isEmpty()) semester = sVal;
                    }

                    String note = "";
                    if (noteCol != -1 && cols.size() > noteCol) {
                        note = cols.get(noteCol).trim();
                    }

                    Student student = studentMap.get(studentCode);
                    int studentId;
                    if (student == null) {
                        student = new Student(classId, studentCode, studentName);
                        student.setNote(note);
                        studentId = (int) studentDAO.insert(student);
                        studentMap.put(studentCode, student);
                    } else {
                        studentId = student.getId();
                        if (!note.isEmpty() && !note.equals(student.getNote())) {
                            student.setNote(note);
                            studentDAO.update(student);
                        }
                    }

                    List<Score> currentScores = scoreDAO.getByStudentAndClass(studentId, classId);
                    Score matchedScore = null;
                    for (Score sc : currentScores) {
                        if (semester.equalsIgnoreCase(sc.getSemester())) {
                            matchedScore = sc;
                            break;
                        }
                    }

                    if (matchedScore != null) {
                        matchedScore.setScoreGK(gk);
                        matchedScore.setScoreCK(ck);
                        matchedScore.setSubject(selectedClass.getSubject());
                        scoreDAO.update(matchedScore);
                    } else {
                        Score newScore = new Score();
                        newScore.setStudentId(studentId);
                        newScore.setClassId(classId);
                        newScore.setSubject(selectedClass.getSubject());
                        newScore.setScoreQT(0f);
                        newScore.setWeightQT(0);
                        newScore.setScoreGK(gk);
                        newScore.setWeightGK(50);
                        newScore.setScoreCK(ck);
                        newScore.setWeightCK(50);
                        newScore.setSemester(semester);
                        scoreDAO.insert(newScore);
                    }
                    successCount++;
                }

                final int fSuccess = successCount;
                runOnUiThread(() -> {
                    txtCsvContent.setText(csvText);
                    Toast.makeText(this, "Nhập thành công " + fSuccess + " học sinh & điểm số!", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi import: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
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

    private String readTextFile(File file) throws IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String readUriContent(Uri uri) throws IOException {
        java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        inputStream.close();
        return sb.toString();
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

    private int findGkColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("gk") || h.contains("giữa") || h.contains("giua") || h.contains("midterm") || h.contains("giữa kỳ") || h.contains("giua ky")) {
                return i;
            }
        }
        return -1;
    }

    private int findCkColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("ck") || h.contains("cuối") || h.contains("cuoi") || h.contains("final") || h.contains("cuối kỳ") || h.contains("cuoi ky")) {
                return i;
            }
        }
        return -1;
    }

    private int findSemesterColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("học kỳ") || h.equals("hoc ky") || h.contains("kỳ") || h.contains("ky") || h.contains("semester")) {
                if (!h.contains("giữa") && !h.contains("giua") && !h.contains("cuối") && !h.contains("cuoi") && !h.equals("gk") && !h.equals("ck")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findNoteColumn(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase().trim();
            if (h.equals("ghi chú") || h.equals("ghi chu") || h.contains("chú") || h.contains("chu") || h.contains("note") || h.contains("nhận xét") || h.contains("nhan xet")) {
                return i;
            }
        }
        return -1;
    }

    private float parseFloatSafe(String val) {
        if (val == null) return 0f;
        val = val.trim().replace(",", ".");
        try {
            return Float.parseFloat(val);
        } catch (Exception e) {
            return 0f;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

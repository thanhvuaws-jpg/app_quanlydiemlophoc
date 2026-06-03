package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.ExcelImporter;

public class ImportStudentActivity extends AppCompatActivity {

    private TextView tvFileInfo, tvPreview, tvResult;
    private Button btnImportNow;

    private Uri selectedUri;
    private String selectedFilename;
    private List<String[]> parsedRows;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;

    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                selectedUri = uri;
                selectedFilename = getFilenameFromUri(uri);
                tvFileInfo.setText("File: " + selectedFilename);
                parseFile();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new AppPreferenceManager(this).isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_import_student);

        Toolbar toolbar = findViewById(R.id.toolbarImport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        classDAO = new ClassDAO(this);
        studentDAO = new StudentDAO(this);

        tvFileInfo = findViewById(R.id.tvFileInfo);
        tvPreview = findViewById(R.id.tvPreview);
        tvResult = findViewById(R.id.tvImportResult);
        btnImportNow = findViewById(R.id.btnImportNow);
        btnImportNow.setEnabled(false);

        Button btnPick = findViewById(R.id.btnPickFile);
        btnPick.setOnClickListener(v -> filePicker.launch(new String[]{"*/*"}));
        btnImportNow.setOnClickListener(v -> confirmImport());
    }

    private void parseFile() {
        try {
            parsedRows = ExcelImporter.parse(this, selectedUri, selectedFilename);
            if (parsedRows.isEmpty()) {
                tvPreview.setText("File rỗng");
                btnImportNow.setEnabled(false);
                return;
            }
            // Show preview (first 8 rows)
            StringBuilder sb = new StringBuilder();
            sb.append("Tổng: ").append(parsedRows.size()).append(" dòng\n\n");
            int preview = Math.min(parsedRows.size(), 8);
            for (int i = 0; i < preview; i++) {
                sb.append(String.join("  |  ", parsedRows.get(i))).append("\n");
            }
            if (parsedRows.size() > 8) sb.append("... và ").append(parsedRows.size() - 8).append(" dòng nữa");
            tvPreview.setText(sb.toString());
            btnImportNow.setEnabled(true);
            tvResult.setText("");
        } catch (Exception e) {
            tvPreview.setText("Lỗi đọc file: " + e.getMessage());
            btnImportNow.setEnabled(false);
        }
    }

    private void confirmImport() {
        int dataRows = parsedRows.size() - 1; // subtract header
        if (dataRows <= 0) { Toast.makeText(this, "Không có dữ liệu để import", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận import")
                .setMessage("Import " + dataRows + " sinh viên từ file \"" + selectedFilename + "\"?\n\n" +
                        "Định dạng cột: MSSV | Họ tên | Lớp | Email | SĐT")
                .setPositiveButton("Import", (d, w) -> doImport())
                .setNegativeButton("Hủy", null).show();
    }

    private void doImport() {
        if (parsedRows == null || parsedRows.size() < 2) return;

        int success = 0, skipped = 0;
        // Row 0 = header, skip it
        for (int i = 1; i < parsedRows.size(); i++) {
            String[] row = parsedRows.get(i);
            if (row.length < 2) continue;

            String mssv = row.length > 0 ? row[0].trim() : "";
            String name = row.length > 1 ? row[1].trim() : "";
            String className = row.length > 2 ? row[2].trim() : "";
            String email = row.length > 3 ? row[3].trim() : "";
            String phone = row.length > 4 ? row[4].trim() : "";

            if (name.isEmpty()) continue;

            // Skip if MSSV already exists
            if (!mssv.isEmpty() && studentDAO.existsByStudentCode(mssv)) {
                skipped++;
                continue;
            }

            // Get or create the class
            int cid = 0;
            if (!className.isEmpty()) {
                ClassRoom cr = classDAO.getOrCreate(className, "");
                cid = cr.getId();
            }

            Student s = new Student(mssv, name, cid, email, phone, className);
            studentDAO.insertFull(s);
            success++;
        }

        String result = "✓ Import thành công: " + success + " sinh viên";
        if (skipped > 0) result += "\n⚠ Bỏ qua " + skipped + " (MSSV đã tồn tại)";
        tvResult.setText(result);
        btnImportNow.setEnabled(false);
        Toast.makeText(this, "Import xong!", Toast.LENGTH_SHORT).show();
    }

    private String getFilenameFromUri(Uri uri) {
        String name = "unknown";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        }
        return name;
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

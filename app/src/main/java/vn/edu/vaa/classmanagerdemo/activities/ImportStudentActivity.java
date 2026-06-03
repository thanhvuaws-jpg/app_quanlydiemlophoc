package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.ExcelImporter;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.utils.LoadingHelper;

public class ImportStudentActivity extends AppCompatActivity {
    private static final String TAG = "ImportStudentActivity";

    private TextView tvFileInfo, tvPreview, tvResult;
    private Button btnImportNow;

    private Uri selectedUri;
    private String selectedFilename;
    private List<String[]> parsedRows;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private final LoadingHelper loading = new LoadingHelper();

    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                selectedUri = uri;
                selectedFilename = getFilenameFromUri(uri);
                tvFileInfo.setText("File: " + selectedFilename);
                parseFileInBackground();
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
        // Debounce tránh mở picker nhiều lần
        btnPick.setOnClickListener(DebounceClickListener.wrap(v -> filePicker.launch(new String[]{"*/*"})));
        btnImportNow.setOnClickListener(DebounceClickListener.wrap(v -> confirmImport()));
    }

    // Đọc file trên background thread — tránh block UI khi file lớn
    private void parseFileInBackground() {
        loading.show(this, "Đang đọc file " + selectedFilename + "...");
        btnImportNow.setEnabled(false);
        tvPreview.setText("Đang phân tích file...");
        tvResult.setText("");

        new Thread(() -> {
            List<String[]> rows = null;
            Exception error = null;
            try {
                rows = ExcelImporter.parse(this, selectedUri, selectedFilename);
            } catch (Exception e) {
                error = e;
            }
            final List<String[]> finalRows = rows;
            final Exception finalError = error;
            runOnUiThread(() -> {
                loading.dismiss();
                if (finalError != null) {
                    String msg = "Lỗi đọc file: " + finalError.getMessage();
                    Log.e(TAG, msg, finalError);
                    tvPreview.setText(msg);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    btnImportNow.setEnabled(false);
                    return;
                }
                parsedRows = finalRows;
                if (parsedRows == null || parsedRows.isEmpty()) {
                    tvPreview.setText("File rỗng hoặc không đọc được dữ liệu.");
                    btnImportNow.setEnabled(false);
                    return;
                }
                showPreview();
            });
        }).start();
    }

    private void showPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tổng: ").append(parsedRows.size()).append(" dòng\n\n");
        int preview = Math.min(parsedRows.size(), 8);
        for (int i = 0; i < preview; i++) {
            sb.append(String.join("  |  ", parsedRows.get(i))).append("\n");
        }
        if (parsedRows.size() > 8) sb.append("... và ").append(parsedRows.size() - 8).append(" dòng nữa");
        tvPreview.setText(sb.toString());
        btnImportNow.setEnabled(true);
        Log.d(TAG, "Đọc file xong: " + parsedRows.size() + " dòng từ " + selectedFilename);
    }

    private void confirmImport() {
        if (parsedRows == null || parsedRows.size() < 2) {
            Toast.makeText(this, "Không có dữ liệu để import (cần ít nhất 1 dòng dữ liệu sau header)", Toast.LENGTH_SHORT).show();
            return;
        }
        int dataRows = parsedRows.size() - 1;
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận import")
                .setMessage("Import " + dataRows + " sinh viên từ file \"" + selectedFilename + "\"?\n\n" +
                        "Định dạng cột: MSSV | Họ tên | Lớp | Email | SĐT")
                .setPositiveButton("Import", (d, w) -> doImportInBackground())
                .setNegativeButton("Hủy", null).show();
    }

    // Import trên background thread — tránh block UI với file nhiều dòng
    private void doImportInBackground() {
        loading.show(this, "Đang import dữ liệu...");
        btnImportNow.setEnabled(false);

        new Thread(() -> {
            int success = 0, skipped = 0;
            String lastError = null;
            try {
                for (int i = 1; i < parsedRows.size(); i++) {
                    String[] row = parsedRows.get(i);
                    if (row.length < 2) continue;

                    String mssv = row.length > 0 ? row[0].trim() : "";
                    String name = row.length > 1 ? row[1].trim() : "";
                    String className = row.length > 2 ? row[2].trim() : "";
                    String email = row.length > 3 ? row[3].trim() : "";
                    String phone = row.length > 4 ? row[4].trim() : "";

                    if (name.isEmpty()) continue;

                    if (!mssv.isEmpty() && studentDAO.existsByStudentCode(mssv)) {
                        skipped++;
                        Log.d(TAG, "Bỏ qua MSSV đã tồn tại: " + mssv);
                        continue;
                    }

                    int cid = 0;
                    if (!className.isEmpty()) {
                        ClassRoom cr = classDAO.getOrCreate(className, "");
                        cid = cr.getId();
                    }

                    Student s = new Student(mssv, name, cid, email, phone, className);
                    studentDAO.insertFull(s);
                    success++;
                }
            } catch (Exception e) {
                lastError = "Lỗi import: " + e.getMessage();
                Log.e(TAG, lastError, e);
            }

            final int finalSuccess = success;
            final int finalSkipped = skipped;
            final String finalError = lastError;
            runOnUiThread(() -> {
                loading.dismiss();
                if (finalError != null) {
                    Toast.makeText(this, finalError, Toast.LENGTH_LONG).show();
                    tvResult.setText("⚠ " + finalError);
                    return;
                }
                String result = "✓ Import thành công: " + finalSuccess + " sinh viên";
                if (finalSkipped > 0) result += "\n⚠ Bỏ qua " + finalSkipped + " (MSSV đã tồn tại)";
                tvResult.setText(result);
                Log.d(TAG, "Import xong: " + finalSuccess + " inserted, " + finalSkipped + " skipped");
                Toast.makeText(this, "Import xong! " + finalSuccess + " sinh viên đã thêm.", Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private String getFilenameFromUri(Uri uri) {
        String name = "unknown";
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy tên file: " + e.getMessage(), e);
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

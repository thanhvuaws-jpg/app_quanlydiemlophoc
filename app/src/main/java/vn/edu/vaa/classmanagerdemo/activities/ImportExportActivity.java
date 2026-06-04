package vn.edu.vaa.classmanagerdemo.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.models.Score;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.CsvExporter;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class ImportExportActivity extends AppCompatActivity {
    private TextView txtCsvContent;
    private ScoreDAO scoreDAO;
    private AppPreferenceManager prefs;
    private int currentStudentId;
    private File lastCsvFile;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleImportCsvFile(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_import_export);
        Toolbar toolbar = findViewById(R.id.toolbarImportExport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scoreDAO = new ScoreDAO(this);
        currentStudentId = prefs.getCurrentUserId();

        txtCsvContent = findViewById(R.id.txtCsvContent);

        Button btnExport = findViewById(R.id.btnExportCsv);
        Button btnShare = findViewById(R.id.btnShareCsv);
        Button btnImportFile = findViewById(R.id.btnImportCsvFile);
        Button btnImportText = findViewById(R.id.btnImportCsvText);

        btnExport.setOnClickListener(v -> handleExportCsv());
        btnShare.setOnClickListener(v -> handleShareCsv());
        btnImportFile.setOnClickListener(v -> openFilePicker());
        btnImportText.setOnClickListener(v -> showImportTextDialog());
    }

    private void handleExportCsv() {
        try {
            List<Score> scores = scoreDAO.getByStudentId(currentStudentId);
            if (scores.isEmpty()) {
                Toast.makeText(this, "Chưa có điểm môn học nào để xuất", Toast.LENGTH_SHORT).show();
                return;
            }
            lastCsvFile = CsvExporter.exportScores(this, scores);
            String content = CsvExporter.readCsv(lastCsvFile);
            txtCsvContent.setText(content);
            Toast.makeText(this, "Xuất thành công: " + lastCsvFile.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xuất CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleShareCsv() {
        if (lastCsvFile == null || !lastCsvFile.exists()) {
            handleExportCsv();
        }
        if (lastCsvFile != null && lastCsvFile.exists()) {
            startActivity(CsvExporter.buildShareIntent(this, lastCsvFile));
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Chọn file CSV điểm"));
    }

    private void handleImportCsvFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) throw new Exception("Không thể mở file");
            File tempFile = new File(getCacheDir(), "temp_import.csv");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
            }
            is.close();

            List<Score> scores = CsvExporter.importScores(tempFile, currentStudentId);
            int count = 0;
            for (Score s : scores) {
                scoreDAO.insert(s);
                count++;
            }

            String preview = CsvExporter.readCsv(tempFile);
            txtCsvContent.setText("Nhập thành công " + count + " môn học!\n\nNội dung file:\n" + preview);
            Toast.makeText(this, "Nhập thành công " + count + " môn học!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nhập file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImportTextDialog() {
        EditText input = new EditText(this);
        input.setHint("Học kỳ,Tên môn học,Số tín chỉ,Điểm quá trình,Tỷ lệ QT,Điểm cuối kỳ,Tỷ lệ CK\nHọc kỳ 1,Toán Cao Cấp A1,3,8.5,30,9.0,70");
        input.setMinLines(6);
        input.setGravity(android.view.Gravity.TOP);

        new AlertDialog.Builder(this)
                .setTitle("Dán văn bản CSV")
                .setMessage("Hãy nhập hoặc dán nội dung CSV điểm. Dòng đầu là tiêu đề cột.")
                .setView(input)
                .setPositiveButton("Nhập điểm", (dialog, which) -> {
                    String csvText = input.getText().toString().trim();
                    if (!csvText.isEmpty()) {
                        handleImportCsvText(csvText);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleImportCsvText(String text) {
        try {
            File tempFile = new File(getCacheDir(), "temp_text_import.csv");
            try (PrintWriter pw = new PrintWriter(tempFile, "UTF-8")) {
                pw.print(text);
            }
            List<Score> scores = CsvExporter.importScores(tempFile, currentStudentId);
            int count = 0;
            for (Score s : scores) {
                scoreDAO.insert(s);
                count++;
            }
            txtCsvContent.setText("Nhập thành công " + count + " môn học từ văn bản!\n\nNội dung:\n" + text);
            Toast.makeText(this, "Nhập thành công " + count + " môn học!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi nhập văn bản: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavigationHelper.finishWithSlide(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

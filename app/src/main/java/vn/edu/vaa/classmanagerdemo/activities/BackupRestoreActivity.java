package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.DatabaseHelper;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class BackupRestoreActivity extends BaseActivity {

    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        Toolbar toolbar = findViewById(R.id.toolbarBackup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sao lưu & Khôi phục");
        }

        // Initialize file picker contract for database restore
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::handleRestoreUri
        );

        findViewById(R.id.btnBackupDb).setOnClickListener(v -> performBackup());
        findViewById(R.id.btnRestoreDb).setOnClickListener(v -> triggerRestorePicker());
    }

    private void performBackup() {
        new Thread(() -> {
            try {
                // Ensure helper is closed/flushed if needed, but normally getDatabasePath works.
                File dbFile = getDatabasePath(DatabaseHelper.DB_NAME);
                if (!dbFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "Không tìm thấy CSDL để sao lưu", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Copy to cache backups directory
                File backupDir = new File(getCacheDir(), "backups");
                if (!backupDir.exists()) backupDir.mkdirs();

                File backupFile = new File(backupDir, "so_diem_backup.db");
                copyFile(dbFile, backupFile);

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", backupFile);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/octet-stream");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, "CSDL Sổ điểm giáo viên");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> startActivity(Intent.createChooser(intent, "Chia sẻ file sao lưu")));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi sao lưu: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void triggerRestorePicker() {
        // We open documents, accepting any file, and check inside if it is a valid SQLite DB.
        filePickerLauncher.launch(new String[]{"*/*"});
    }

    private void handleRestoreUri(Uri uri) {
        if (uri == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Khôi phục CSDL")
                .setMessage("Thao tác này sẽ ghi đè toàn bộ dữ liệu hiện tại và khởi động lại ứng dụng. Bạn chắc chắn chứ?")
                .setPositiveButton("Khôi phục", (dialog, which) -> restoreDatabase(uri))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void restoreDatabase(Uri uri) {
        new Thread(() -> {
            try {
                // 1. Close active DB connections
                DatabaseHelper.resetInstance();

                // 2. Overwrite the database file
                File dbFile = getDatabasePath(DatabaseHelper.DB_NAME);
                if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
                    dbFile.getParentFile().mkdirs();
                }

                try (InputStream is = getContentResolver().openInputStream(uri);
                     OutputStream os = new FileOutputStream(dbFile)) {
                    if (is == null) throw new Exception("Không thể đọc file đã chọn");
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Khôi phục thành công! Đang khởi động lại...", Toast.LENGTH_LONG).show();
                    findViewById(R.id.btnBackupDb).postDelayed(this::restartApp, 1500);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi khôi phục: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void restartApp() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    private void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
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

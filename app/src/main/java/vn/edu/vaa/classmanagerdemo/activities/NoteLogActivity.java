package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.TextFileManager;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class NoteLogActivity extends AppCompatActivity {
    private static final String NOTE_FILE = "note.txt";
    private EditText edtNote;
    private TextView txtFileContent, txtExplanation;
    private ActionLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferenceManager prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_note_log);
        logger = new ActionLogger(this);
        initViews();
        initListeners();
        txtExplanation.setText("Chức năng ghi chú và nhật ký thao tác dùng file text:\n" +
                "- MODE_PRIVATE: ghi đè note.txt.\n" +
                "- MODE_APPEND: nối thêm actions.log.\n" +
                "- UTF-8: tránh lỗi tiếng Việt.\n\n" +
                "Hãy ghi ghi chú, đọc lại, sau đó ghi log và đọc toàn bộ log.");
        NavigationHelper.setupBottomNavigation(this, R.id.nav_logs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_logs);
    }

    private void initViews() {
        edtNote = findViewById(R.id.edtNote);
        txtFileContent = findViewById(R.id.txtFileContent);
        txtExplanation = findViewById(R.id.txtExplanation);
    }

    private void initListeners() {
        Button btnWrite = findViewById(R.id.btnWriteNote);
        Button btnRead = findViewById(R.id.btnReadNote);
        Button btnAppendLog = findViewById(R.id.btnAppendLog);
        Button btnReadLog = findViewById(R.id.btnReadLog);
        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnWrite.setOnClickListener(v -> handleWriteNote());
        btnRead.setOnClickListener(v -> handleReadNote());
        btnAppendLog.setOnClickListener(v -> handleAppendLog());
        btnReadLog.setOnClickListener(v -> handleReadLog());
        btnClearLog.setOnClickListener(v -> handleClearLog());
    }

    private void handleWriteNote() {
        String content = edtNote.getText().toString().trim();
        if (content.isEmpty()) {
            edtNote.setError("Nội dung ghi chú không được rỗng");
            return;
        }
        try {
            TextFileManager.writeText(this, NOTE_FILE, content);
            logger.log("Write note.txt bằng MODE_PRIVATE");
            Toast.makeText(this, "Đã ghi note.txt", Toast.LENGTH_SHORT).show();
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Ghi đè note.txt\"",
                    "noteContent = nội dung EditText.",
                    "Nội dung không được rỗng.",
                    "Mở openFileOutput(\"note.txt\", MODE_PRIVATE), ghi chuỗi UTF-8, tự đóng stream.",
                    "File Internal Storage: note.txt. MODE_PRIVATE ghi đè nội dung cũ.",
                    "File note.txt được tạo hoặc cập nhật; mở lại app vẫn đọc được."
            ));
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi ghi file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleReadNote() {
        try {
            String text = TextFileManager.readText(this, NOTE_FILE);
            txtFileContent.setText(text);
            logger.log("Read note.txt");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Đọc note.txt\"",
                    "Không lấy dữ liệu từ form.",
                    "Nếu file chưa tồn tại sẽ phát sinh IOException và app báo lỗi.",
                    "Mở openFileInput(\"note.txt\"), đọc từng dòng bằng BufferedReader UTF-8.",
                    "File Internal Storage: note.txt.",
                    "Nội dung file được hiển thị ở TextView."
            ));
        } catch (IOException e) {
            txtFileContent.setText("(Chưa có note.txt)");
            Toast.makeText(this, "Chưa có note.txt", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAppendLog() {
        logger.log("Người dùng click nút Ghi log thao tác");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Ghi log thao tác\"",
                "Tạo dữ liệu log gồm thời gian hiện tại và mô tả hành động.",
                "Không cần validate.",
                "Mở actions.log bằng MODE_APPEND và ghi thêm một dòng mới.",
                "File Internal Storage: actions.log. MODE_APPEND không xóa nội dung cũ.",
                "File log có thêm một dòng lịch sử thao tác."
        ));
        Toast.makeText(this, "Đã ghi log", Toast.LENGTH_SHORT).show();
    }

    private void handleReadLog() {
        String log = logger.readLog();
        txtFileContent.setText(log);
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Đọc actions.log\"",
                "Không lấy dữ liệu từ form.",
                "Nếu file chưa tồn tại thì hiển thị thông báo mặc định.",
                "Đọc nội dung actions.log bằng TextFileManager.readText().",
                "File Internal Storage: actions.log.",
                "Toàn bộ lịch sử thao tác được hiển thị."
        ));
    }

    private void handleClearLog() {
        logger.clearLog();
        txtFileContent.setText("(Đã xóa log)");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Xóa actions.log\"",
                "Không lấy dữ liệu từ form.",
                "Không cần validate.",
                "Gọi deleteFile(\"actions.log\") trong ActionLogger.clearLog().",
                "Xóa file log trong Internal Storage.",
                "Lịch sử thao tác bị xóa."
        ));
    }
}

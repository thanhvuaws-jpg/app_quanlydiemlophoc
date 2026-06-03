package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.StudentAdapter;
import vn.edu.vaa.classmanagerdemo.database.StudentDAO;
import vn.edu.vaa.classmanagerdemo.models.Student;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.CsvExporter;
import vn.edu.vaa.classmanagerdemo.storage.XmlStudentParser;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class ImportExportActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView txtCsvContent, txtExplanation;
    private StudentDAO dao;
    private ActionLogger logger;
    private final List<Student> students = new ArrayList<>();
    private StudentAdapter adapter;
    private File lastCsvFile;

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
        
        setContentView(R.layout.activity_import_export);
        Toolbar toolbar = findViewById(R.id.toolbarImportExport);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dao = new StudentDAO(this);
        logger = new ActionLogger(this);
        initViews();
        initRecyclerView();
        initListeners();
        loadStudentsFromDb();
        txtExplanation.setText("Chức năng nhập/xuất dữ liệu dùng XML và CSV:\n" +
                "- Import XML bằng XmlPullParser từ res/raw/students_sample.xml.\n" +
                "- Insert dữ liệu mẫu vào SQLite.\n" +
                "- Export danh sách sinh viên ra CSV trong app-specific external storage.\n" +
                "- Chia sẻ CSV bằng FileProvider.");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerImportStudents);
        txtCsvContent = findViewById(R.id.txtCsvContent);
        txtExplanation = findViewById(R.id.txtExplanation);
    }

    private void initRecyclerView() {
        adapter = new StudentAdapter(students, new StudentAdapter.OnStudentClickListener() {
            @Override public void onStudentClick(Student student, int position) { }
            @Override public void onStudentLongClick(Student student, int position) { }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initListeners() {
        Button btnImport = findViewById(R.id.btnImportXml);
        Button btnExport = findViewById(R.id.btnExportCsv);
        Button btnShare = findViewById(R.id.btnShareCsv);
        Button btnView = findViewById(R.id.btnViewCsv);
        btnImport.setOnClickListener(v -> handleImportXml());
        btnExport.setOnClickListener(v -> handleExportCsv());
        btnShare.setOnClickListener(v -> handleShareCsv());
        btnView.setOnClickListener(v -> handleViewCsv());
    }

    private void handleImportXml() {
        try {
            List<Student> imported = XmlStudentParser.parseSample(this);
            int insertCount = 0, skipCount = 0;
            for (Student s : imported) {
                if (!dao.existsByNameAndClass(s.getName(), s.getClassName())) {
                    dao.insert(new Student(s.getName(), s.getClassName(), s.getEmail(), s.getPhone()));
                    insertCount++;
                } else {
                    skipCount++;
                }
            }
            loadStudentsFromDb();
            logger.log("Import XML: " + insertCount + " inserted, " + skipCount + " skipped");
            String msg = "Đã import " + insertCount + " sinh viên từ students_sample.xml";
            if (skipCount > 0) msg += "\n⚠ Bỏ qua " + skipCount + " (đã tồn tại)";
            txtCsvContent.setText(msg);
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Import sinh viên từ XML mẫu\"",
                    "Nguồn dữ liệu là file res/raw/students_sample.xml.",
                    "Parser kiểm tra các tag student, id, name, className, email, phone.",
                    "Kiểm tra trùng bằng existsByNameAndClass() trước khi insert — tránh duplicate khi nhấn nhiều lần.",
                    "XML nằm trong res/raw; dữ liệu sau import được ghi vào SQLite bảng students.",
                    "RecyclerView hiển thị danh sách; log ghi lại số inserted và skipped."
            ));
        } catch (Exception e) {
            Toast.makeText(this, "Import XML lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleExportCsv() {
        try {
            List<Student> list = dao.getAll();
            lastCsvFile = CsvExporter.exportStudents(this, list);
            logger.log("Export CSV: " + lastCsvFile.getAbsolutePath());
            txtCsvContent.setText("Đã export file:\n" + lastCsvFile.getAbsolutePath());
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Export sinh viên ra CSV\"",
                    "Lấy danh sách sinh viên hiện có từ SQLite bằng StudentDAO.getAll().",
                    "Nếu danh sách rỗng vẫn tạo CSV chỉ có dòng header.",
                    "Tạo nội dung CSV gồm header và từng dòng sinh viên; ghi bằng UTF-8 có BOM.",
                    "File lưu ở app-specific external storage, thư mục Documents của app.",
                    "File students_export.csv được tạo và có thể chia sẻ qua nút Chia sẻ file CSV."
            ));
        } catch (Exception e) {
            Toast.makeText(this, "Export CSV lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleShareCsv() {
        if (lastCsvFile == null || !lastCsvFile.exists()) {
            Toast.makeText(this, "Hãy export CSV trước", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = CsvExporter.buildShareIntent(this, lastCsvFile);
        startActivity(intent);
        logger.log("Share CSV");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Chia sẻ file CSV\"",
                "Lấy file CSV vừa export: students_export.csv.",
                "Kiểm tra file có tồn tại hay chưa.",
                "Dùng FileProvider tạo content Uri, tạo Intent ACTION_SEND, cấp FLAG_GRANT_READ_URI_PERMISSION.",
                "File nằm trong app-specific external storage, được chia sẻ tạm qua FileProvider.",
                "Android mở hộp thoại chọn app nhận file như Gmail, Drive, Zalo nếu có."
        ));
    }

    private void handleViewCsv() {
        try {
            if (lastCsvFile == null || !lastCsvFile.exists()) {
                Toast.makeText(this, "Hãy export CSV trước", Toast.LENGTH_SHORT).show();
                return;
            }
            String content = CsvExporter.readCsv(lastCsvFile);
            txtCsvContent.setText(content);
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Xem nội dung CSV\"",
                    "Lấy file CSV vừa export.",
                    "Kiểm tra file tồn tại.",
                    "Đọc toàn bộ byte của file và chuyển thành String UTF-8.",
                    "File CSV trong app-specific external storage.",
                    "Nội dung CSV được hiển thị trong TextView."
            ));
        } catch (Exception e) {
            Toast.makeText(this, "Không đọc được CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void loadStudentsFromDb() {
        students.clear();
        students.addAll(dao.getAll());
        adapter.notifyDataSetChanged();
    }
}

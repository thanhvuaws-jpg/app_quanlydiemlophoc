package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.os.Bundle;
import java.util.Calendar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.TodoAdapter;
import vn.edu.vaa.classmanagerdemo.models.Todo;
import vn.edu.vaa.classmanagerdemo.storage.ActionLogger;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.storage.JsonTodoStorage;
import vn.edu.vaa.classmanagerdemo.utils.ExplanationBuilder;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class TodoActivity extends AppCompatActivity {
    private EditText edtTitle, edtDeadline;
    private CheckBox cbCompleted;
    private TextView txtJson, txtExplanation;
    private RecyclerView recyclerView;
    private final List<Todo> todos = new ArrayList<>();
    private TodoAdapter adapter;
    private int nextId = 1;
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
        
        setContentView(R.layout.activity_todo);
        logger = new ActionLogger(this);
        initViews();
        initRecyclerView();
        initListeners();
        loadJsonSilently();
        updateJsonText();
        txtExplanation.setText("Chức năng quản lý công việc học tập lưu bằng JSON trong Internal Storage.\n\n" +
                "Thêm công việc, đổi trạng thái hoàn thành, xóa/hoàn tác. Khung bên dưới giải thích file todos.json được ghi/đọc như thế nào.");
        NavigationHelper.setupBottomNavigation(this, R.id.nav_todo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_todo);
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtDeadline = findViewById(R.id.edtDeadline);
        cbCompleted = findViewById(R.id.cbCompleted);
        txtJson = findViewById(R.id.txtJson);
        txtExplanation = findViewById(R.id.txtExplanation);
        recyclerView = findViewById(R.id.recyclerTodos);
    }

    private void initRecyclerView() {
        adapter = new TodoAdapter(todos, new TodoAdapter.OnTodoClickListener() {
            @Override
            public void onTodoClick(Todo todo, int position) {
                todo.setCompleted(!todo.isCompleted());
                adapter.notifyItemChanged(position);
                saveJsonSilently();
                updateJsonText();
                logger.log("Toggle Todo id=" + todo.getId());
                txtExplanation.setText(ExplanationBuilder.build(
                        "Click vào item Todo trong RecyclerView",
                        "Lấy Todo tại vị trí click: id, title, deadline, completed.",
                        "Không cần validate vì dữ liệu đã tồn tại trong danh sách.",
                        "Đảo completed: true ↔ false; cập nhật item; lưu lại toàn bộ list xuống JSON.",
                        "File Internal Storage: todos.json.",
                        "Todo được đánh dấu hoàn thành/chưa hoàn thành; TextView JSON hiển thị nội dung mới."
                ));
            }

            @Override
            public void onTodoLongClick(Todo todo, int position) {
                Todo removed = todos.remove(position);
                adapter.notifyItemRemoved(position);
                saveJsonSilently();
                updateJsonText();
                logger.log("Delete Todo id=" + removed.getId());
                Snackbar.make(recyclerView, "Đã xóa: " + removed.getTitle(), Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", v -> {
                            todos.add(position, removed);
                            adapter.notifyItemInserted(position);
                            saveJsonSilently();
                            updateJsonText();
                        }).show();
                txtExplanation.setText(ExplanationBuilder.build(
                        "Long click vào item Todo",
                        "Lấy Todo tại vị trí được nhấn giữ.",
                        "Không xóa ngay khỏi file trước khi cập nhật list; thao tác có Snackbar Hoàn tác.",
                        "Xóa Todo khỏi ArrayList, notifyItemRemoved(), ghi lại toàn bộ List<Todo> xuống JSON.",
                        "File Internal Storage: todos.json được ghi đè bằng danh sách mới.",
                        "Todo biến mất khỏi RecyclerView; người dùng có thể nhấn Hoàn tác để thêm lại."
                ));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initListeners() {
        Button btnAdd = findViewById(R.id.btnAddTodo);
        Button btnLoad = findViewById(R.id.btnLoadJson);
        Button btnDelete = findViewById(R.id.btnDeleteJson);
        btnAdd.setOnClickListener(v -> handleAddTodo());
        btnLoad.setOnClickListener(v -> handleLoadJson());
        btnDelete.setOnClickListener(v -> handleDeleteJson());
        // DatePicker cho field hạn nộp
        edtDeadline.setFocusable(false);
        edtDeadline.setClickable(true);
        edtDeadline.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                edtDeadline.setText(String.format("%02d/%02d/%d", day, month + 1, year));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void handleAddTodo() {
        String title = edtTitle.getText().toString().trim();
        String deadline = edtDeadline.getText().toString().trim();
        boolean completed = cbCompleted.isChecked();
        if (title.isEmpty()) {
            edtTitle.setError("Tên công việc không được rỗng");
            return;
        }
        Todo todo = new Todo(nextId++, title, deadline, completed);
        todos.add(todo);
        adapter.notifyItemInserted(todos.size() - 1);
        saveJsonSilently();
        updateJsonText();
        edtTitle.setText("");
        edtDeadline.setText("");
        cbCompleted.setChecked(false);
        logger.log("Add Todo: " + title);
        Toast.makeText(this, "Đã thêm và lưu JSON", Toast.LENGTH_SHORT).show();
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Thêm công việc và lưu JSON\"",
                "title từ EditText; deadline từ EditText; completed từ CheckBox.",
                "title không được rỗng. Deadline có thể để trống trong demo này.",
                "Tạo object Todo, thêm vào ArrayList, notifyItemInserted(), chuyển List<Todo> thành JSONArray.",
                "Ghi đè file todos.json trong Internal Storage bằng JsonTodoStorage.save().",
                "RecyclerView thêm item mới; TextView hiển thị JSON hiện tại; log ghi lại thao tác."
        ));
    }

    private void handleLoadJson() {
        try {
            todos.clear();
            todos.addAll(JsonTodoStorage.load(this));
            updateNextId();
            adapter.notifyDataSetChanged();
            updateJsonText();
            logger.log("Load todos.json");
            txtExplanation.setText(ExplanationBuilder.build(
                    "Click nút \"Đọc JSON\"",
                    "Không lấy dữ liệu từ form; app đọc nội dung file todos.json.",
                    "Nếu file chưa tồn tại hoặc JSON lỗi, app bắt exception và báo thông báo.",
                    "Đọc String từ file, parse JSONArray thành List<Todo>, gán lại cho RecyclerView.",
                    "File Internal Storage: todos.json.",
                    "Danh sách công việc được khôi phục từ file."
            ));
        } catch (Exception e) {
            Toast.makeText(this, "Không đọc được JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleDeleteJson() {
        boolean deleted = deleteFile(JsonTodoStorage.FILE_NAME);
        todos.clear();
        adapter.notifyDataSetChanged();
        updateJsonText();
        logger.log("Delete todos.json");
        txtExplanation.setText(ExplanationBuilder.build(
                "Click nút \"Xóa file todos.json\"",
                "Không lấy dữ liệu từ form.",
                "Không cần validate.",
                "Gọi deleteFile(\"todos.json\"), xóa list trên màn hình.",
                "Xóa file JSON trong Internal Storage.",
                deleted ? "File đã được xóa." : "File chưa tồn tại hoặc xóa thất bại."
        ));
    }

    private void saveJsonSilently() {
        try { JsonTodoStorage.save(this, todos); } catch (IOException | JSONException e) { e.printStackTrace(); }
    }

    private void loadJsonSilently() {
        try {
            todos.clear();
            todos.addAll(JsonTodoStorage.load(this));
            updateNextId();
            adapter.notifyDataSetChanged();
        } catch (Exception ignored) { }
    }

    private void updateNextId() {
        int max = 0;
        for (Todo t : todos) if (t.getId() > max) max = t.getId();
        nextId = max + 1;
    }

    private void updateJsonText() {
        txtJson.setText(JsonTodoStorage.readRawJson(this));
    }
}

package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.adapters.ClassAdapter;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;

public class ClassListActivity extends AppCompatActivity {

    private ClassDAO dao;
    private ClassAdapter adapter;
    private final List<ClassRoom> classes = new ArrayList<>();
    private TextView tvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new AppPreferenceManager(this).isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_class_list);

        Toolbar toolbar = findViewById(R.id.toolbarClassList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dao = new ClassDAO(this);
        tvStats = findViewById(R.id.tvClassStats);

        RecyclerView rv = findViewById(R.id.recyclerClasses);
        adapter = new ClassAdapter(classes, new ClassAdapter.OnClassClickListener() {
            @Override
            public void onClassClick(ClassRoom cr) {
                Intent i = new Intent(ClassListActivity.this, ClassDetailActivity.class);
                i.putExtra("classId", cr.getId());
                i.putExtra("className", cr.getName());
                startActivity(i);
            }
            @Override
            public void onClassLongClick(ClassRoom cr) {
                showDeleteDialog(cr);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearchClass);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadClasses(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        FloatingActionButton fab = findViewById(R.id.fabAddClass);
        fab.setOnClickListener(v -> showAddDialog(null));

        loadClasses("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses("");
    }

    private void loadClasses(String keyword) {
        classes.clear();
        List<ClassRoom> all = dao.getAll();
        for (ClassRoom cr : all) {
            if (keyword.isEmpty() || cr.getName().toLowerCase().contains(keyword.toLowerCase())) {
                classes.add(cr);
            }
        }
        adapter.notifyDataSetChanged();
        int total = 0;
        for (ClassRoom cr : all) total += cr.getStudentCount();
        tvStats.setText(all.size() + " lớp  •  " + total + " sinh viên");
    }

    private void showAddDialog(ClassRoom existing) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_class, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etDialogClassName);
        TextInputEditText etYear = dialogView.findViewById(R.id.etDialogClassYear);
        if (existing != null) {
            etName.setText(existing.getName());
            etYear.setText(existing.getSchoolYear());
        }
        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm lớp mới" : "Sửa lớp")
                .setView(dialogView)
                .setPositiveButton(existing == null ? "Thêm" : "Lưu", (d, w) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String year = etYear.getText() != null ? etYear.getText().toString().trim() : "";
                    if (name.isEmpty()) { Toast.makeText(this, "Tên lớp không được rỗng", Toast.LENGTH_SHORT).show(); return; }
                    if (existing == null) {
                        dao.insert(new ClassRoom(name, year));
                        Toast.makeText(this, "Đã thêm lớp " + name, Toast.LENGTH_SHORT).show();
                    } else {
                        existing.setName(name);
                        existing.setSchoolYear(year);
                        dao.update(existing);
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                    }
                    loadClasses("");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(ClassRoom cr) {
        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn")
                .setItems(new String[]{"Sửa lớp", "Xóa lớp"}, (d, which) -> {
                    if (which == 0) showAddDialog(cr);
                    else confirmDelete(cr);
                }).show();
    }

    private void confirmDelete(ClassRoom cr) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lớp")
                .setMessage("Xóa lớp \"" + cr.getName() + "\"? Sinh viên trong lớp sẽ bị mất liên kết.")
                .setPositiveButton("Xóa", (d, w) -> {
                    dao.delete(cr.getId());
                    loadClasses("");
                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null).show();
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

package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ClassDAO;
import vn.edu.vaa.classmanagerdemo.models.SchoolClass;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;
import vn.edu.vaa.classmanagerdemo.utils.DebounceClickListener;
import vn.edu.vaa.classmanagerdemo.adapters.ClassAdapter;

public class ClassListActivity extends BaseActivity {

    private ClassDAO classDAO;
    private AppPreferenceManager prefs;
    private RecyclerView recyclerClasses;
    private List<SchoolClass> classList = new ArrayList<>();
    private ClassAdapter classAdapter;
    private android.widget.ProgressBar progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new AppPreferenceManager(this);
        if (!prefs.isLoggedIn()) { goLogin(); return; }
        setContentView(R.layout.activity_class_list);

        Toolbar toolbar = findViewById(R.id.toolbarClasses);
        setSupportActionBar(toolbar);

        classDAO = new ClassDAO(this);
        recyclerClasses = findViewById(R.id.recyclerClasses);
        progressLoading = findViewById(R.id.progressLoading);

        classAdapter = new ClassAdapter(classList,
            cls -> {
                Intent intent = new Intent(this, StudentListActivity.class);
                intent.putExtra("class_id", cls.getId());
                intent.putExtra("class_name", cls.getClassName());
                intent.putExtra("class_subject", cls.getSubject());
                startActivity(intent);
            },
            cls -> showEditClassDialog(cls),
            cls -> confirmDeleteClass(cls)
        );
        recyclerClasses.setLayoutManager(new LinearLayoutManager(this));
        recyclerClasses.setAdapter(classAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAddClass);
        fab.setOnClickListener(DebounceClickListener.wrap(v -> showAddClassDialog()));

        NavigationHelper.setupBottomNavigation(this, R.id.nav_grades);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_grades);
    }

    private void loadClasses() {
        if (progressLoading != null) progressLoading.setVisibility(android.view.View.VISIBLE);
        new Thread(() -> {
            List<SchoolClass> list = classDAO.getByTeacherId(prefs.getCurrentUserId());
            runOnUiThread(() -> {
                if (progressLoading != null) progressLoading.setVisibility(android.view.View.GONE);
                classList.clear();
                classList.addAll(list);
                classAdapter.notifyDataSetChanged();
                android.view.View layoutEmpty = findViewById(R.id.layoutEmpty);
                if (layoutEmpty != null) {
                    layoutEmpty.setVisibility(list.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    recyclerClasses.setVisibility(list.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
                }
            });
        }).start();
    }

    private void showAddClassDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_class, null);
        EditText edtClassName = view.findViewById(R.id.edtClassName);
        EditText edtSubject = view.findViewById(R.id.edtSubject);
        AutoCompleteTextView actvYear = view.findViewById(R.id.actvSchoolYear);
        EditText edtDeadline = view.findViewById(R.id.edtDeadline);

        setupDatePicker(edtDeadline);

        String[] years = {"2024-2025", "2025-2026", "2026-2027"};
        actvYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
        actvYear.setText(years[0], false);

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(view);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) tvTitle.setText("Thêm lớp học mới");
        
        view.findViewById(R.id.btnSaveClass).setOnClickListener(DebounceClickListener.wrap(v -> {
                String name = edtClassName.getText().toString().trim();
                String subject = edtSubject.getText().toString().trim();
                String year = actvYear.getText() != null ? actvYear.getText().toString().trim() : years[0];
                String deadline = edtDeadline.getText().toString().trim();
                if (name.isEmpty()) { Toast.makeText(this, "Nhập tên lớp", Toast.LENGTH_SHORT).show(); return; }
                if (subject.isEmpty()) { Toast.makeText(this, "Nhập môn học", Toast.LENGTH_SHORT).show(); return; }
                SchoolClass cls = new SchoolClass(prefs.getCurrentUserId(), name, subject, year, deadline);
                long newId = classDAO.insert(cls);
                if (newId != -1) {
                    cls.setId((int) newId);
                    vn.edu.vaa.classmanagerdemo.utils.DeadlineScheduler.scheduleAlarm(this, cls);
                }
                loadClasses();
                dialog.dismiss();
                Toast.makeText(this, "Đã thêm lớp " + name, Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void showEditClassDialog(SchoolClass cls) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_add_class, null);
        EditText edtClassName = view.findViewById(R.id.edtClassName);
        EditText edtSubject = view.findViewById(R.id.edtSubject);
        AutoCompleteTextView actvYear = view.findViewById(R.id.actvSchoolYear);
        EditText edtDeadline = view.findViewById(R.id.edtDeadline);

        edtClassName.setText(cls.getClassName());
        edtSubject.setText(cls.getSubject());
        String[] years = {"2024-2025", "2025-2026", "2026-2027"};
        actvYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, years));
        actvYear.setText(cls.getSchoolYear(), false);
        edtDeadline.setText(cls.getDeadline());

        setupDatePicker(edtDeadline);

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        dialog.setContentView(view);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) tvTitle.setText("Chỉnh sửa lớp học");

        view.findViewById(R.id.btnSaveClass).setOnClickListener(DebounceClickListener.wrap(v -> {
                cls.setClassName(edtClassName.getText().toString().trim());
                cls.setSubject(edtSubject.getText().toString().trim());
                if (actvYear.getText() != null) cls.setSchoolYear(actvYear.getText().toString().trim());
                cls.setDeadline(edtDeadline.getText().toString().trim());
                classDAO.update(cls);
                vn.edu.vaa.classmanagerdemo.utils.DeadlineScheduler.scheduleAlarm(this, cls);
                loadClasses();
                dialog.dismiss();
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void confirmDeleteClass(SchoolClass cls) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa lớp học")
            .setMessage("Xóa lớp \"" + cls.getClassName() + "\" sẽ xóa toàn bộ học sinh và điểm. Tiếp tục?")
            .setPositiveButton("Xóa", (d, w) -> {
                classDAO.deleteById(cls.getId());
                vn.edu.vaa.classmanagerdemo.utils.DeadlineScheduler.cancelAlarm(this, cls.getId());
                loadClasses();
                Toast.makeText(this, "Đã xóa lớp " + cls.getClassName(), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void setupDatePicker(EditText edt) {
        edt.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String existing = edt.getText().toString().trim();
            if (!existing.isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                    java.util.Date d = sdf.parse(existing);
                    if (d != null) cal.setTime(d);
                } catch (Exception ignored) {}
            }
            int year = cal.get(java.util.Calendar.YEAR);
            int month = cal.get(java.util.Calendar.MONTH);
            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(this, (view, y, m, d) -> {
                String dateStr = String.format(java.util.Locale.US, "%d-%02d-%02d", y, m + 1, d);
                edt.setText(dateStr);
            }, year, month, day);
            dpd.show();
        });
    }
}

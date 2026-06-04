package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.widget.Toast;

import vn.edu.vaa.classmanagerdemo.R;

public class ImportExportActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        // TODO: Implement import/export functionality for Teacher Gradebook
        // The previous implementation was for the individual GPA tracker.
        
        findViewById(R.id.btnExportCsv).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang được phát triển...", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btnImportCsvFile).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang được phát triển...", Toast.LENGTH_SHORT).show();
        });
    }
}

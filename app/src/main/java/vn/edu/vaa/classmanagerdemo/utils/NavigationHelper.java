package vn.edu.vaa.classmanagerdemo.utils;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.activities.MainActivity;
import vn.edu.vaa.classmanagerdemo.activities.StudentActivity;
import vn.edu.vaa.classmanagerdemo.activities.TodoActivity;
import vn.edu.vaa.classmanagerdemo.activities.NoteLogActivity;
import vn.edu.vaa.classmanagerdemo.activities.SettingsActivity;

public class NavigationHelper {
    public static void setupBottomNavigation(AppCompatActivity activity, int currentTabId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;
        
        // Highlight the current active tab
        bottomNavigation.setSelectedItemId(currentTabId);
        
        // Set listener for tab switches
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentTabId) return true; // Already on this screen
            
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_students) {
                intent = new Intent(activity, StudentActivity.class);
            } else if (itemId == R.id.nav_todo) {
                intent = new Intent(activity, TodoActivity.class);
            } else if (itemId == R.id.nav_logs) {
                intent = new Intent(activity, NoteLogActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            }
            
            if (intent != null) {
                // Reorder activities to front to avoid creating duplicates and retain their state
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0); // Remove animation for a tab-like feel
                return true;
            }
            return false;
        });
    }
}

package vn.edu.vaa.classmanagerdemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.activities.MainActivity;
import vn.edu.vaa.classmanagerdemo.activities.NoteLogActivity;
import vn.edu.vaa.classmanagerdemo.activities.SettingsActivity;
import vn.edu.vaa.classmanagerdemo.activities.StudentActivity;
import vn.edu.vaa.classmanagerdemo.activities.TodoActivity;

public class NavigationHelper {
    private static long lastNavClickTime = 0;
    private static final long NAV_DEBOUNCE_MS = 600;

    public static void setupBottomNavigation(AppCompatActivity activity, int currentTabId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(currentTabId);

        bottomNavigation.setOnItemSelectedListener(item -> {
            long now = SystemClock.elapsedRealtime();
            if (now - lastNavClickTime < NAV_DEBOUNCE_MS) return true;
            lastNavClickTime = now;

            int itemId = item.getItemId();
            if (itemId == currentTabId) return true;

            Intent intent = null;
            if (itemId == R.id.nav_home)     intent = new Intent(activity, MainActivity.class);
            else if (itemId == R.id.nav_students) intent = new Intent(activity, StudentActivity.class);
            else if (itemId == R.id.nav_todo)    intent = new Intent(activity, TodoActivity.class);
            else if (itemId == R.id.nav_logs)    intent = new Intent(activity, NoteLogActivity.class);
            else if (itemId == R.id.nav_settings) intent = new Intent(activity, SettingsActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                // Bottom nav tabs: fade mượt thay vì bặt đột ngột
                applyFadeTransition(activity);
                return true;
            }
            return false;
        });
    }

    // Dùng cho push screen (mở màn hình con: ClassList → ClassDetail, v.v.)
    public static void navigateTo(Activity from, Intent intent) {
        from.startActivity(intent);
        applySlideInTransition(from);
    }

    // Gọi trong onBackPressed / finish() của sub-screen để slide ngược lại
    public static void finishWithSlide(Activity activity) {
        activity.finish();
        applySlideOutTransition(activity);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private static void applyFadeTransition(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.fade_in, R.anim.fade_out);
        } else {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private static void applySlideInTransition(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private static void applySlideOutTransition(Activity activity) {
        if (Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}

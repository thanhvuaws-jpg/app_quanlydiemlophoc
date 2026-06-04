package vn.edu.vaa.classmanagerdemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.activities.MainActivity;
import vn.edu.vaa.classmanagerdemo.activities.GradeActivity;
import vn.edu.vaa.classmanagerdemo.activities.SettingsActivity;

public class NavigationHelper {
    private static long lastNavClickTime = 0;
    private static final long NAV_DEBOUNCE_MS = 300;

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
            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_grades) {
                intent = new Intent(activity, GradeActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                if (Build.VERSION.SDK_INT >= 34) {
                    activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
                    activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
                } else {
                    activity.overridePendingTransition(0, 0);
                }
                return true;
            }
            return false;
        });
    }

    public static void navigateTo(Activity from, Intent intent) {
        from.startActivity(intent);
        applySlideInTransition(from);
    }

    public static void finishWithSlide(Activity activity) {
        activity.finish();
        applySlideOutTransition(activity);
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

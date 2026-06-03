package vn.edu.vaa.classmanagerdemo;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppPreferenceManager prefs = new AppPreferenceManager(this);

        // Đọc darkMode TRƯỚC khi clearLoginSession (isDarkMode() cần username còn trong prefs)
        boolean darkMode = prefs.isDarkMode();

        if (!prefs.isRememberLogin()) {
            prefs.clearLoginSession();
        }

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}

package vn.edu.vaa.classmanagerdemo;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        AppPreferenceManager prefs = new AppPreferenceManager(this);
        
        // If the user did not choose "Remember login" previously, clear their login session on new app launch
        if (!prefs.isRememberLogin()) {
            prefs.clearLoginSession();
        }
        
        // Read saved Dark Mode preference and apply it at application startup
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}

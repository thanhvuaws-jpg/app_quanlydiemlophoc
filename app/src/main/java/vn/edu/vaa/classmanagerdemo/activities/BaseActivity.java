package vn.edu.vaa.classmanagerdemo.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        AppPreferenceManager prefs = new AppPreferenceManager(newBase);
        String lang = prefs.getLanguage(); // defaults to "vi"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }
}

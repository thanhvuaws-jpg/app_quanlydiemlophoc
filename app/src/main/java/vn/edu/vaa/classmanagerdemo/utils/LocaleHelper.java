package vn.edu.vaa.classmanagerdemo.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {
    public static Context setLocale(Context context, String language) {
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        // For Android 7.0 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context = context.createConfigurationContext(config);
        } else {
            // For older Android versions
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }

        return context;
    }
}

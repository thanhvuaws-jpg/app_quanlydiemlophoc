package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;
import android.content.SharedPreferences;

import vn.edu.vaa.classmanagerdemo.models.User;

public class AppPreferenceManager {
    private static final String PREF_NAME = "APP_PREFS";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_REMEMBER = "rememberLogin";
    public static final String KEY_DARK_MODE = "darkMode";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_LOGGED_IN = "loggedIn";
    public static final String KEY_CURRENT_USER_ID = "currentUserId";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";

    private final SharedPreferences prefs;

    public AppPreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLoginSession(User user, boolean remember) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putBoolean(KEY_REMEMBER, remember)
                .putInt(KEY_CURRENT_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_PHONE, user.getPhone())
                .apply();
    }

    public void saveAppSettings(boolean darkMode, String language) {
        String username = getUsername();
        if (!username.isEmpty()) {
            prefs.edit()
                    .putBoolean(KEY_DARK_MODE + "_" + username, darkMode)
                    .putString(KEY_LANGUAGE + "_" + username, language)
                    .apply();
        } else {
            prefs.edit()
                    .putBoolean(KEY_DARK_MODE, darkMode)
                    .putString(KEY_LANGUAGE, language)
                    .apply();
        }
    }

    /**
     * @deprecated Dùng saveLoginSession() + saveAppSettings() thay thế.
     * Method này lưu darkMode vào key chung (không per-user) nên không
     * tương thích với isDarkMode() hiện tại.
     */
    @Deprecated
    public void save(String username, boolean remember, boolean darkMode, String language) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putBoolean(KEY_REMEMBER, remember)
                .putBoolean(KEY_DARK_MODE, darkMode)
                .putString(KEY_LANGUAGE, language)
                .apply();
    }

    public boolean isLoggedIn() { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public boolean isRememberLogin() { return prefs.getBoolean(KEY_REMEMBER, false); }
    public boolean shouldAutoLogin() { return isLoggedIn() && isRememberLogin() && getCurrentUserId() > 0; }
    public int getCurrentUserId() { return prefs.getInt(KEY_CURRENT_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getPhone() { return prefs.getString(KEY_PHONE, ""); }
    public boolean isDarkMode() {
        String username = getUsername();
        if (!username.isEmpty()) {
            return prefs.getBoolean(KEY_DARK_MODE + "_" + username, false);
        }
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public String getLanguage() {
        String username = getUsername();
        if (!username.isEmpty()) {
            return prefs.getString(KEY_LANGUAGE + "_" + username, "vi");
        }
        return prefs.getString(KEY_LANGUAGE, "vi");
    }

    public void clearLoginSession() {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .putBoolean(KEY_REMEMBER, false)
                .remove(KEY_CURRENT_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_FULL_NAME)
                .remove(KEY_EMAIL)
                .remove(KEY_PHONE)
                .apply();
    }

    public void clearAll() { prefs.edit().clear().apply(); }

    public void clear() { clearAll(); }
}

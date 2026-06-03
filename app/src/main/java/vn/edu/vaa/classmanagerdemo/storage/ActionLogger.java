package vn.edu.vaa.classmanagerdemo.storage;

import android.content.Context;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActionLogger {
    public static final String LOG_FILE = "actions.log";
    private final Context context;

    public ActionLogger(Context context) {
        this.context = context.getApplicationContext();
    }

    public void log(String action) {
        String time = new SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(new Date());
        String line = "[" + time + "] " + action + "\n";
        try {
            TextFileManager.appendText(context, LOG_FILE, line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLog() {
        try {
            return TextFileManager.readText(context, LOG_FILE);
        } catch (IOException e) {
            return "(Chưa có log hoặc file không tồn tại)";
        }
    }

    public void clearLog() {
        context.deleteFile(LOG_FILE);
    }
}

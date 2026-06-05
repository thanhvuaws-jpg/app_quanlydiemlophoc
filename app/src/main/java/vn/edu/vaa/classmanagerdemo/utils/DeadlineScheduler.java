package vn.edu.vaa.classmanagerdemo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.models.SchoolClass;

public class DeadlineScheduler {

    private static final String TAG = "DeadlineScheduler";

    public static void scheduleAlarm(Context context, SchoolClass sc) {
        String deadlineStr = sc.getDeadline();
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            cancelAlarm(context, sc.getId());
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(deadlineStr);
            if (date == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            // Set alarm time to 8:00 AM on the deadline day
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            long triggerTime = cal.getTimeInMillis();
            if (triggerTime <= System.currentTimeMillis()) {
                Log.d(TAG, "Deadline is in the past, not scheduling.");
                return;
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;

            Intent intent = new Intent(context, DeadlineNotificationReceiver.class);
            intent.putExtra("class_id", sc.getId());
            intent.putExtra("class_name", sc.getClassName());
            intent.putExtra("subject", sc.getSubject());

            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    sc.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
            Log.d(TAG, "Scheduled alarm for class " + sc.getClassName() + " at " + cal.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm", e);
        }
    }

    public static void cancelAlarm(Context context, int classId) {
        try {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;

            Intent intent = new Intent(context, DeadlineNotificationReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    classId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
                Log.d(TAG, "Cancelled alarm for class ID " + classId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
        }
    }
}

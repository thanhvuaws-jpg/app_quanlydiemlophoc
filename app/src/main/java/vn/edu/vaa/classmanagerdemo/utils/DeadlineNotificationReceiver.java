package vn.edu.vaa.classmanagerdemo.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.activities.LoginActivity;

public class DeadlineNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "deadline_notifications";
    private static final String CHANNEL_NAME = "Nhắc nhở hạn nộp điểm";

    @Override
    public void onReceive(Context context, Intent intent) {
        String className = intent.getStringExtra("class_name");
        String subject = intent.getStringExtra("subject");
        int classId = intent.getIntExtra("class_id", -1);

        if (className == null) className = "Lớp học";
        if (subject == null) subject = "";

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        // Channel creation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo hạn nộp điểm lớp học");
            nm.createNotificationChannel(channel);
        }

        // Tap action
        Intent tapIntent = new Intent(context, LoginActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                classId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Hạn nộp điểm sắp tới!")
                .setContentText("Lớp: " + className + (subject.isEmpty() ? "" : " - Môn: " + subject) + " cần hoàn thành điểm số.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        nm.notify(classId, builder.build());
    }
}

package vn.edu.vaa.classmanagerdemo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingHelper {
    private static final String TAG = "LoadingHelper";
    private AlertDialog dialog;

    public void show(Context context, String message) {
        if (dialog != null && dialog.isShowing()) return;
        try {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(70, 55, 70, 55);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setBackgroundResource(android.R.color.white);

            ProgressBar pb = new ProgressBar(context);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(72, 72);
            layout.addView(pb, pbParams);

            TextView tv = new TextView(context);
            tv.setText(message);
            tv.setTextSize(15f);
            tv.setPadding(36, 0, 0, 0);
            tv.setTextColor(0xFF0F172A);
            layout.addView(tv);

            dialog = new AlertDialog.Builder(context)
                    .setView(layout)
                    .setCancelable(false)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Không thể hiện loading dialog: " + e.getMessage(), e);
        }
    }

    public void show(Context context) {
        show(context, "Đang xử lý...");
    }

    public void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi dismiss loading: " + e.getMessage(), e);
        } finally {
            dialog = null;
        }
    }
}

package vn.edu.vaa.classmanagerdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.Locale;

public class GpaShareRenderer {

    public static Bitmap generateGpaCard(Context context, String studentName, String studentCode,
                                         float gpa4, float gpa10, int totalCredits, String rank) {
        int width = 800;
        int height = 1000;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 1. Draw premium background gradient (Indigo to Violet)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient bgGradient = new LinearGradient(0, 0, 0, height,
                Color.parseColor("#4F46E5"), Color.parseColor("#7C3AED"), Shader.TileMode.CLAMP);
        bgPaint.setShader(bgGradient);
        canvas.drawRect(0, 0, width, height, bgPaint);

        // Decorative background circles
        Paint decPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        decPaint.setColor(Color.parseColor("#FFFFFF"));
        decPaint.setAlpha(15); // very faint
        canvas.drawCircle(0, 0, 400, decPaint);
        canvas.drawCircle(width, height, 300, decPaint);

        // 2. Main card background (White with rounded corners)
        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(Color.WHITE);
        RectF cardRect = new RectF(50, 120, width - 50, height - 100);
        canvas.drawRoundRect(cardRect, 32, 32, cardPaint);

        // Accent top bar
        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(Color.parseColor("#4F46E5"));
        RectF accentRect = new RectF(50, 120, width - 50, 140);
        canvas.drawRect(accentRect, accentPaint);

        // 3. Header Texts
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Subtitle VAA
        textPaint.setTextSize(16);
        textPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText(context.getString(vn.edu.vaa.classmanagerdemo.R.string.vaa_name), width / 2f, 200, textPaint);

        // Main Title
        textPaint.setTextSize(32);
        textPaint.setColor(Color.parseColor("#1E293B"));
        canvas.drawText(context.getString(vn.edu.vaa.classmanagerdemo.R.string.gpa_card_title), width / 2f, 250, textPaint);

        // 4. Student info section
        Paint infoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        infoPaint.setTextSize(24);
        infoPaint.setColor(Color.parseColor("#334155"));
        // Giới hạn độ rộng tên trong card (tối đa 640px — card width 700, padding 30 mỗi bên)
        float maxNameWidth = width - 160f;
        String displayName = studentName.toUpperCase();
        // Đo độ rộng text, nếu vượt quá thì cắt bớt và thêm "..."
        while (infoPaint.measureText(displayName) > maxNameWidth && displayName.length() > 3) {
            displayName = displayName.substring(0, displayName.length() - 4) + "...";
        }
        canvas.drawText(displayName, width / 2f, 320, infoPaint);

        infoPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        infoPaint.setTextSize(16);
        infoPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText(context.getString(vn.edu.vaa.classmanagerdemo.R.string.student_id_label, studentCode), width / 2f, 355, infoPaint);

        // 5. Circle GPA display
        float circleX = width / 2f;
        float circleY = 530f;
        float circleRadius = 110f;

        // Circle stroke path
        Paint circleBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBg.setColor(Color.parseColor("#F1F5F9"));
        canvas.drawCircle(circleX, circleY, circleRadius, circleBg);

        Paint circleBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBorder.setStyle(Paint.Style.STROKE);
        circleBorder.setStrokeWidth(12);
        circleBorder.setColor(Color.parseColor("#4F46E5"));
        canvas.drawCircle(circleX, circleY, circleRadius - 6, circleBorder);

        // GPA Value inside circle
        Paint gpaValPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gpaValPaint.setTextAlign(Paint.Align.CENTER);
        gpaValPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        gpaValPaint.setTextSize(54);
        gpaValPaint.setColor(Color.parseColor("#4F46E5"));
        canvas.drawText(String.format(Locale.US, "%.2f", gpa4), circleX, circleY + 14, gpaValPaint);

        Paint gpaLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gpaLabelPaint.setTextAlign(Paint.Align.CENTER);
        gpaLabelPaint.setTextSize(13);
        gpaLabelPaint.setColor(Color.parseColor("#64748B"));
        gpaLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(context.getString(vn.edu.vaa.classmanagerdemo.R.string.gpa_cumulative_4_label), circleX, circleY + 46, gpaLabelPaint);

        // 6. Secondary stats cards (Grid of 3 cards)
        // Card dimensions
        float cardY = 690f;
        float cardW = 200f;
        float cardH = 110f;
        float gap = 25f;

        // Card 1: GPA 10
        float card1X = 50 + gap + 15;
        drawStatCard(canvas, card1X, cardY, cardW, cardH, context.getString(vn.edu.vaa.classmanagerdemo.R.string.gpa_10), String.format(Locale.US, "%.2f", gpa10), "#3B82F6");

        // Card 2: Total Credits
        float card2X = card1X + cardW + gap;
        drawStatCard(canvas, card2X, cardY, cardW, cardH, context.getString(vn.edu.vaa.classmanagerdemo.R.string.total_credits_label), totalCredits + " TC", "#10B981");

        // Card 3: Rank
        float card3X = card2X + cardW + gap;
        drawStatCard(canvas, card3X, cardY, cardW, cardH, context.getString(vn.edu.vaa.classmanagerdemo.R.string.rank_label), rank, "#F59E0B");

        // 7. Footer branding
        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setTextAlign(Paint.Align.CENTER);
        footerPaint.setTextSize(13);
        footerPaint.setColor(Color.parseColor("#94A3B8"));
        footerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText(context.getString(vn.edu.vaa.classmanagerdemo.R.string.gpa_card_footer), width / 2f, 850, footerPaint);

        return bitmap;
    }

    private static void drawStatCard(Canvas canvas, float x, float y, float w, float h,
                                     String label, String value, String colorHex) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Draw background
        paint.setColor(Color.parseColor("#F8FAFC"));
        RectF r = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(r, 16, 16, paint);

        // Draw tiny colored indicator circle
        paint.setColor(Color.parseColor(colorHex));
        canvas.drawCircle(x + 20, y + 25, 6, paint);

        // Draw label
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(13);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(label, x + 34, y + 30, textPaint);

        // Draw value
        textPaint.setColor(Color.parseColor("#1E293B"));
        textPaint.setTextSize(22);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(value, x + 20, y + 75, textPaint);
    }
}

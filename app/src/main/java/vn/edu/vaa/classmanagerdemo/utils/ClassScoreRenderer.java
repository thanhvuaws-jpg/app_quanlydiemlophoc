package vn.edu.vaa.classmanagerdemo.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.models.Score;

public class ClassScoreRenderer {

    public static File renderToPng(File outputDir, String className, String subject, List<Score> scores) throws Exception {
        int width = 800;
        int headerHeight = 180;
        int rowHeight = 45;
        int footerHeight = 120;

        int limit = Math.min(scores.size(), 20);
        int tableHeight = (limit + 1) * rowHeight; // +1 for table header row
        int height = headerHeight + tableHeight + footerHeight;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Paints
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#F8FAFC")); // Light slate bg
        canvas.drawRect(0, 0, width, height, bgPaint);

        // Header bg
        Paint headerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerBgPaint.setColor(Color.parseColor("#1E293B")); // Premium Slate Navy
        canvas.drawRect(0, 0, width, headerHeight, headerBgPaint);

        // Title text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(spToPx(24));
        textPaint.setFakeBoldText(true);
        canvas.drawText("BẢNG ĐIỂM CHI TIẾT", 40, 50, textPaint);

        // Subtitle text
        textPaint.setTextSize(spToPx(16));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("Lớp: " + className + "  |  Môn: " + subject, 40, 85, textPaint);

        // Calculate statistics
        float sum = 0, max = 0, min = 10;
        int cntA = 0, cntB = 0, cntC = 0, cntD = 0, cntF = 0;
        for (Score s : scores) {
            float sc = s.getScore();
            sum += sc;
            if (sc > max) max = sc;
            if (sc < min) min = sc;
            String letter = s.getGradeLetter();
            switch (letter) {
                case "A": cntA++; break;
                case "B": cntB++; break;
                case "C": cntC++; break;
                case "D": cntD++; break;
                default: cntF++; break;
            }
        }
        float avg = scores.isEmpty() ? 0 : sum / scores.size();

        // Stats summary drawing in header
        textPaint.setColor(Color.parseColor("#F59E0B")); // Amber Gold
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(spToPx(13));
        String statsStr = String.format(Locale.US, "Sĩ số: %d  |  ĐTB: %.1f  |  Cao nhất: %.1f  |  Thấp nhất: %.1f",
                scores.size(), avg, max, min);
        canvas.drawText(statsStr, 40, 120, textPaint);

        // Stats breakdown
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(spToPx(11));
        String breakdownStr = String.format(Locale.US, "Xếp loại: Giỏi (A): %d | Khá (B): %d | TB (C): %d | Yếu (D): %d | Kém (F): %d",
                cntA, cntB, cntC, cntD, cntF);
        canvas.drawText(breakdownStr, 40, 145, textPaint);

        // Table Header
        int y = headerHeight;
        Paint tableHeaderBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableHeaderBg.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRect(20, y, width - 20, y + rowHeight, tableHeaderBg);

        Paint tableTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tableTextPaint.setColor(Color.parseColor("#0F172A"));
        tableTextPaint.setTextSize(spToPx(13));
        tableTextPaint.setFakeBoldText(true);

        canvas.drawText("Mã HS", 40, y + 28, tableTextPaint);
        canvas.drawText("Họ và tên", 180, y + 28, tableTextPaint);
        canvas.drawText("QT", 440, y + 28, tableTextPaint);
        canvas.drawText("GK", 500, y + 28, tableTextPaint);
        canvas.drawText("CK", 560, y + 28, tableTextPaint);
        canvas.drawText("TK", 620, y + 28, tableTextPaint);
        canvas.drawText("Xếp loại", 680, y + 28, tableTextPaint);

        // Table Rows
        tableTextPaint.setFakeBoldText(false);
        Paint rowLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rowLinePaint.setColor(Color.parseColor("#E2E8F0"));
        rowLinePaint.setStrokeWidth(1);

        Paint altRowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        altRowPaint.setColor(Color.parseColor("#F1F5F9"));

        for (int i = 0; i < limit; i++) {
            y += rowHeight;
            Score s = scores.get(i);

            // Alt row backgrounds
            if (i % 2 == 1) {
                canvas.drawRect(20, y, width - 20, y + rowHeight, altRowPaint);
            }
            canvas.drawLine(20, y + rowHeight, width - 20, y + rowHeight, rowLinePaint);

            // Column texts
            canvas.drawText(s.getStudentCode() != null ? s.getStudentCode() : "", 40, y + 28, tableTextPaint);
            canvas.drawText(s.getStudentName() != null ? s.getStudentName() : "", 180, y + 28, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.1f", s.getScoreQT()), 440, y + 28, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.1f", s.getScoreGK()), 500, y + 28, tableTextPaint);
            canvas.drawText(String.format(Locale.US, "%.1f", s.getScoreCK()), 560, y + 28, tableTextPaint);

            // TK & Grade Letter with Colors
            Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            colorPaint.setTextSize(spToPx(13));
            colorPaint.setFakeBoldText(true);
            try {
                colorPaint.setColor(Color.parseColor(s.getGradeColor()));
            } catch (Exception e) {
                colorPaint.setColor(Color.BLACK);
            }
            canvas.drawText(String.format(Locale.US, "%.1f", s.getScore()), 620, y + 28, colorPaint);
            canvas.drawText(s.getGradeLetter(), 680, y + 28, colorPaint);
        }

        // Draw side borders of table
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#CBD5E1"));
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(20, headerHeight, width - 20, headerHeight + tableHeight, borderPaint);

        // Footer block
        y = headerHeight + tableHeight;
        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(Color.parseColor("#94A3B8"));
        footerPaint.setTextSize(spToPx(12));
        footerPaint.setTextAlign(Paint.Align.CENTER);

        if (scores.size() > 20) {
            canvas.drawText("... và " + (scores.size() - 20) + " học sinh khác (danh sách đã rút gọn) ...", width / 2f, y + 35, footerPaint);
        }

        canvas.drawText("Sổ điểm GV - Ứng dụng quản lý lớp học chuyên nghiệp dành cho Giáo viên", width / 2f, y + 70, footerPaint);

        // Write to output file
        if (!outputDir.exists()) outputDir.mkdirs();
        File file = new File(outputDir, "bang_diem_" + className + ".png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        return file;
    }

    private static float spToPx(float sp) {
        // Simple pixel scaling calculation (assuming standard density multiplier approx 2.0-3.0)
        return sp * 1.5f;
    }
}

package vn.edu.vaa.classmanagerdemo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GpaChartView extends View {
    private List<String> semesters = new ArrayList<>();
    private List<Float> gpaValues = new ArrayList<>();

    private Paint gridPaint;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Path linePath;
    private Path fillPath;

    public GpaChartView(Context context) {
        super(context);
        init();
    }

    public GpaChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GpaChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#0EA5E9")); // Sky Blue primary
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(Color.parseColor("#0EA5E9"));
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#64748B")); // slate-500
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePath = new Path();
        fillPath = new Path();
    }

    public void setData(List<String> semesters, List<Float> gpaValues) {
        this.semesters = semesters != null ? semesters : new ArrayList<>();
        this.gpaValues = gpaValues != null ? gpaValues : new ArrayList<>();
        invalidate(); // request redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 80f;
        float paddingRight = 80f;
        float paddingTop = 60f;
        float paddingBottom = 60f;

        float graphWidth = width - paddingLeft - paddingRight;
        float graphHeight = height - paddingTop - paddingBottom;

        if (semesters.isEmpty() || gpaValues.isEmpty()) {
            canvas.drawText(getContext().getString(vn.edu.vaa.classmanagerdemo.R.string.chart_no_data), width / 2f, height / 2f, textPaint);
            return;
        }

        // Draw horizontal grid lines for GPA (1.0, 2.0, 3.0, 4.0)
        for (int i = 1; i <= 4; i++) {
            float gpaLevel = i;
            float y = paddingTop + graphHeight * (1f - (gpaLevel / 4.0f));
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);
            canvas.drawText(String.valueOf(i) + ".0", paddingLeft - 40f, y + 8f, textPaint);
        }

        int pointsCount = semesters.size();
        float xStep = pointsCount > 1 ? graphWidth / (pointsCount - 1) : graphWidth;

        float[] xCoords = new float[pointsCount];
        float[] yCoords = new float[pointsCount];

        for (int i = 0; i < pointsCount; i++) {
            xCoords[i] = paddingLeft + (pointsCount > 1 ? i * xStep : graphWidth / 2f);
            float gpa = gpaValues.get(i);
            if (gpa > 4.0f) gpa = 4.0f;
            if (gpa < 0.0f) gpa = 0.0f;
            yCoords[i] = paddingTop + graphHeight * (1f - (gpa / 4.0f));
        }

        // Draw fill gradient below the line
        if (pointsCount > 1) {
            fillPath.reset();
            fillPath.moveTo(xCoords[0], yCoords[0]);
            for (int i = 1; i < pointsCount; i++) {
                fillPath.lineTo(xCoords[i], yCoords[i]);
            }
            fillPath.lineTo(xCoords[pointsCount - 1], paddingTop + graphHeight);
            fillPath.lineTo(xCoords[0], paddingTop + graphHeight);
            fillPath.close();

            int startColor = Color.parseColor("#330EA5E9");
            int endColor = Color.parseColor("#000EA5E9");
            fillPaint.setShader(new LinearGradient(0, paddingTop, 0, paddingTop + graphHeight, startColor, endColor, Shader.TileMode.CLAMP));
            canvas.drawPath(fillPath, fillPaint);
        }

        // Draw line path
        linePath.reset();
        linePath.moveTo(xCoords[0], yCoords[0]);
        for (int i = 1; i < pointsCount; i++) {
            linePath.lineTo(xCoords[i], yCoords[i]);
        }
        if (pointsCount > 1) {
            canvas.drawPath(linePath, linePaint);
        } else {
            // Draw a flat horizontal line if only 1 point
            canvas.drawLine(paddingLeft, yCoords[0], width - paddingRight, yCoords[0], linePaint);
        }

        // Draw points and texts
        for (int i = 0; i < pointsCount; i++) {
            float cx = xCoords[i];
            float cy = yCoords[i];

            // Point dot
            pointPaint.setColor(Color.parseColor("#0EA5E9"));
            canvas.drawCircle(cx, cy, 12f, pointPaint);
            pointPaint.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy, 6f, pointPaint);

            // GPA text above dot
            String gpaText = String.format("%.2f", gpaValues.get(i));
            canvas.drawText(gpaText, cx, cy - 20f, textPaint);

            // Semester label below
            String semLabel = semesters.get(i);
            if (semLabel.toLowerCase().contains("học kỳ")) {
                semLabel = semLabel.replaceAll("(?i)học kỳ\\s*", "HK");
            }
            canvas.drawText(semLabel, cx, paddingTop + graphHeight + 40f, textPaint);
        }
    }
}

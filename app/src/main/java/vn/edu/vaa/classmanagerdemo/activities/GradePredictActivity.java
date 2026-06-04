package vn.edu.vaa.classmanagerdemo.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.database.ScoreDAO;
import vn.edu.vaa.classmanagerdemo.storage.AppPreferenceManager;
import vn.edu.vaa.classmanagerdemo.utils.NavigationHelper;

public class GradePredictActivity extends AppCompatActivity {
    private TextInputEditText edtCurrentGpa, edtCurrentCredits, edtTargetGpa, edtFutureCredits;
    private MaterialCardView cardPredictResult;
    private TextView tvPredictTitle, tvPredictMessage;
    private ScoreDAO scoreDAO;
    private AppPreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_predict);

        Toolbar toolbar = findViewById(R.id.toolbarPredict);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = new AppPreferenceManager(this);
        scoreDAO = new ScoreDAO(this);

        initViews();
        loadCurrentStats();

        findViewById(R.id.btnCalculatePredict).setOnClickListener(v -> calculatePrediction());
    }

    private void initViews() {
        edtCurrentGpa = findViewById(R.id.edtCurrentGpa);
        edtCurrentCredits = findViewById(R.id.edtCurrentCredits);
        edtTargetGpa = findViewById(R.id.edtTargetGpa);
        edtFutureCredits = findViewById(R.id.edtFutureCredits);
        cardPredictResult = findViewById(R.id.cardPredictResult);
        tvPredictTitle = findViewById(R.id.tvPredictTitle);
        tvPredictMessage = findViewById(R.id.tvPredictMessage);
    }

    private void loadCurrentStats() {
        int userId = prefs.getCurrentUserId();
        float gpa = scoreDAO.getCumulativeGpaByStudentId(userId);
        int credits = scoreDAO.getTotalCreditsByStudentId(userId);

        edtCurrentGpa.setText(String.format(Locale.US, "%.2f", gpa));
        edtCurrentCredits.setText(String.valueOf(credits));
    }

    private void calculatePrediction() {
        String currentGpaStr = edtCurrentGpa.getText().toString().trim();
        String currentCreditsStr = edtCurrentCredits.getText().toString().trim();
        String targetGpaStr = edtTargetGpa.getText().toString().trim();
        String futureCreditsStr = edtFutureCredits.getText().toString().trim();

        if (currentGpaStr.isEmpty() || currentCreditsStr.isEmpty() || targetGpaStr.isEmpty() || futureCreditsStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float currentGpa = Float.parseFloat(currentGpaStr);
            int currentCredits = Integer.parseInt(currentCreditsStr);
            float targetGpa = Float.parseFloat(targetGpaStr);
            int futureCredits = Integer.parseInt(futureCreditsStr);

            if (currentGpa < 0 || currentGpa > 4.0 || targetGpa < 0 || targetGpa > 4.0) {
                Toast.makeText(this, "GPA phải nằm trong khoảng từ 0.0 đến 4.0", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentCredits < 0 || futureCredits <= 0) {
                Toast.makeText(this, "Số tín chỉ phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalCredits = currentCredits + futureCredits;
            float totalPointsNeeded = targetGpa * totalCredits;
            float currentPoints = currentGpa * currentCredits;
            float futurePointsNeeded = totalPointsNeeded - currentPoints;
            float futureGpaRequired = futurePointsNeeded / futureCredits;

            cardPredictResult.setVisibility(View.VISIBLE);

            if (futureGpaRequired > 4.0f) {
                tvPredictTitle.setText("❌ Không khả thi");
                tvPredictTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvPredictMessage.setText(String.format(Locale.getDefault(),
                        "Để đạt mục tiêu GPA tích lũy là %.2f, bạn cần đạt điểm trung bình GPA tương lai là %.2f trong %d tín chỉ tiếp theo. Điều này vượt quá mức tối đa là 4.00.\n\nKhuyên: Bạn nên giảm bớt mục tiêu GPA mong muốn hoặc tăng số tín chỉ học tiếp theo để giảm gánh nặng điểm số.",
                        targetGpa, futureGpaRequired, futureCredits));
            } else if (futureGpaRequired <= 0f) {
                tvPredictTitle.setText("🎉 Đã đạt mục tiêu");
                tvPredictTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                tvPredictMessage.setText(String.format(Locale.getDefault(),
                        "GPA hiện tại của bạn (%.2f) đã đạt hoặc vượt mục tiêu mong muốn (%.2f). Bạn chỉ cần hoàn thành các tín chỉ mới mà không cần lo lắng về việc cải thiện điểm GPA.",
                        currentGpa, targetGpa));
            } else {
                tvPredictTitle.setText(String.format(Locale.US, "💪 Cần đạt: %.2f", futureGpaRequired));
                tvPredictTitle.setTextColor(getResources().getColor(R.color.primary));

                String letter = getSuggestedLetter(futureGpaRequired);
                String system10 = getSuggestedSystem10(futureGpaRequired);

                tvPredictMessage.setText(String.format(Locale.getDefault(),
                        "Để đạt mục tiêu GPA tích lũy là %.2f sau khi học thêm %d tín chỉ, bạn cần đạt điểm GPA trung bình là %.2f cho các môn học sắp tới.\n\n" +
                                "👉 Quy đổi tương đương:\n" +
                                "• Điểm hệ 10 khoảng: %s\n" +
                                "• Điểm chữ khoảng: %s",
                        targetGpa, futureCredits, futureGpaRequired, system10, letter));
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Dữ liệu nhập vào không đúng định dạng", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSuggestedLetter(float gpa4) {
        if (gpa4 >= 3.75) return "A";
        if (gpa4 >= 3.25) return "B+";
        if (gpa4 >= 2.75) return "B";
        if (gpa4 >= 2.25) return "C+";
        if (gpa4 >= 1.75) return "C";
        if (gpa4 >= 1.25) return "D+";
        if (gpa4 >= 0.75) return "D";
        return "F";
    }

    private String getSuggestedSystem10(float gpa4) {
        if (gpa4 >= 3.75) return ">= 8.5";
        if (gpa4 >= 3.25) return "8.0 - 8.4";
        if (gpa4 >= 2.75) return "7.0 - 7.9";
        if (gpa4 >= 2.25) return "6.5 - 6.9";
        if (gpa4 >= 1.75) return "5.5 - 6.4";
        if (gpa4 >= 1.25) return "5.0 - 5.4";
        if (gpa4 >= 0.75) return "4.0 - 4.9";
        return "< 4.0";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavigationHelper.finishWithSlide(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

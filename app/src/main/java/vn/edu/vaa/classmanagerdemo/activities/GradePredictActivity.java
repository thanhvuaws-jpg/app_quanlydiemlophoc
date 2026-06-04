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

public class GradePredictActivity extends BaseActivity {
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
            Toast.makeText(this, getString(R.string.error_missing_info), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float currentGpa = Float.parseFloat(currentGpaStr);
            int currentCredits = Integer.parseInt(currentCreditsStr);
            float targetGpa = Float.parseFloat(targetGpaStr);
            int futureCredits = Integer.parseInt(futureCreditsStr);

            if (currentGpa < 0 || currentGpa > 4.0 || targetGpa < 0 || targetGpa > 4.0) {
                Toast.makeText(this, getString(R.string.error_gpa_range), Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentCredits <= 0 || futureCredits <= 0) {
                Toast.makeText(this, getString(R.string.error_credits_positive), Toast.LENGTH_SHORT).show();
                return;
            }

            int totalCredits = currentCredits + futureCredits;
            float totalPointsNeeded = targetGpa * totalCredits;
            float currentPoints = currentGpa * currentCredits;
            float futurePointsNeeded = totalPointsNeeded - currentPoints;
            float futureGpaRequired = futurePointsNeeded / futureCredits;

            cardPredictResult.setVisibility(View.VISIBLE);

            if (futureGpaRequired > 4.0f) {
                tvPredictTitle.setText(getString(R.string.predict_impossible));
                tvPredictTitle.setTextColor(getResources().getColor(R.color.danger, null));
                tvPredictMessage.setText(getString(R.string.predict_impossible_desc, targetGpa, futureGpaRequired, futureCredits));
            } else if (futureGpaRequired <= currentGpa && futureGpaRequired <= 0) {
                tvPredictTitle.setText(getString(R.string.predict_achieved));
                tvPredictTitle.setTextColor(getResources().getColor(R.color.success, null));
                tvPredictMessage.setText(getString(R.string.predict_achieved_desc, currentGpa, targetGpa));
            } else {
                tvPredictTitle.setText(getString(R.string.predict_feasible));
                tvPredictTitle.setTextColor(getResources().getColor(R.color.primary, null));
                tvPredictMessage.setText(getString(R.string.predict_feasible_desc, futureGpaRequired, futureCredits, targetGpa));
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

package vn.edu.vaa.classmanagerdemo.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Score;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    public interface OnScoreEditListener {
        void onEdit(Score score, int position);
        void onDelete(Score score, int position);
    }

    private final List<Score> list;
    private final OnScoreEditListener editListener;

    public ScoreAdapter(List<Score> list, OnScoreEditListener editListener) {
        this.list = list;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Score score = list.get(position);

        h.tvSemester.setText(score.getSemester());
        h.tvScoreQT.setText(String.format(Locale.US, "QT (%.0f%%): %.1f", (float)score.getWeightQT(), score.getScoreQT()));
        h.tvScoreGK.setText(String.format(Locale.US, "GK (%.0f%%): %.1f", (float)score.getWeightGK(), score.getScoreGK()));
        h.tvScoreCK.setText(String.format(Locale.US, "CK (%.0f%%): %.1f", (float)score.getWeightCK(), score.getScoreCK()));

        float finalScore = score.getScore();
        h.tvFinalScore.setText(String.format(Locale.US, "%.1f", finalScore));
        h.tvGradeLetter.setText(score.getGradeLetter());
        
        try {
            h.tvFinalScore.setTextColor(Color.parseColor(score.getGradeColor()));
            h.tvGradeLetter.setTextColor(Color.parseColor(score.getGradeColor()));
        } catch (IllegalArgumentException e) {
            h.tvFinalScore.setTextColor(Color.BLACK);
            h.tvGradeLetter.setTextColor(Color.BLACK);
        }

        h.itemView.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("Tùy chọn điểm")
                .setItems(new String[]{"Chỉnh sửa", "Xóa"}, (d, which) -> {
                    if (which == 0) editListener.onEdit(score, position);
                    else editListener.onDelete(score, position);
                }).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSemester;
        TextView tvScoreQT, tvScoreGK, tvScoreCK;
        TextView tvFinalScore, tvGradeLetter;

        ViewHolder(View v) {
            super(v);
            tvSemester = v.findViewById(R.id.tvSemester);
            tvScoreQT = v.findViewById(R.id.tvScoreQT);
            tvScoreGK = v.findViewById(R.id.tvScoreGK);
            tvScoreCK = v.findViewById(R.id.tvScoreCK);
            tvFinalScore = v.findViewById(R.id.tvFinalScore);
            tvGradeLetter = v.findViewById(R.id.tvGradeLetter);
        }
    }
}

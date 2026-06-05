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

        // Badge bg cũng đổi màu theo grade
        try {
            com.google.android.material.card.MaterialCardView badge =
                (com.google.android.material.card.MaterialCardView) h.tvGradeLetter.getParent();
            badge.setCardBackgroundColor(
                android.graphics.Color.parseColor(score.getGradeColor() + "33")); // 20% opacity
            h.tvGradeLetter.setTextColor(android.graphics.Color.parseColor(score.getGradeColor()));
        } catch (Exception ignored) {}

        h.itemView.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle("Tùy chọn điểm")
                .setItems(new String[]{"Chỉnh sửa", "Xóa", "Nhận xét tự động"}, (d, which) -> {
                    if (which == 0) editListener.onEdit(score, position);
                    else if (which == 1) editListener.onDelete(score, position);
                    else if (which == 2) {
                        String comment = score.generateComment();
                        new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle("Nhận xét tự động")
                            .setMessage(comment)
                            .setPositiveButton("Sao chép", (dialog, w) -> {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                                    v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Comment", comment);
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(clip);
                                    android.widget.Toast.makeText(v.getContext(), "Đã sao chép nhận xét!", android.widget.Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Đóng", null)
                            .show();
                    }
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

package vn.edu.vaa.classmanagerdemo.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Score;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    public interface OnScoreDeleteListener {
        void onDelete(Score score, int position);
    }

    private final List<Score> list;
    private final OnScoreDeleteListener listener;

    public ScoreAdapter(List<Score> list, OnScoreDeleteListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Score s = list.get(position);
        String color = s.getGradeColor();
        int parsedColor = Color.parseColor(color);

        h.tvScore.setText(String.format("%.1f", s.getScore()));
        h.tvScore.setTextColor(parsedColor);
        h.scoreCircle.setStrokeColor(parsedColor);

        if (s.getStudentName() != null && !s.getStudentName().isEmpty()) {
            h.tvStudent.setVisibility(View.VISIBLE);
            h.tvStudent.setText(s.getStudentName());
        } else {
            h.tvStudent.setVisibility(View.GONE);
        }

        h.tvSubject.setText(s.getSubject());
        h.tvSemester.setText(s.getSemester() != null ? s.getSemester() : "");
        h.tvGrade.setText(s.getGradeLabel());
        h.tvGrade.setBackgroundColor(Color.parseColor(color + "22")); // translucent
        h.tvGrade.setTextColor(parsedColor);

        h.itemView.setOnLongClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                listener.onDelete(list.get(pos), pos);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView scoreCircle;
        TextView tvScore, tvStudent, tvSubject, tvSemester, tvGrade;

        ViewHolder(View v) {
            super(v);
            scoreCircle = v.findViewById(R.id.cardScoreCircle);
            tvScore = v.findViewById(R.id.tvScoreValue);
            tvStudent = v.findViewById(R.id.tvScoreStudent);
            tvSubject = v.findViewById(R.id.tvScoreSubject);
            tvSemester = v.findViewById(R.id.tvScoreSemester);
            tvGrade = v.findViewById(R.id.tvScoreGrade);
        }
    }
}

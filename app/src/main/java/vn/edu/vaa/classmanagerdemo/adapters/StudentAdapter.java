package vn.edu.vaa.classmanagerdemo.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Student;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    public interface OnStudentClickListener { void onClick(Student student); }
    public interface OnStudentDeleteListener { void onDelete(Student student); }
    public interface OnStudentLongClickListener { void onLongClick(Student student); }
    public interface OnGetAvgScoreCallback { float getAvg(int studentId); }

    private final List<Student> list;
    private final OnStudentClickListener clickListener;
    private final OnStudentDeleteListener deleteListener;
    private OnStudentLongClickListener longClickListener;
    private OnGetAvgScoreCallback avgCallback;

    /** Legacy 2-param constructor for backward compat */
    public StudentAdapter(List<Student> list, OnStudentClickListener click, OnStudentDeleteListener delete) {
        this.list = list;
        this.clickListener = click;
        this.deleteListener = delete;
    }

    /** Full constructor with avg score callback */
    public StudentAdapter(List<Student> list, OnStudentClickListener click,
                          OnStudentDeleteListener delete, OnStudentLongClickListener longClick,
                          OnGetAvgScoreCallback avgCallback) {
        this.list = list;
        this.clickListener = click;
        this.deleteListener = delete;
        this.longClickListener = longClick;
        this.avgCallback = avgCallback;
    }

    private static final String[] AVATAR_COLORS = {
        "#4F46E5", "#0891B2", "#059669", "#D97706",
        "#DC2626", "#7C3AED", "#0284C7", "#15803D"
    };

    private static String getAvatarColor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_COLORS[0];
        int hash = 0;
        for (char c : name.toCharArray()) hash = hash * 31 + c;
        return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Student s = list.get(position);
        h.tvInitials.setText(s.getInitials());

        // Đổi màu avatar theo tên — mỗi học sinh có màu riêng
        String avatarColor = getAvatarColor(s.getFullName());
        try {
            ((com.google.android.material.card.MaterialCardView) h.tvInitials.getParent())
                .setCardBackgroundColor(android.graphics.Color.parseColor(avatarColor));
        } catch (Exception ignored) {}

        h.tvName.setText(s.getFullName());
        h.tvCode.setText(s.getStudentCode());

        // Average score display
        if (h.tvAvg != null && avgCallback != null) {
            float avg = avgCallback.getAvg(s.getId());
            if (avg > 0) {
                h.tvAvg.setText("TB: " + String.format(Locale.US, "%.1f", avg));
                String color = avg >= 8.5f ? "#10B981" : avg >= 7.0f ? "#3B82F6" : avg >= 5.5f ? "#F59E0B" : "#EF4444";
                try { h.tvAvg.setTextColor(Color.parseColor(color)); } catch (Exception ignored) {}
                h.tvAvg.setVisibility(View.VISIBLE);
            } else {
                h.tvAvg.setVisibility(View.GONE);
            }
        }

        h.itemView.setOnClickListener(v -> clickListener.onClick(s));
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                new AlertDialog.Builder(v.getContext())
                    .setTitle(s.getFullName())
                    .setItems(new String[]{"Chỉnh sửa", "Xóa"}, (d, which) -> {
                        if (which == 0) longClickListener.onLongClick(s);
                        else deleteListener.onDelete(s);
                    }).show();
            } else {
                deleteListener.onDelete(s);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvCode, tvAvg;
        ViewHolder(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tvInitials);
            tvName = v.findViewById(R.id.tvStudentName);
            tvCode = v.findViewById(R.id.tvStudentCode);
            tvAvg = v.findViewById(R.id.tvStudentAvgScore);
        }
    }
}

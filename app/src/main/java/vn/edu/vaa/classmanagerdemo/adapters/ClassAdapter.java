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
import vn.edu.vaa.classmanagerdemo.models.ClassRoom;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    private static final String[] COLORS = {
            "#6366F1", "#EC4899", "#F59E0B", "#10B981",
            "#3B82F6", "#8B5CF6", "#EF4444", "#06B6D4"
    };

    public interface OnClassClickListener {
        void onClassClick(ClassRoom classRoom);
        void onClassLongClick(ClassRoom classRoom);
    }

    private final List<ClassRoom> list;
    private final OnClassClickListener listener;

    public ClassAdapter(List<ClassRoom> list, OnClassClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ClassRoom cr = list.get(position);
        String color = COLORS[position % COLORS.length];

        h.colorBar.setBackgroundColor(Color.parseColor(color));
        h.tvInitial.setText(cr.getName().length() > 0 ? String.valueOf(cr.getName().charAt(0)).toUpperCase() : "C");
        h.tvInitial.setTextColor(Color.parseColor(color));
        h.tvName.setText(cr.getName());
        h.tvYear.setText(cr.getSchoolYear() != null && !cr.getSchoolYear().isEmpty()
                ? "Năm học: " + cr.getSchoolYear() : "Chưa có năm học");
        h.tvCount.setText(cr.getStudentCount() + " sinh viên");
        h.tvCount.setTextColor(Color.parseColor(color));

        h.itemView.setOnClickListener(v -> listener.onClassClick(cr));
        h.itemView.setOnLongClickListener(v -> { listener.onClassLongClick(cr); return true; });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View colorBar;
        TextView tvInitial, tvName, tvYear, tvCount;

        ViewHolder(View v) {
            super(v);
            colorBar = v.findViewById(R.id.viewClassColor);
            tvInitial = v.findViewById(R.id.tvClassInitial);
            tvName = v.findViewById(R.id.tvClassName);
            tvYear = v.findViewById(R.id.tvClassYear);
            tvCount = v.findViewById(R.id.tvClassStudentCount);
        }
    }
}

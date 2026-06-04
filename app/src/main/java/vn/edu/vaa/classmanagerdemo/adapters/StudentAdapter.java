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
import vn.edu.vaa.classmanagerdemo.models.Student;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    public interface OnStudentClickListener { void onClick(Student student); }
    public interface OnStudentDeleteListener { void onDelete(Student student); }

    private final List<Student> list;
    private final OnStudentClickListener clickListener;
    private final OnStudentDeleteListener deleteListener;

    public StudentAdapter(List<Student> list, OnStudentClickListener click, OnStudentDeleteListener delete) {
        this.list = list;
        this.clickListener = click;
        this.deleteListener = delete;
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
        h.tvName.setText(s.getFullName());
        h.tvCode.setText(s.getStudentCode());

        h.itemView.setOnClickListener(v -> clickListener.onClick(s));
        h.itemView.setOnLongClickListener(v -> {
            deleteListener.onDelete(s);
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvCode;
        ViewHolder(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tvInitials);
            tvName = v.findViewById(R.id.tvStudentName);
            tvCode = v.findViewById(R.id.tvStudentCode);
        }
    }
}

package vn.edu.vaa.classmanagerdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Student;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
    public interface OnStudentClickListener {
        void onStudentClick(Student student, int position);
        void onStudentLongClick(Student student, int position);
    }

    private final List<Student> students;
    private final OnStudentClickListener listener;

    public StudentAdapter(List<Student> students, OnStudentClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student s = students.get(position);
        holder.tvName.setText(s.getName());
        holder.tvInfo.setText("Lớp: " + s.getClassName() + " | Email: " + s.getEmail() + " | SĐT: " + s.getPhone());
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onStudentClick(students.get(pos), pos);
        });
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onStudentLongClick(students.get(pos), pos);
            return true;
        });
    }

    @Override
    public int getItemCount() { return students.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }
}

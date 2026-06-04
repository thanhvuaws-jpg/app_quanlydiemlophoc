package vn.edu.vaa.classmanagerdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.SchoolClass;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    public interface OnClassClickListener {
        void onClick(SchoolClass cls);
    }
    public interface OnClassLongClickListener {
        void onEdit(SchoolClass cls);
        void onDelete(SchoolClass cls);
    }

    private final List<SchoolClass> list;
    private final OnClassClickListener clickListener;
    private final OnClassLongClickListener editListener;
    private final OnClassLongClickListener deleteListener;

    public ClassAdapter(List<SchoolClass> list,
                        OnClassClickListener clickListener,
                        OnClassLongClickListener editListener,
                        OnClassLongClickListener deleteListener) {
        this.list = list;
        this.clickListener = clickListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SchoolClass cls = list.get(position);
        h.tvClassName.setText(cls.getClassName());
        h.tvSubject.setText(cls.getSubject());
        h.tvSchoolYear.setText(cls.getSchoolYear());
        h.tvStudentCount.setText(cls.getStudentCount() + " học sinh");

        h.itemView.setOnClickListener(v -> clickListener.onClick(cls));
        h.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                .setTitle(cls.getClassName())
                .setItems(new String[]{"Chỉnh sửa", "Xóa"}, (d, which) -> {
                    if (which == 0) editListener.onEdit(cls);
                    else deleteListener.onDelete(cls);
                }).show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvSubject, tvSchoolYear, tvStudentCount;
        ViewHolder(View v) {
            super(v);
            tvClassName = v.findViewById(R.id.tvClassName);
            tvSubject = v.findViewById(R.id.tvSubject);
            tvSchoolYear = v.findViewById(R.id.tvSchoolYear);
            tvStudentCount = v.findViewById(R.id.tvStudentCount);
        }
    }
}

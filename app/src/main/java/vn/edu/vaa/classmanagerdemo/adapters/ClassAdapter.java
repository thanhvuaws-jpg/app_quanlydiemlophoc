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

    private static final String[] CLASS_COLORS = {"#EEF2FF","#FEF3C7","#ECFDF5","#FFF1F2","#F0F9FF","#FDF4FF"};
    private static final String[] CLASS_ICON_COLORS = {"#4F46E5","#D97706","#059669","#E11D48","#0284C7","#9333EA"};

    public interface OnClassClickListener {
        void onClick(SchoolClass cls);
    }
    public interface OnClassEditListener {
        void onEdit(SchoolClass cls);
    }
    public interface OnClassDeleteListener {
        void onDelete(SchoolClass cls);
    }

    private final List<SchoolClass> list;
    private final OnClassClickListener clickListener;
    private final OnClassEditListener editListener;
    private final OnClassDeleteListener deleteListener;

    public ClassAdapter(List<SchoolClass> list,
                        OnClassClickListener clickListener,
                        OnClassEditListener editListener,
                        OnClassDeleteListener deleteListener) {
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

        // Đổi màu icon lớp theo position
        try {
            int ci = position % CLASS_COLORS.length;
            android.view.View iconCard = h.itemView.findViewById(R.id.cardClassIcon);
            if (iconCard instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) iconCard)
                    .setCardBackgroundColor(android.graphics.Color.parseColor(CLASS_COLORS[ci]));
            }
            android.widget.ImageView icon = h.itemView.findViewById(R.id.ivClassIcon);
            if (icon != null) {
                icon.setColorFilter(android.graphics.Color.parseColor(CLASS_ICON_COLORS[ci]));
            }
        } catch (Exception ignored) {}

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

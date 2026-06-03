package vn.edu.vaa.classmanagerdemo.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.vaa.classmanagerdemo.R;
import vn.edu.vaa.classmanagerdemo.models.Todo;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {
    public interface OnTodoClickListener {
        void onTodoClick(Todo todo, int position);
        void onTodoLongClick(Todo todo, int position);
    }

    private final List<Todo> todos;
    private final OnTodoClickListener listener;

    public TodoAdapter(List<Todo> todos, OnTodoClickListener listener) {
        this.todos = todos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo t = todos.get(position);
        holder.tvTitle.setText((t.isCompleted() ? "✓ " : "○ ") + t.getTitle());
        holder.tvDeadline.setText("Hạn nộp: " + (t.getDeadline().isEmpty() ? "(không có)" : t.getDeadline()));
        holder.tvTitle.setPaintFlags(t.isCompleted()
                ? holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onTodoClick(todos.get(pos), pos);
        });
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onTodoLongClick(todos.get(pos), pos);
            return true;
        });
    }

    @Override
    public int getItemCount() { return todos.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDeadline;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }
}

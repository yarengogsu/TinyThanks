package com.example.tinythanks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskEntry> taskList = new ArrayList<>();
    private OnTaskActionListener listener;

    // Tıklama olayları için arabirim
    public interface OnTaskActionListener {
        void onTaskCheckChanged(TaskEntry task, boolean isChecked);
        void onTaskDelete(TaskEntry task);
    }

    public TaskAdapter(OnTaskActionListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskEntry> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskEntry task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());

        // Checkbox dinleyicisini geçici olarak durdur (sonsuz döngü olmasın diye)
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        // Eğer yapıldıysa üzerini çiz
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1.0f);
        }

        // Tik atılınca
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onTaskCheckChanged(task, isChecked);
        });

        // Silme butonuna basılınca
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onTaskDelete(task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CheckBox checkBox;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            checkBox = itemView.findViewById(R.id.cbTaskCompleted);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
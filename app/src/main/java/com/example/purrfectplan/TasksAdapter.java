package com.example.purrfectplan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskDao;
import com.example.purrfectplan.room.TaskEntity;

import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private final Context context;
    private final TaskDao taskDao;
    private List<TaskEntity> tasks;

    public TasksAdapter(Context context, List<TaskEntity> tasks, TaskDao taskDao) {
        this.context = context;
        this.tasks = tasks;
        this.taskDao = taskDao;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public TaskEntity getTaskAt(int position) {
        return tasks.get(position);
    }

    public void removeAt(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = tasks.get(position);

        holder.tvTitle.setText(task.title);

        if ("completed".equals(task.status)) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.checkbox.setImageResource(R.drawable.paw_icon);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checkbox.setImageResource(R.drawable.custom_checkbox_shape);
        }

        holder.checkbox.setOnClickListener(v -> {
            if ("completed".equals(task.status)) {
                task.status = "not finished";
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.checkbox.setImageResource(R.drawable.custom_checkbox_shape);
            } else {
                task.status = "completed";
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.checkbox.setImageResource(R.drawable.paw_icon);
            }
            taskDao.update(task);
        });

        holder.ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("taskId", task.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tasks == null ? 0 : tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView checkbox;
        ImageView ivEdit;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            checkbox = itemView.findViewById(R.id.checkbox);
            ivEdit = itemView.findViewById(R.id.ivEditTask);
        }
    }
}


package com.example.purrfectplan;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskDao;
import com.example.purrfectplan.room.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity {

    private LinearLayout todayTasksContainer;
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        todayTasksContainer = findViewById(R.id.todayTasksContainer);

        // Inicjalizacja Room
        taskDao = AppDatabase.getInstance(this).taskDao();

        // Wyświetlenie dzisiejszych tasków
        loadTasksForToday();

        // Przycisk Add
        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(this, NewTaskActivity.class)));

        // Footer
        findViewById(R.id.btnLogout).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // Ustaw dzisiejszą datę
        TextView tvDate = findViewById(R.id.tvDate);
        String today = new SimpleDateFormat("d'th of' MMM, yyyy", Locale.ENGLISH)
                .format(Calendar.getInstance().getTime());
        tvDate.setText(today);
    }

    private void loadTasksForToday() {
        todayTasksContainer.removeAllViews();

        // Zakres timestampów dla dzisiejszego dnia
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        // Pobranie tasków z Room
        List<TaskEntity> tasks = taskDao.getTasksForToday(start.getTimeInMillis(), end.getTimeInMillis());

        for (TaskEntity task : tasks) {
            View taskView = LayoutInflater.from(this)
                    .inflate(R.layout.item_task, todayTasksContainer, false);

            TextView tvTitle = taskView.findViewById(R.id.tvTitle);
            ImageView checkbox = taskView.findViewById(R.id.checkbox);
            ImageView ivEdit = taskView.findViewById(R.id.ivEditTask);

            tvTitle.setText(task.title);

            // --- Ustawienie checkboxa / paw i przekreślenia ---
            if ("completed".equals(task.status)) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                checkbox.setImageResource(R.drawable.paw_icon);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                checkbox.setImageResource(R.drawable.custom_checkbox_shape);
            }

            // --- Kliknięcie checkbox / paw ---
            checkbox.setOnClickListener(v -> {
                if ("completed".equals(task.status)) {
                    task.status = "not finished";
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    checkbox.setImageResource(R.drawable.custom_checkbox_shape);
                } else {
                    task.status = "completed";
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    checkbox.setImageResource(R.drawable.paw_icon);
                }
                taskDao.update(task);
            });

            // --- Kliknięcie w trzy kropki -> edycja ---
            ivEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.putExtra("taskId", task.id);
                startActivity(intent);
            });

            todayTasksContainer.addView(taskView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksForToday(); // odśwież po powrocie z NewTaskActivity/EditTaskActivity
    }
}


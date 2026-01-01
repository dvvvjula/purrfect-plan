package com.example.purrfectplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskDao;
import com.example.purrfectplan.room.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView rvTodayTasks;
    private TasksAdapter tasksAdapter;
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        rvTodayTasks = findViewById(R.id.rvTodayTasks);
        rvTodayTasks.setLayoutManager(new LinearLayoutManager(this));

        taskDao = AppDatabase.getInstance(this).taskDao();

        tasksAdapter = new TasksAdapter(this, new java.util.ArrayList<>(), taskDao);
        rvTodayTasks.setAdapter(tasksAdapter);

        attachSwipeToDelete(rvTodayTasks);

        loadTasksForToday();

        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(new Intent(this, NewTaskActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        TextView tvDate = findViewById(R.id.tvDate);
        String today = new SimpleDateFormat("d'th of' MMM, yyyy", Locale.ENGLISH)
                .format(Calendar.getInstance().getTime());
        tvDate.setText(today);
    }

    private void loadTasksForToday() {
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

        List<TaskEntity> tasks = taskDao.getTasksForToday(
                start.getTimeInMillis(),
                end.getTimeInMillis()
        );

        tasksAdapter.setTasks(tasks);
    }

    private void attachSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        TaskEntity task = tasksAdapter.getTaskAt(position);
                        showDeleteDialog(task, position);
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void showDeleteDialog(TaskEntity task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_task, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // USUŃ BIAŁE TŁO + DOROBIENIE ROGÓW
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            taskDao.delete(task);
            tasksAdapter.removeAt(position);
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> {
            tasksAdapter.notifyItemChanged(position);
            dialog.dismiss();
        });

        dialog.show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        loadTasksForToday();
    }
}



package com.example.purrfectplan;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskDao;
import com.example.purrfectplan.room.TaskEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private AppDatabase db;
    private TaskDao taskDao;
    private TaskEntity task;
    private Calendar selectedDateTime = Calendar.getInstance();

    private AppCompatEditText etTitle, etDesc;
    private TextView tvDateSelect, tvTimeSelect;
    private Spinner statusSpinner;
    private SwitchCompat switchNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // 1. INIT ROOM
        db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        // 2. INIT UI
        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        tvDateSelect = findViewById(R.id.tvDateSelect);
        tvTimeSelect = findViewById(R.id.tvTimeSelect);
        statusSpinner = findViewById(R.id.statusSpinner);
        switchNotify = findViewById(R.id.switchNotify);

        // 3. LOAD TASK
        int taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            loadTask(taskId);
        }

        // 4. DATE/TIME PICKERS
        tvDateSelect.setOnClickListener(v -> showDatePicker(tvDateSelect));
        tvTimeSelect.setOnClickListener(v -> showTimePicker(tvTimeSelect));

        // 5. STATUS SPINNER
        setupStatusSpinner(statusSpinner);

        // 6. NOTIFY SWITCH LISTENER (NOWE!)
        switchNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences("PurrfectPrefs", MODE_PRIVATE);
            boolean globalNotificationsEnabled = prefs.getBoolean("notifications_enabled", false);

            if (isChecked && !globalNotificationsEnabled) {
                Toast.makeText(EditTaskActivity.this, "You have to turn notifications ON in app Settings first! Meow.", Toast.LENGTH_LONG).show();
                switchNotify.setChecked(false);
                return;
            }
            if (task != null) {
                task.notify = isChecked;
            }
        });

        // 7. SAVE BUTTON
        MaterialButton btnSave = findViewById(R.id.btnSaveChanges);
        btnSave.setOnClickListener(v -> saveChanges());

        // 8. FOOTER & SHARE
        findViewById(R.id.shareLayout).setOnClickListener(v -> shareTask());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void loadTask(int taskId) {
        new Thread(() -> {
            task = taskDao.getTaskById(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    etTitle.setText(task.title);
                    etDesc.setText(task.description);

                    selectedDateTime.setTimeInMillis(task.dateTime);
                    SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
                    SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                    tvDateSelect.setText(sdfDate.format(selectedDateTime.getTime()));
                    tvTimeSelect.setText(sdfTime.format(selectedDateTime.getTime()));

                    String[] statuses = {"not finished", "in process", "completed"};
                    int statusIndex = 0;
                    for (int i = 0; i < statuses.length; i++) {
                        if (statuses[i].equals(task.status)) {
                            statusIndex = i;
                            break;
                        }
                    }
                    statusSpinner.setSelection(statusIndex);

                    switchNotify.setChecked(task.notify);
                });
            }
        }).start();
    }

    private void saveChanges() {
        if (task == null) return;

        task.title = etTitle.getText().toString();
        task.description = etDesc.getText().toString();
        task.dateTime = selectedDateTime.getTimeInMillis();
        task.status = statusSpinner.getSelectedItem().toString();
        task.notify = switchNotify.isChecked();

        new Thread(() -> {
            taskDao.update(task);
            runOnUiThread(() -> {
                // Anuluj stare powiadomienie
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent cancelIntent = new Intent(EditTaskActivity.this, NotificationReceiver.class);
                PendingIntent cancelPending = PendingIntent.getBroadcast(EditTaskActivity.this, task.id, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmManager.cancel(cancelPending);

                // Nowe tylko jeśli globalne ON
                SharedPreferences prefs = getSharedPreferences("PurrfectPrefs", MODE_PRIVATE);
                boolean globalNotify = prefs.getBoolean("notifications_enabled", false);
                if (task.notify && globalNotify) {
                    scheduleNotification(task);
                }

                Toast.makeText(EditTaskActivity.this, "Meow-ssion updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void showDatePicker(TextView tv) {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDateTime.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            tv.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker(TextView tv) {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            tv.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    private void setupStatusSpinner(Spinner spinner) {
        String[] statusNames = {"not finished", "in process", "completed"};
        StatusAdapter adapter = new StatusAdapter(this, statusNames);
        spinner.setAdapter(adapter);

        ImageView ivArrow = findViewById(R.id.ivArrow);
        if (ivArrow != null) {
            ivArrow.setOnClickListener(v -> spinner.performClick());
        }
        spinner.setPopupBackgroundResource(android.R.color.white);
    }
    private String formatDateWithOrdinal(long dateTimeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateTimeMillis);

        int day = cal.get(Calendar.DAY_OF_MONTH);
        String ordinal = getOrdinalSuffix(day);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM " + day + ordinal + ", yyyy", Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }

    private String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    private void scheduleNotification(TaskEntity task) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task_id", task.id);
        intent.putExtra("task_title", task.title);
        intent.putExtra("task_description", task.description);
        intent.putExtra("task_time", new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(new Date(task.dateTime)));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = task.dateTime;
        if (triggerTime <= System.currentTimeMillis()) {
            triggerTime += 24 * 60 * 60 * 1000L;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
    private void shareTask() {
        if (task == null) {
            Toast.makeText(this, "No task to share! Meow?", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format wiadomości z task details
        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

        String shareText = String.format(
                "Task:\n" +
                        "Title: %s\n" +
                        "Description: %s\n" +
                        "Time: %s | %s\n" +
                        "Status: %s\n" +
                        "Shared from PurrfectPlan!",
                task.title,
                task.description,
                sdfDate.format(new Date(task.dateTime)),
                sdfTime.format(new Date(task.dateTime)),
                task.status,
                task.notify ? "Yes" : "No"
        );

        // Share Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Toast potwierdzenia
        Toast.makeText(this, "Task copied to share!", Toast.LENGTH_SHORT).show();

        startActivity(Intent.createChooser(shareIntent, "Share task via..."));
    }

    public class StatusAdapter extends ArrayAdapter<String> {
        private final String[] titles;
        private final int[] images = {
                R.drawable.not_finished_icon,
                R.drawable.in_process_icon,
                R.drawable.finished_icon
        };

        public StatusAdapter(Context context, String[] titles) {
            super(context, R.layout.item_status, titles);
            this.titles = titles;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        private View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View row = inflater.inflate(R.layout.item_status, parent, false);

            TextView label = row.findViewById(R.id.tvStatusName);
            ImageView icon = row.findViewById(R.id.ivStatusIcon);

            label.setText(titles[position]);
            icon.setImageResource(images[position]);

            LinearLayout layout = (LinearLayout) row;

            if (parent instanceof Spinner) {
                layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                row.setPadding(10, 0, 0, 0);
            } else {
                layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                row.setPadding(20, 20, 0, 20);
            }

            return row;
        }
    }
}
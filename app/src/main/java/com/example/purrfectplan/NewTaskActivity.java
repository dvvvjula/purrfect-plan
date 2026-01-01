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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewTaskActivity extends AppCompatActivity {

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // 1. DATE/TIME PICKERS
        TextView tvDate = findViewById(R.id.tvDateSelect);
        tvDate.setOnClickListener(v -> showDatePicker(tvDate));

        TextView tvTime = findViewById(R.id.tvTimeSelect);
        tvTime.setOnClickListener(v -> showTimePicker(tvTime));

        // 2. STATUS SPINNER
        Spinner spinner = findViewById(R.id.statusSpinner);
        setupStatusSpinner(spinner);

        // 3. NOTIFY SWITCH + LISTENER (NOWE!)
        SwitchCompat switchNotify = findViewById(R.id.switchNotify);
        switchNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences("PurrfectPrefs", MODE_PRIVATE);
            boolean globalNotificationsEnabled = prefs.getBoolean("notifications_enabled", false);

            if (isChecked && !globalNotificationsEnabled) {
                Toast.makeText(NewTaskActivity.this, "You have to turn notifications ON in app Settings first! Meow.", Toast.LENGTH_LONG).show();
                switchNotify.setChecked(false);
                return;
            }
        });

        // 4. SAVE BUTTON
        findViewById(R.id.btnSave).setOnClickListener(v -> saveTaskToRoom());

        // 5. FOOTER NAVIGATION
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
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
        ivArrow.setOnClickListener(v -> spinner.performClick());
        spinner.setPopupBackgroundResource(android.R.color.white);
    }

    private void saveTaskToRoom() {
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDesc = findViewById(R.id.etDesc);
        Spinner spinner = findViewById(R.id.statusSpinner);
        SwitchCompat switchNotify = findViewById(R.id.switchNotify);

        String title = etTitle.getText().toString();
        String description = etDesc.getText().toString();
        String status = spinner.getSelectedItem().toString();
        boolean notify = switchNotify.isChecked();

        long dateTime = selectedDateTime.getTimeInMillis();

        TaskEntity task = new TaskEntity();
        task.title = title;
        task.description = description;
        task.dateTime = dateTime;
        task.status = status;
        task.notify = notify;

        // Insert (void DAO)
        AppDatabase.getInstance(this).taskDao().insert(task);

        // Fake ID dla PendingIntent
        task.id = (int) (System.currentTimeMillis() % 100000);

        // Sprawdź globalne ustawienia PRZED planowaniem
        SharedPreferences prefs = getSharedPreferences("PurrfectPrefs", MODE_PRIVATE);
        boolean globalNotifyEnabled = prefs.getBoolean("notifications_enabled", false);

        if (notify && globalNotifyEnabled) {
            scheduleNotification(task);
        }

        Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
        finish();
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

    // StatusAdapter bez zmian
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

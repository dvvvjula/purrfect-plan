package com.example.purrfectplan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.purrfectplan.room.AppDatabase;
import com.example.purrfectplan.room.TaskDao;
import com.example.purrfectplan.room.TaskEntity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private AppDatabase db;
    private TaskDao taskDao;
    private TaskEntity task; // obiekt taska, który edytujemy
    private Calendar selectedDateTime = Calendar.getInstance();

    private AppCompatEditText etTitle, etDesc;
    private TextView tvDateSelect, tvTimeSelect;
    private Spinner statusSpinner;
    private SwitchCompat switchNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // --- 1. INIT ROOM ---
        db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        // --- 2. INIT UI ---
        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        tvDateSelect = findViewById(R.id.tvDateSelect);
        tvTimeSelect = findViewById(R.id.tvTimeSelect);
        statusSpinner = findViewById(R.id.statusSpinner);
        switchNotify = findViewById(R.id.switchNotify);

        // --- 3. GET TASK ID ---
        int taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            loadTask(taskId); // metoda wczytuje dane do pól
        }

        // --- 4. DATE/TIME PICKERS ---
        tvDateSelect.setOnClickListener(v -> showDatePicker(tvDateSelect));
        tvTimeSelect.setOnClickListener(v -> showTimePicker(tvTimeSelect));

        // --- 5. STATUS SPINNER ---
        setupStatusSpinner(statusSpinner);

        // --- 6. SAVE BUTTON ---
        MaterialButton btnSave = findViewById(R.id.btnSaveChanges);
        btnSave.setOnClickListener(v -> saveChanges());

        // --- 7. FOOTER & SHARE ---
        findViewById(R.id.shareLayout).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Checking my meow-ssion progress in PurrfectPlan!");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    // --- LOAD TASK DATA ---
    private void loadTask(int taskId) {
        new Thread(() -> {
            task = taskDao.getTaskById(taskId);
            if (task != null) {
                runOnUiThread(() -> {
                    etTitle.setText(task.title);
                    etDesc.setText(task.description);

                    // Data i godzina
                    selectedDateTime.setTimeInMillis(task.dateTime);
                    SimpleDateFormat sdfDate = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
                    SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                    tvDateSelect.setText(sdfDate.format(selectedDateTime.getTime()));
                    tvTimeSelect.setText(sdfTime.format(selectedDateTime.getTime()));

                    // Status
                    String[] statuses = {"not finished", "in process", "completed"};
                    int statusIndex = 0;
                    for (int i = 0; i < statuses.length; i++) {
                        if (statuses[i].equals(task.status)) {
                            statusIndex = i;
                            break;
                        }
                    }
                    statusSpinner.setSelection(statusIndex);

                    // Notify
                    switchNotify.setChecked(task.notify);
                });
            }
        }).start();
    }

    // --- SAVE CHANGES ---
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
                Toast.makeText(EditTaskActivity.this, "Meow-ssion updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    // --- DATE PICKER ---
    private void showDatePicker(TextView tv) {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDateTime.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            tv.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    // --- TIME PICKER ---
    private void showTimePicker(TextView tv) {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hour, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
            selectedDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            tv.setText(sdf.format(selectedDateTime.getTime()));
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    // --- STATUS SPINNER ---
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

    // --- ADAPTER STATUS ---
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
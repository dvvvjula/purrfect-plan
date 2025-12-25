package com.example.purrfectplan;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTaskActivity extends AppCompatActivity {

    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // --- 1. WYBÓR DATY I GODZINY ---
        TextView tvDate = findViewById(R.id.tvDateSelect);
        tvDate.setOnClickListener(v -> showDatePicker(tvDate));

        TextView tvTime = findViewById(R.id.tvTimeSelect);
        tvTime.setOnClickListener(v -> showTimePicker(tvTime));

        // --- 2. KONFIGURACJA SPINNERA STATUSU ---
        Spinner spinner = findViewById(R.id.statusSpinner);
        setupStatusSpinner(spinner);

        // --- 3. PRZYCISK SAVE ---
        findViewById(R.id.btnSave).setOnClickListener(v -> saveTaskToRoom());

        // --- 4. NAWIGACJA FOOTER ---
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

        // Dodaj to pod setupStatusSpinner(spinner);
        ImageView ivArrow = findViewById(R.id.ivArrow);
        ivArrow.setOnClickListener(v -> spinner.performClick());

// Wymuszenie białego tła listy rozwijanej
        spinner.setPopupBackgroundResource(android.R.color.white);
    }


    private void saveTaskToRoom() {
        // Logika zapisu do bazy Room
        Toast.makeText(this, "Task saved to your meow-list!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ADAPTER JAKO KLASA WEWNĘTRZNA (Poprawiona)
    public class StatusAdapter extends ArrayAdapter<String> {
        private final String[] titles;
        // Upewnij się, że te nazwy drawable są poprawne w Twoim projekcie!
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
                // WIDOK W RAMCE:
                // Ustawiamy mały padding z lewej strony, aby ikonka nie dotykała linii obramowania
                layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                row.setPadding(10, 0, 0, 0);
            } else {
                // WIDOK NA LIŚCIE:
                // Większe odstępy, żeby lista wyglądała na czystą i przestronną
                layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                row.setPadding(20, 20, 0, 20);
            }

            return row;
        }
    }
}
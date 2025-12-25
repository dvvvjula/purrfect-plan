package com.example.purrfectplan;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // --- 1. USTAWIANIE AKTUALNEJ DATY ---
        TextView tvDate = findViewById(R.id.tvDate);
        tvDate.setText(getFormattedDate());

        // --- 2. NAWIGACJA (LOGOUT I SETTINGS) ---
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // --- 3. ADD NEW TASK (Z TWOIM OBRAZKIEM Z FIGMY) ---
        View btnAdd = findViewById(R.id.btnAdd);
        // Teraz szukamy tylko jednego obrazka ivPurrfectDialog
        ImageView purrfectDialog = findViewById(R.id.ivPurrfectDialog);

        btnAdd.setOnClickListener(v -> {
            if (purrfectDialog != null) {
                purrfectDialog.setVisibility(View.VISIBLE);
            }

            // Czekamy 800ms, żeby użytkownik zobaczył dymek, potem idziemy dalej
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(TaskListActivity.this, NewTaskActivity.class);
                startActivity(intent);

                // Ukrywamy dymek z powrotem, żeby był gotowy na następny raz
                if (purrfectDialog != null) {
                    purrfectDialog.setVisibility(View.INVISIBLE);
                }
            }, 800);
        });

        // --- 4. OBSŁUGA ZADAŃ (PRZEKREŚLANIE I EDYCJA) ---
        setupTaskRow(R.id.taskRow1);
        setupTaskRow(R.id.taskRow2);
        setupTaskRow(R.id.taskRow3);
    }

    private void setupTaskRow(int rowId) {
        View row = findViewById(rowId);
        if (row == null) return;

        // Wyciągamy elementy: index 0 to more_icon, 1 to checkbox, 2 to text
        ImageView moreIcon = (ImageView) ((android.view.ViewGroup) row).getChildAt(0);
        View checkbox = ((android.view.ViewGroup) row).getChildAt(1);
        TextView taskText = (TextView) ((android.view.ViewGroup) row).getChildAt(2);

        // Ikona kropek -> Edycja zadania
        moreIcon.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, EditTaskActivity.class);
            startActivity(intent);
        });

        // Kliknięcie w kwadrat -> Przekreślenie czarne + łapka
        checkbox.setOnClickListener(v -> {
            // Przekreślenie
            taskText.setPaintFlags(taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            // Upewniamy się, że kolor zostaje czarny
            taskText.setTextColor(Color.BLACK);
            // Podmiana tła kwadratu na ikonę łapki
            checkbox.setBackgroundResource(R.drawable.paw_icon);
        });
    }

    // Funkcja generująca datę: np. 25th of Dec, 2025
    private String getFormattedDate() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String dayWithSuffix = day + getDaySuffix(day);
        String monthAndYear = new SimpleDateFormat(" 'of' MMM, yyyy", Locale.ENGLISH).format(cal.getTime());

        return dayWithSuffix + monthAndYear;
    }

    private String getDaySuffix(int n) {
        if (n >= 11 && n <= 13) return "th";
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
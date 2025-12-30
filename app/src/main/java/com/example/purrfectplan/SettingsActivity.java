package com.example.purrfectplan;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private View aboutDialogContainer;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Inicjalizacja widoków
        aboutDialogContainer = findViewById(R.id.aboutDialogContainer);
        ImageView btnInfo = findViewById(R.id.btnInfo);
        TextView tvAboutContent = findViewById(R.id.tvAboutContent);
        LinearLayout btnGoBack = findViewById(R.id.btnGoBack);
        ImageView btnLogout = findViewById(R.id.btnLogout);
        SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);

        // 2. SharedPreferences (zapisywanie ustawień)
        sharedPreferences = getSharedPreferences("PurrfectPrefs", MODE_PRIVATE);
        boolean isNotifyEnabled = sharedPreferences.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(isNotifyEnabled);

        // 3. Tekst "About"
        String aboutText = "<b>PurrfectPlan</b> is a task planner with a cat’s personality. It helps you stay organized, focused, and just a little more entertained while doing it.<br><br>" +
                "Turn everyday to-dos into meow-ssions, track your progress paw by paw, and let a slightly judgmental cat keep an eye on things.<br><br>" +
                "Because even the best humans need reminders.<br>And cats definitely do.";

        if (tvAboutContent != null) {
            tvAboutContent.setText(Html.fromHtml(aboutText, Html.FROM_HTML_MODE_LEGACY));
        }

        // 4. Obsługa kliknięcia Info (Toggle)
        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> {
                if (aboutDialogContainer.getVisibility() == View.VISIBLE) {
                    aboutDialogContainer.setVisibility(View.INVISIBLE);
                } else {
                    aboutDialogContainer.setVisibility(View.VISIBLE);
                }
            });
        }

        // 5. Obsługa Switcha Powiadomień
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Zapisujemy stan w pamięci telefonu
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();

            if (isChecked) {
                requestNotificationPermission();
            }
        });

        // 6. Wylogowanie (Powrót do MainActivity / activity_main.xml)
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                // Flagami upewniamy się, że użytkownik nie wróci do Settings przyciskiem wstecz
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // 7. Powrót do Task List
        if (btnGoBack != null) {
            btnGoBack.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, TaskListActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled! Meow!", Toast.LENGTH_SHORT).show();
            } else {
                // Jeśli użytkownik odmówił, wyłączamy switch
                SwitchCompat switchNotifications = findViewById(R.id.switchNotifications);
                switchNotifications.setChecked(false);
                sharedPreferences.edit().putBoolean("notifications_enabled", false).apply();
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
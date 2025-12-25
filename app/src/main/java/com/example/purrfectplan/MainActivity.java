package com.example.purrfectplan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Znajdź przycisk i łapki
        Button btnNext = findViewById(R.id.btnNext);
        final ImageView paw1 = findViewById(R.id.paw1);
        final ImageView paw2 = findViewById(R.id.paw2);
        final ImageView paw3 = findViewById(R.id.paw3);
        final ImageView paw4 = findViewById(R.id.paw4);

        // 2. Obsługa kliknięcia
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatePaws(paw1, paw2, paw3, paw4);
            }
        });
    }

    private void animatePaws(View p1, View p2, View p3, View p4) {
        Handler handler = new Handler();
        View[] paws = {p1, p2, p3, p4};

        for (int i = 0; i < paws.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                paws[index].setVisibility(View.VISIBLE);
                paws[index].setAlpha(0f);
                paws[index].animate().alpha(1f).scaleX(1.2f).scaleY(1.2f).setDuration(200).withEndAction(() -> {
                    paws[index].animate().scaleX(1f).scaleY(1f).setDuration(100);
                });
            }, i * 300);
        }

        // Przejście do TaskListActivity po 1.5 sekundy
        handler.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, TaskListActivity.class);
            startActivity(intent);
        }, 1500);
    }
}
package com.example.purrfectplan;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TaskListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ta linia poniżej jest kluczowa - łączy plik XML z tą klasą
        setContentView(R.layout.activity_task_list);
    }
}

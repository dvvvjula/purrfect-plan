package com.example.purrfectplan.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public long dateTime;
    public String time;
    public String status;
    public boolean notify;
}






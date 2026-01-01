package com.example.purrfectplan.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Query("SELECT * FROM tasks WHERE dateTime BETWEEN :startOfDay AND :endOfDay ORDER BY dateTime ASC")
    List<TaskEntity> getTasksForToday(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity getTaskById(int taskId);
}







// plik: app/src/main/java/com/example/purrfectplan/room/TaskDao.java
package com.example.purrfectplan.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);   // <-- TO MUSI BYĆ

    @Query("SELECT * FROM tasks WHERE dateTime BETWEEN :startOfDay AND :endOfDay ORDER BY dateTime ASC")
    List<TaskEntity> getTasksForToday(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity getTaskById(int taskId);
}








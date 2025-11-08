package com.mob.proyectoandroid.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mob.proyectoandroid.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTasks(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllTasks(userId: String)

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)

    @Query("SELECT * FROM tasks WHERE userId = :userId AND completed = 0 ORDER BY createdAt DESC")
    fun getWidgetTasks(userId: String): List<Task>
}
package com.mob.proyectoandroid.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("completed")
    val completed: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("priority")
    val priority: Int = TaskPriority.MEDIUM.value,

    @SerializedName("category")
    val category: String = TaskCategory.OTHER.value,

    @SerializedName("due_date")
    val dueDate: String? = null,

    val isSynced: Boolean = true
) {
    // Métodos auxiliares con nombres diferentes para evitar conflictos con Room
    fun priorityEnum(): TaskPriority = TaskPriority.fromValue(priority)
    fun categoryEnum(): TaskCategory = TaskCategory.fromValue(category)
}
package com.mob.proyectoandroid.data.model

import com.google.gson.annotations.SerializedName

data class TaskRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("completed")
    val completed: Boolean = false,

    @SerializedName("priority")
    val priority: Int = TaskPriority.MEDIUM.value,

    @SerializedName("category")
    val category: String = TaskCategory.OTHER.value,

    @SerializedName("due_date")
    val dueDate: String? = null
)
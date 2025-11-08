package com.mob.proyectoandroid.data.model

import com.google.gson.annotations.SerializedName

data class TaskUpdateRequest(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("completed")
    val completed: Boolean? = null,

    @SerializedName("priority")
    val priority: Int? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("due_date")
    val dueDate: String? = null
)
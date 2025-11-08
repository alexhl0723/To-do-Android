package com.mob.proyectoandroid.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("created_at")
    val createdAt: String? = null
)
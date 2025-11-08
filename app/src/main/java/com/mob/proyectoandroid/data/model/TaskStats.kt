package com.mob.proyectoandroid.data.model

data class TaskStats(
    val total: Int,
    val completed: Int,
    val active: Int,
    val unsynced: Int
)
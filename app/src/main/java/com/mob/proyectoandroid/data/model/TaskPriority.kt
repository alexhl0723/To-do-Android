package com.mob.proyectoandroid.data.model

enum class TaskPriority(val value: Int, val displayName: String, val colorResId: Int) {
    LOW(1, "Baja", android.R.color.holo_green_light),
    MEDIUM(2, "Media", android.R.color.holo_orange_light),
    HIGH(3, "Alta", android.R.color.holo_red_light);

    companion object {
        fun fromValue(value: Int): TaskPriority {
            return values().find { it.value == value } ?: MEDIUM
        }
    }
}
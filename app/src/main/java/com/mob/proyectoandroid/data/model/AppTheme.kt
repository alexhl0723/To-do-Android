package com.mob.proyectoandroid.data.model

enum class AppTheme(val displayName: String) {
    LIGHT("Claro"),
    DARK("Oscuro"),
    SYSTEM("Sistema");

    companion object {
        fun fromString(value: String): AppTheme {
            return values().find { it.name == value } ?: SYSTEM
        }
    }
}
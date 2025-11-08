package com.mob.proyectoandroid.data.model

enum class TaskCategory(val value: String, val displayName: String, val emoji: String) {
    WORK("work", "Trabajo", "💼"),
    PERSONAL("personal", "Personal", "👤"),
    SHOPPING("shopping", "Compras", "🛒"),
    HEALTH("health", "Salud", "❤️"),
    STUDY("study", "Estudio", "📚"),
    HOME("home", "Casa", "🏠"),
    OTHER("other", "Otro", "📌");

    companion object {
        fun fromValue(value: String): TaskCategory {
            return values().find { it.value == value } ?: OTHER
        }
    }
}
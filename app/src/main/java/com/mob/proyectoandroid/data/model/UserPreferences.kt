package com.mob.proyectoandroid.data.model

data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val sortOption: SortOption = SortOption.DATE_CREATED,
    val showCompletedTasks: Boolean = true,
    val enableNotifications: Boolean = true,
    val syncFrequency: Int = 15,
    val confirmDelete: Boolean = true,
    val compactView: Boolean = false
)
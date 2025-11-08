package com.mob.proyectoandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob.proyectoandroid.data.model.AppTheme
import com.mob.proyectoandroid.data.model.SortOption
import com.mob.proyectoandroid.data.model.UserPreferences
import com.mob.proyectoandroid.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = settingsManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val currentTheme: StateFlow<AppTheme> = userPreferences
        .map { it.theme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    val currentSortOption: StateFlow<SortOption> = userPreferences
        .map { it.sortOption }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOption.DATE_CREATED
        )

    suspend fun updateTheme(theme: AppTheme) {
        settingsManager.updateTheme(theme)
    }

    suspend fun updateSortOption(sortOption: SortOption) {
        settingsManager.updateSortOption(sortOption)
    }

    suspend fun updateShowCompleted(show: Boolean) {
        settingsManager.updateShowCompleted(show)
    }

    suspend fun updateEnableNotifications(enable: Boolean) {
        settingsManager.updateEnableNotifications(enable)
    }

    suspend fun updateSyncFrequency(minutes: Int) {
        settingsManager.updateSyncFrequency(minutes)
    }

    suspend fun updateConfirmDelete(confirm: Boolean) {
        settingsManager.updateConfirmDelete(confirm)
    }

    suspend fun updateCompactView(compact: Boolean) {
        settingsManager.updateCompactView(compact)
    }
}
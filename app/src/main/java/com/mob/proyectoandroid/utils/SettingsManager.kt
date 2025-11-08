package com.mob.proyectoandroid.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mob.proyectoandroid.data.model.AppTheme
import com.mob.proyectoandroid.data.model.SortOption
import com.mob.proyectoandroid.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val SYNC_FREQUENCY = intPreferencesKey("sync_frequency")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val COMPACT_VIEW = booleanPreferencesKey("compact_view")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                theme = AppTheme.fromString(preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name),
                sortOption = SortOption.fromString(preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.DATE_CREATED.name),
                showCompletedTasks = preferences[PreferencesKeys.SHOW_COMPLETED] ?: true,
                enableNotifications = preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] ?: true,
                syncFrequency = preferences[PreferencesKeys.SYNC_FREQUENCY] ?: 15,
                confirmDelete = preferences[PreferencesKeys.CONFIRM_DELETE] ?: true,
                compactView = preferences[PreferencesKeys.COMPACT_VIEW] ?: false
            )
        }

    suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun updateSortOption(sortOption: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = sortOption.name
        }
    }

    suspend fun updateShowCompleted(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED] = show
        }
    }

    suspend fun updateEnableNotifications(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] = enable
        }
    }

    suspend fun updateSyncFrequency(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_FREQUENCY] = minutes
        }
    }

    suspend fun updateConfirmDelete(confirm: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFIRM_DELETE] = confirm
        }
    }

    suspend fun updateCompactView(compact: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPACT_VIEW] = compact
        }
    }
}
package com.mob.proyectoandroid.ui.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mob.proyectoandroid.data.model.AppTheme
import com.mob.proyectoandroid.data.model.SortOption
import com.mob.proyectoandroid.databinding.ActivitySettingsBinding
import com.mob.proyectoandroid.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
        observePreferences()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Configuración"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViews() {
        binding.apply {
            // Tema
            cardTheme.setOnClickListener {
                showThemeDialog()
            }

            // Ordenamiento
            cardSort.setOnClickListener {
                showSortDialog()
            }

            // Mostrar tareas completadas
            switchShowCompleted.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    viewModel.updateShowCompleted(isChecked)
                }
            }

            // Notificaciones
            switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    viewModel.updateEnableNotifications(isChecked)
                }
            }

            // Frecuencia de sincronización
            cardSyncFrequency.setOnClickListener {
                showSyncFrequencyDialog()
            }

            // Confirmar eliminación
            switchConfirmDelete.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    viewModel.updateConfirmDelete(isChecked)
                }
            }

            // Vista compacta
            switchCompactView.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    viewModel.updateCompactView(isChecked)
                }
            }

            // Estadísticas
            cardStats.setOnClickListener {
                // TODO: Abrir pantalla de estadísticas
                MaterialAlertDialogBuilder(this@SettingsActivity)
                    .setTitle("Próximamente")
                    .setMessage("Esta función estará disponible pronto")
                    .setPositiveButton("OK", null)
                    .show()
            }

            // Exportar datos
            cardExport.setOnClickListener {
                showExportDialog()
            }

            // Acerca de
            cardAbout.setOnClickListener {
                showAboutDialog()
            }
        }
    }

    private fun observePreferences() {
        lifecycleScope.launch {
            viewModel.userPreferences.collect { prefs ->
                binding.apply {
                    tvThemeValue.text = prefs.theme.displayName
                    tvSortValue.text = prefs.sortOption.displayName

                    // Actualizar switches sin trigger listeners
                    switchShowCompleted.setOnCheckedChangeListener(null)
                    switchShowCompleted.isChecked = prefs.showCompletedTasks
                    switchShowCompleted.setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            viewModel.updateShowCompleted(isChecked)
                        }
                    }

                    switchNotifications.setOnCheckedChangeListener(null)
                    switchNotifications.isChecked = prefs.enableNotifications
                    switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            viewModel.updateEnableNotifications(isChecked)
                        }
                    }

                    switchConfirmDelete.setOnCheckedChangeListener(null)
                    switchConfirmDelete.isChecked = prefs.confirmDelete
                    switchConfirmDelete.setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            viewModel.updateConfirmDelete(isChecked)
                        }
                    }

                    switchCompactView.setOnCheckedChangeListener(null)
                    switchCompactView.isChecked = prefs.compactView
                    switchCompactView.setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            viewModel.updateCompactView(isChecked)
                        }
                    }

                    tvSyncFrequencyValue.text = when (prefs.syncFrequency) {
                        -1 -> "Manual"
                        else -> "${prefs.syncFrequency} minutos"
                    }
                }
            }
        }
    }

    private fun showThemeDialog() {
        val themes = AppTheme.values().map { it.displayName }.toTypedArray()
        val currentTheme = viewModel.currentTheme.value
        val currentIndex = AppTheme.values().indexOf(currentTheme)

        MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar tema")
            .setSingleChoiceItems(themes, currentIndex) { dialog, which ->
                val selectedTheme = AppTheme.values()[which]
                lifecycleScope.launch {
                    viewModel.updateTheme(selectedTheme)
                    applyTheme(selectedTheme)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSortDialog() {
        val sortOptions = SortOption.values().map { it.displayName }.toTypedArray()
        val currentSort = viewModel.currentSortOption.value
        val currentIndex = SortOption.values().indexOf(currentSort)

        MaterialAlertDialogBuilder(this)
            .setTitle("Ordenar tareas por")
            .setSingleChoiceItems(sortOptions, currentIndex) { dialog, which ->
                val selectedSort = SortOption.values()[which]
                lifecycleScope.launch {
                    viewModel.updateSortOption(selectedSort)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSyncFrequencyDialog() {
        val frequencies = arrayOf("15 minutos", "30 minutos", "1 hora", "2 horas", "Manual")
        val values = arrayOf(15, 30, 60, 120, -1)

        MaterialAlertDialogBuilder(this)
            .setTitle("Frecuencia de sincronización")
            .setItems(frequencies) { dialog, which ->
                val minutes = values[which]
                lifecycleScope.launch {
                    viewModel.updateSyncFrequency(minutes)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showExportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exportar datos")
            .setMessage("¿Qué formato deseas usar?")
            .setPositiveButton("CSV") { _, _ ->
                // TODO: Implementar exportación CSV
                MaterialAlertDialogBuilder(this)
                    .setTitle("Próximamente")
                    .setMessage("Exportación CSV estará disponible pronto")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("JSON") { _, _ ->
                // TODO: Implementar exportación JSON
                MaterialAlertDialogBuilder(this)
                    .setTitle("Próximamente")
                    .setMessage("Exportación JSON estará disponible pronto")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Acerca de")
            .setMessage("""
                📱 Listora App v1.0
                
                Desarrollado con:
                • Kotlin
                • MVVM + Clean Architecture
                • Supabase Backend
                • Material Design 3
                • Room Database
                • WorkManager
                
                © 2025 Listora App
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun applyTheme(theme: AppTheme) {
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
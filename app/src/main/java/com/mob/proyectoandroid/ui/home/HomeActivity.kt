// ui/home/HomeActivity.kt (VERSIÓN CORREGIDA)
package com.mob.proyectoandroid.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.mob.proyectoandroid.R
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.model.Task
import com.mob.proyectoandroid.data.model.TaskCategory
import com.mob.proyectoandroid.data.model.TaskFilter
import com.mob.proyectoandroid.data.model.TaskPriority
import com.mob.proyectoandroid.databinding.ActivityHomeBinding
import com.mob.proyectoandroid.ui.adapters.TaskAdapter
import com.mob.proyectoandroid.ui.login.LoginActivity
import com.mob.proyectoandroid.utils.hide
import com.mob.proyectoandroid.utils.show
import com.mob.proyectoandroid.utils.showSnackbar
import com.mob.proyectoandroid.viewmodel.HomeViewModel
import com.mob.proyectoandroid.workers.WorkManagerInitializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    @Inject
    lateinit var workManagerInitializer: WorkManagerInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⚠️ NO intentar aplicar tema aquí - TodoApplication ya lo hace
        // El tema ya está aplicado globalmente

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mis Tareas"

        setupWorkManager()
        setupRecyclerView()
        setupViews()
        setupObservers()
        setupFilterChips()
    }

    private fun setupWorkManager() {
        workManagerInitializer.setupPeriodicSync()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> showEditTaskDialog(task) },
            onTaskLongClick = { task -> showDeleteConfirmation(task) },
            onCheckboxClick = { task -> viewModel.toggleTaskCompletion(task) }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = taskAdapter
        }
    }

    private fun setupViews() {
        binding.apply {
            fabAddTask.setOnClickListener {
                showAddTaskDialog()
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.syncTasks()
            }
        }
    }

    private fun setupFilterChips() {
        binding.apply {
            chipAll.setOnClickListener {
                viewModel.setFilter(TaskFilter.ALL)
                updateChipSelection(chipAll)
            }

            chipActive.setOnClickListener {
                viewModel.setFilter(TaskFilter.ACTIVE)
                updateChipSelection(chipActive)
            }

            chipCompleted.setOnClickListener {
                viewModel.setFilter(TaskFilter.COMPLETED)
                updateChipSelection(chipCompleted)
            }

            // Chip "All" seleccionado por defecto
            updateChipSelection(chipAll)
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        binding.apply {
            listOf(chipAll, chipActive, chipCompleted).forEach { chip ->
                chip.isChecked = chip == selectedChip
            }
        }
    }

    private fun setupObservers() {
        viewModel.tasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)

            if (tasks.isEmpty()) {
                binding.tvEmptyState.show()
                binding.rvTasks.hide()
            } else {
                binding.tvEmptyState.hide()
                binding.rvTasks.show()
            }
        }

        viewModel.taskStats.observe(this) { stats ->
            binding.apply {
                tvTotalTasks.text = "Total: ${stats.total}"
                tvActiveTasks.text = "Activas: ${stats.active}"
                tvCompletedTasks.text = "Completadas: ${stats.completed}"

                if (stats.unsynced > 0) {
                    tvUnsyncedWarning.text = "${stats.unsynced} sin sincronizar"
                    tvUnsyncedWarning.show()
                } else {
                    tvUnsyncedWarning.hide()
                }
            }
        }

        viewModel.syncState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.show()
                    }
                }
                is Resource.Success -> {
                    binding.progressBar.hide()
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    binding.progressBar.hide()
                    binding.swipeRefresh.isRefreshing = false
                    binding.root.showSnackbar(resource.message ?: "Error al sincronizar")
                }
            }
        }

        viewModel.taskOperationState.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.root.showSnackbar("Operación exitosa")
                }
                is Resource.Error -> {
                    binding.root.showSnackbar(resource.message ?: "Error en la operación")
                }
                is Resource.Loading -> {
                    // Optional: show loading indicator
                }
            }
        }

        // Observar filtro actual
        lifecycleScope.launch {
            viewModel.currentFilter.collectLatest { filter ->
                when (filter) {
                    TaskFilter.ALL -> updateChipSelection(binding.chipAll)
                    TaskFilter.ACTIVE -> updateChipSelection(binding.chipActive)
                    TaskFilter.COMPLETED -> updateChipSelection(binding.chipCompleted)
                }
            }
        }
    }

    private fun showAddTaskDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        // Referencias a los views del dialog
        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTaskTitle)
        val etDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTaskDescription)
        val chipGroupPriority = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupPriority)
        val chipPriorityLow = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityLow)
        val chipPriorityMedium = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityMedium)
        val chipPriorityHigh = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityHigh)
        val actvCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.actvCategory)

        // Configurar categorías en el dropdown
        val categories = TaskCategory.values().map { "${it.emoji} ${it.displayName}" }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(categoryAdapter)
        actvCategory.setText("${TaskCategory.OTHER.emoji} ${TaskCategory.OTHER.displayName}", false)

        // Seleccionar prioridad media por defecto
        chipPriorityMedium.isChecked = true

        dialogBuilder.setView(dialogView)
            .setTitle("Nueva Tarea")
            .setPositiveButton("Crear") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val categoryText = actvCategory.text.toString()

                // Obtener prioridad seleccionada
                val selectedPriority = when (chipGroupPriority.checkedChipId) {
                    R.id.chipPriorityLow -> TaskPriority.LOW
                    R.id.chipPriorityHigh -> TaskPriority.HIGH
                    else -> TaskPriority.MEDIUM
                }

                // Obtener categoría seleccionada
                val selectedCategory = TaskCategory.values().find {
                    categoryText.contains(it.displayName)
                } ?: TaskCategory.OTHER

                if (title.isNotEmpty()) {
                    viewModel.createTask(
                        title = title,
                        description = description.ifEmpty { null },
                        priority = selectedPriority,
                        category = selectedCategory
                    )
                } else {
                    binding.root.showSnackbar("El título es requerido")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)

        // Referencias a los views del dialog
        val etTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTaskTitle)
        val etDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTaskDescription)
        val chipGroupPriority = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupPriority)
        val chipPriorityLow = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityLow)
        val chipPriorityMedium = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityMedium)
        val chipPriorityHigh = dialogView.findViewById<com.google.android.material.chip.Chip>(R.id.chipPriorityHigh)
        val actvCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.actvCategory)

        // Llenar con datos existentes
        etTitle.setText(task.title)
        etDescription.setText(task.description)

        // Configurar categorías
        val categories = TaskCategory.values().map { "${it.emoji} ${it.displayName}" }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(categoryAdapter)

        // Seleccionar categoría actual
        val currentCategory = task.categoryEnum()
        actvCategory.setText("${currentCategory.emoji} ${currentCategory.displayName}", false)

        // Seleccionar prioridad actual
        when (task.priorityEnum()) {
            TaskPriority.LOW -> chipPriorityLow.isChecked = true
            TaskPriority.MEDIUM -> chipPriorityMedium.isChecked = true
            TaskPriority.HIGH -> chipPriorityHigh.isChecked = true
        }

        dialogBuilder.setView(dialogView)
            .setTitle("Editar Tarea")
            .setPositiveButton("Guardar") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val categoryText = actvCategory.text.toString()

                // Obtener prioridad seleccionada
                val selectedPriority = when (chipGroupPriority.checkedChipId) {
                    R.id.chipPriorityLow -> TaskPriority.LOW
                    R.id.chipPriorityHigh -> TaskPriority.HIGH
                    else -> TaskPriority.MEDIUM
                }

                // Obtener categoría seleccionada
                val selectedCategory = TaskCategory.values().find {
                    categoryText.contains(it.displayName)
                } ?: TaskCategory.OTHER

                if (title.isNotEmpty()) {
                    viewModel.updateTask(
                        taskId = task.id,
                        title = title,
                        description = description.ifEmpty { null },
                        completed = null,
                        priority = selectedPriority,
                        category = selectedCategory
                    )
                } else {
                    binding.root.showSnackbar("El título es requerido")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                viewModel.deleteTask(task.id)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showDeleteCompletedConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Completadas")
            .setMessage("¿Eliminar todas las tareas completadas?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                viewModel.deleteCompletedTasks()
                binding.root.showSnackbar("Tareas completadas eliminadas")
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)

        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Buscar tareas..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            R.id.action_refresh -> {
                viewModel.syncTasks()
                true
            }
            R.id.action_delete_completed -> {
                showDeleteCompletedConfirmation()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, com.mob.proyectoandroid.ui.settings.SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { dialog, _ ->
                workManagerInitializer.cancelPeriodicSync()
                viewModel.signOut {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // No cancelar el work aquí, solo al hacer logout
    }
}
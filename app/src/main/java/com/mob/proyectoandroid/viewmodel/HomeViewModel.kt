package com.mob.proyectoandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.model.Task
import com.mob.proyectoandroid.data.model.TaskCategory
import com.mob.proyectoandroid.data.model.TaskFilter
import com.mob.proyectoandroid.data.model.TaskPriority
import com.mob.proyectoandroid.data.model.TaskStats
import com.mob.proyectoandroid.data.repository.AuthRepository
import com.mob.proyectoandroid.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    // Filtros y búsqueda
    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Tareas filtradas y buscadas
    val tasks: LiveData<List<Task>> = taskRepository.getLocalTasks(userId)
        .combine(_currentFilter) { tasks, filter ->
            when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.ACTIVE -> tasks.filter { !it.completed }
                TaskFilter.COMPLETED -> tasks.filter { it.completed }
            }
        }
        .combine(_searchQuery) { tasks, query ->
            if (query.isBlank()) {
                tasks
            } else {
                tasks.filter { task ->
                    task.title.contains(query, ignoreCase = true) ||
                            task.description?.contains(query, ignoreCase = true) == true
                }
            }
        }
        .asLiveData()

    // Estadísticas
    val taskStats: LiveData<TaskStats> = taskRepository.getLocalTasks(userId)
        .map { tasks ->
            TaskStats(
                total = tasks.size,
                completed = tasks.count { it.completed },
                active = tasks.count { !it.completed },
                unsynced = tasks.count { !it.isSynced }
            )
        }
        .asLiveData()

    private val _syncState = MutableLiveData<Resource<List<Task>>>()
    val syncState: LiveData<Resource<List<Task>>> = _syncState

    private val _taskOperationState = MutableLiveData<Resource<Unit>>()
    val taskOperationState: LiveData<Resource<Unit>> = _taskOperationState

    init {
        syncTasks()
    }

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun syncTasks() {
        viewModelScope.launch {
            _syncState.value = Resource.Loading()
            val result = taskRepository.syncTasks()
            _syncState.value = result
        }
    }

    fun createTask(
        title: String,
        description: String?,
        priority: TaskPriority = TaskPriority.MEDIUM,
        category: TaskCategory = TaskCategory.OTHER
    ): Job { // <-- 1. AÑADE EL TIPO DE RETORNO

        return viewModelScope.launch { // <-- 2. AÑADE 'return'
            _taskOperationState.value = Resource.Loading()
            val result = taskRepository.createTask(title, description, priority, category)
            _taskOperationState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Error desconocido")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun updateTask(
        taskId: String,
        title: String?,
        description: String?,
        completed: Boolean?,
        priority: TaskPriority? = null,
        category: TaskCategory? = null
    ) {
        viewModelScope.launch {
            _taskOperationState.value = Resource.Loading()
            val result = taskRepository.updateTask(taskId, title, description, completed, priority, category)
            _taskOperationState.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Error desconocido")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _taskOperationState.value = Resource.Loading()
            val result = taskRepository.deleteTask(taskId)
            _taskOperationState.value = result
        }
    }

    fun toggleTaskCompletion(task: Task) {
        updateTask(task.id, null, null, !task.completed)
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            val completedTasks = tasks.value?.filter { it.completed } ?: emptyList()
            completedTasks.forEach { task ->
                taskRepository.deleteTask(task.id)
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSuccess()
        }
    }
}
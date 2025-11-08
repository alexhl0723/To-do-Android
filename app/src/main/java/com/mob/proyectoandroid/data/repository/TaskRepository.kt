package com.mob.proyectoandroid.data.repository

import com.mob.proyectoandroid.data.local.TaskDao
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.model.Task
import com.mob.proyectoandroid.data.model.TaskCategory
import com.mob.proyectoandroid.data.model.TaskPriority
import com.mob.proyectoandroid.data.model.TaskRequest
import com.mob.proyectoandroid.data.model.TaskUpdateRequest
import com.mob.proyectoandroid.data.network.SupabaseApiService
import com.mob.proyectoandroid.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val api: SupabaseApiService,
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager
) {

    fun getLocalTasks(userId: String): Flow<List<Task>> {
        return taskDao.getAllTasks(userId)
    }

    suspend fun syncTasks(): Resource<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getAccessToken() ?: return@withContext Resource.Error<List<Task>>("No autenticado")
                val userId = preferenceManager.getUserId() ?: return@withContext Resource.Error<List<Task>>("Usuario no encontrado")

                val response = api.getTasks("Bearer $token", "eq.$userId")

                if (response.isSuccessful && response.body() != null) {
                    val tasks = response.body()!!
                    taskDao.deleteAllTasks(userId)
                    taskDao.insertTasks(tasks)
                    Resource.Success(tasks)
                } else {
                    Resource.Error("Error al sincronizar tareas")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error syncing tasks")
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun createTask(
        title: String,
        description: String?,
        priority: TaskPriority = TaskPriority.MEDIUM,
        category: TaskCategory = TaskCategory.OTHER
    ): Resource<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getAccessToken() ?: return@withContext Resource.Error<Task>("No autenticado")
                val userId = preferenceManager.getUserId() ?: return@withContext Resource.Error<Task>("Usuario no encontrado")

                val taskRequest = TaskRequest(
                    userId = userId,
                    title = title,
                    description = description,
                    priority = priority.value,
                    category = category.value
                )

                val response = api.createTask("Bearer $token", "return=representation", taskRequest)

                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val task = response.body()!![0]
                    taskDao.insertTask(task)
                    Resource.Success(task)
                } else {
                    // Crear tarea local si falla la sincronización
                    val localTask = Task(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        title = title,
                        description = description,
                        completed = false,
                        createdAt = System.currentTimeMillis().toString(),
                        priority = priority.value,
                        category = category.value,
                        isSynced = false
                    )
                    taskDao.insertTask(localTask)
                    Resource.Success(localTask)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating task")
                Resource.Error("Error al crear tarea: ${e.localizedMessage}")
            }
        }
    }

    suspend fun updateTask(
        taskId: String,
        title: String?,
        description: String?,
        completed: Boolean?,
        priority: TaskPriority? = null,
        category: TaskCategory? = null
    ): Resource<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getAccessToken() ?: return@withContext Resource.Error<Task>("No autenticado")

                val updateRequest = TaskUpdateRequest(
                    title = title,
                    description = description,
                    completed = completed,
                    priority = priority?.value,
                    category = category?.value
                )

                val response = api.updateTask("Bearer $token", "return=representation", "eq.$taskId", updateRequest)

                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    val task = response.body()!![0]
                    taskDao.insertTask(task)
                    Resource.Success(task)
                } else {
                    Resource.Error("Error al actualizar tarea")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating task")
                Resource.Error("Error al actualizar: ${e.localizedMessage}")
            }
        }
    }

    suspend fun deleteTask(taskId: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getAccessToken() ?: return@withContext Resource.Error<Unit>("No autenticado")

                val response = api.deleteTask("Bearer $token", "eq.$taskId")

                if (response.isSuccessful) {
                    taskDao.deleteTaskById(taskId)
                    Resource.Success(Unit)
                } else {
                    Resource.Error("Error al eliminar tarea")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting task")
                Resource.Error("Error al eliminar: ${e.localizedMessage}")
            }
        }
    }
}
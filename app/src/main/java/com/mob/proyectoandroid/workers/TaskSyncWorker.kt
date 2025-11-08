package com.mob.proyectoandroid.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting background sync")

            when (val result = taskRepository.syncTasks()) {
                is Resource.Success -> {
                    val taskCount = result.data?.size ?: 0

                    Timber.d("Background sync successful: $taskCount tasks")
                    Result.success()
                }
                is Resource.Error -> {
                    Timber.e("Background sync failed: ${result.message}")
                    Result.retry()
                }
                is Resource.Loading -> Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Background sync exception")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "task_sync_work"
    }
}
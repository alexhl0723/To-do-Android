package com.mob.proyectoandroid.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mob.proyectoandroid.R
import com.mob.proyectoandroid.data.local.TaskDao
import com.mob.proyectoandroid.data.model.Task
import com.mob.proyectoandroid.data.model.TaskPriority
import com.mob.proyectoandroid.utils.PreferenceManager // <-- ¡Importante!
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskWidgetService : RemoteViewsService() {

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var preferenceManager: PreferenceManager // <-- ¡Inyectamos el manager!

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        // ¡Le pasamos AMBOS al constructor!
        return TaskRemoteViewsFactory(applicationContext, taskDao, preferenceManager)
    }
}

// --- FÁBRICA (FACTORY) ---
// La ponemos en el mismo archivo para simplificar

class TaskRemoteViewsFactory(
    private val context: Context,
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager // <-- ¡Recibimos el manager!
) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = emptyList()

    override fun onCreate() {
        // No se necesita nada
    }

    override fun onDataSetChanged() {
        // ¡Esta es la lógica final!
        loadTasksFromDb()
    }

    /**
     * Esta función carga las tareas desde la BD
     * usando el PreferenceManager para obtener el ID de forma segura.
     */
    private fun loadTasksFromDb() {
        // ¡Ya no adivinamos las claves! Usamos el manager.
        val currentUserId = preferenceManager.getUserId()

        tasks = if (currentUserId != null) {
            // ¡Llama a la función síncrona del DAO!
            taskDao.getWidgetTasks(currentUserId)
        } else {
            // Si no hay usuario, lista vacía
            emptyList()
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    // --- ¡¡ESTE ES EL MÉTODO CORREGIDO A PRUEBA DE CRASHES!! ---
    @SuppressLint("RemoteViewLayout")
    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        try {
            // Revisa si la posición es válida
            if (position < tasks.size) {
                val task = tasks[position]

                // --- TEXTO DEL TÍTULO (Seguro) ---
                views.setTextViewText(R.id.tvWidgetTaskTitle, task.title ?: "Tarea sin título")

                // --- CATEGORÍA (Seguro) ---
                try {
                    // Intenta poner el emoji
                    views.setTextViewText(R.id.tvWidgetTaskCategory, task.categoryEnum().emoji)
                } catch (e: Exception) {
                    // Si falla (p.ej. es null), pon uno por defecto
                    views.setTextViewText(R.id.tvWidgetTaskCategory, "❓")
                }

                // --- PRIORIDAD (Seguro) ---
                var colorRes: Int
                try {
                    // Intenta obtener el color
                    colorRes = when (task.priorityEnum()) {
                        TaskPriority.HIGH -> android.R.color.holo_red_light
                        TaskPriority.MEDIUM -> android.R.color.holo_orange_light
                        TaskPriority.LOW -> android.R.color.holo_green_light
                    }
                } catch (e: Exception) {
                    // Si falla (p.ej. es null), pon uno por defecto
                    colorRes = android.R.color.darker_gray
                }
                views.setInt(R.id.viewPriorityIndicator, "setBackgroundResource", colorRes)


                // --- INTENT (Seguro) ---
                val fillInIntent = Intent().apply {
                    putExtra("task_id", task.id)
                }
                views.setOnClickFillInIntent(R.id.widgetTaskItem, fillInIntent)
            }
        } catch (e: Exception) {
            // Si algo catastrófico pasa, al menos no crashees el widget
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
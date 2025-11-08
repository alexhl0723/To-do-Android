package com.mob.proyectoandroid.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mob.proyectoandroid.R
import com.mob.proyectoandroid.ui.home.HomeActivity
import kotlin.jvm.java

class TaskWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Widget se agregó por primera vez
    }

    override fun onDisabled(context: Context) {
        // Último widget removido
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Intent para abrir la app
            val intent = Intent(context, HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent para agregar tarea rápida
            val addTaskIntent = Intent(context, QuickAddTaskActivity::class.java)
            val addTaskPendingIntent = PendingIntent.getActivity(
                context,
                1,
                addTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Construir el widget
            val views = RemoteViews(context.packageName, R.layout.widget_task)

            // Configurar clicks
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
            views.setOnClickPendingIntent(R.id.btnAddTaskWidget, addTaskPendingIntent)

            // Configurar el adaptador para la lista
            val serviceIntent = Intent(context, TaskWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            views.setRemoteAdapter(R.id.listViewTasks, serviceIntent)

            // Intent para items individuales
            val itemIntent = Intent(context, HomeActivity::class.java)
            val itemPendingIntent = PendingIntent.getActivity(
                context,
                0,
                itemIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.listViewTasks, itemPendingIntent)

            // Actualizar widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewTasks)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TaskWidget::class.java)
            )

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
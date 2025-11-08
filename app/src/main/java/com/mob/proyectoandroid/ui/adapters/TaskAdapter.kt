package com.mob.proyectoandroid.ui.adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mob.proyectoandroid.R
import com.mob.proyectoandroid.data.model.Task
import com.mob.proyectoandroid.data.model.TaskPriority
import com.mob.proyectoandroid.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(getItem(position))

        // Animación de entrada
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(
                holder.itemView.context,
                R.anim.item_animation_fall_down
            )
            holder.itemView.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun onViewDetachedFromWindow(holder: TaskViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                tvTaskTitle.text = task.title
                tvTaskDescription.text = task.description ?: ""

                // Mostrar categoría con emoji
                val categoryEmoji = task.categoryEnum().emoji
                tvTaskTitle.text = "$categoryEmoji ${task.title}"

                checkboxCompleted.isChecked = task.completed

                // Apply strikethrough if completed
                if (task.completed) {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTaskDescription.paintFlags = tvTaskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTaskTitle.alpha = 0.6f
                    tvTaskDescription.alpha = 0.6f
                } else {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTaskDescription.paintFlags = tvTaskDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTaskTitle.alpha = 1.0f
                    tvTaskDescription.alpha = 1.0f
                }

                // Mostrar indicador de prioridad
                val priorityColor = when (task.priorityEnum()) {
                    TaskPriority.LOW -> android.R.color.holo_green_light
                    TaskPriority.MEDIUM -> android.R.color.holo_orange_light
                    TaskPriority.HIGH -> android.R.color.holo_red_light
                }

                // Cambiar color del checkbox según prioridad
                checkboxCompleted.buttonTintList = ContextCompat.getColorStateList(
                    root.context,
                    priorityColor
                )

                // Show sync indicator
                ivSyncIndicator.visibility = if (task.isSynced) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }

                root.setOnClickListener { onTaskClick(task) }
                root.setOnLongClickListener {
                    onTaskLongClick(task)
                    true
                }

                checkboxCompleted.setOnClickListener {
                    onCheckboxClick(task)
                }
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
package com.mob.proyectoandroid.ui.widget

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // <-- ¡¡IMPORTADO!!
import com.mob.proyectoandroid.databinding.ActivityQuickAddTaskBinding
import com.mob.proyectoandroid.utils.showSnackbar
import com.mob.proyectoandroid.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // <-- ¡¡IMPORTADO!!

@AndroidEntryPoint
class QuickAddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuickAddTaskBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- ¡¡ESTE BLOQUE CAMBIÓ!! ---
        binding.btnAddQuick.setOnClickListener {
            val title = binding.etQuickTaskTitle.text.toString().trim()

            if (title.isNotEmpty()) {

                // 1. Llama a la función y guarda el "trabajo"
                val job = viewModel.createTask(title, null) // <-- Esto ya no dará error

                // 2. Lanza una corrutina
                lifecycleScope.launch {

                    // 3. ¡ESPERA a que el 'job' termine!
                    job.join() // <-- Esto ya no dará error

                    // 4. Ahora sí, actualiza el widget (DESPUÉS de guardar)
                    TaskWidget.updateAllWidgets(this@QuickAddTaskActivity)
                    binding.root.showSnackbar("Tarea agregada")
                    finish()
                }

            } else {
                binding.root.showSnackbar("El título es requerido")
            }
        }
        // --- FIN DEL BLOQUE QUE CAMBIÓ ---

        binding.btnCancelQuick.setOnClickListener {
            finish()
        }
    }

    // ¡¡DE AQUÍ BORRASTE LA FUNCIÓN 'Unit.join()'!!
}
package com.mob.proyectoandroid.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.databinding.ActivityLoginBinding
import com.mob.proyectoandroid.ui.home.HomeActivity
import com.mob.proyectoandroid.utils.hide
import com.mob.proyectoandroid.utils.show
import com.mob.proyectoandroid.utils.showSnackbar
import com.mob.proyectoandroid.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (viewModel.isUserLoggedIn()) {
            navigateToHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.apply {
            btnSubmit.setOnClickListener {
                if (isLoginMode) {
                    performLogin()
                } else {
                    performSignUp()
                }
            }

            tvToggleMode.setOnClickListener {
                toggleMode()
            }
        }
    }

    // LoginActivity.kt

    private fun setupObservers() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    binding.root.showSnackbar("Inicio de sesión exitoso")
                    navigateToHome() // <-- Esto está bien para el LOGIN
                }
                is Resource.Error -> {
                    showLoading(false)
                    binding.root.showSnackbar(resource.message ?: "Error al iniciar sesión")
                }
            }
        }

        // ==== BLOQUE MODIFICADO ====
        viewModel.signUpState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    // 1. Muestra el mensaje de confirmación
                    binding.root.showSnackbar("¡Registro exitoso! Revisa tu email para confirmar tu cuenta.")

                    // 2. NO navegues a Home. Quédate aquí.
                    // navigateToHome() // <-- LÍNEA ELIMINADA

                    // 3. Opcional: regresa la UI al modo "Iniciar Sesión"
                    if (!isLoginMode) {
                        toggleMode()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    // Esto ahora mostrará el error del ViewModel (si RLS falla, etc.)
                    // y ya no el crash del PreferenceManager
                    binding.root.showSnackbar(resource.message ?: "Error al registrarse")
                }
            }
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // ==== MODIFICADO ====
        // Solo valida email y password
        if (!validateInputs(email, password)) return

        viewModel.login(email, password)
    }

    private fun performSignUp() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        // ==== NUEVO ====
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // ==== MODIFICADO ====
        // Valida email, password y confirmPassword
        if (!validateInputs(email, password, confirmPassword)) return

        viewModel.signUp(email, password)
    }

    // ==== FUNCIÓN MODIFICADA ====
    // Se añade "confirmPassword" como parámetro opcional (nullable)
    private fun validateInputs(email: String, password: String, confirmPassword: String? = null): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "El email es requerido"
            return false
        }

        if (!viewModel.validateEmail(email)) {
            binding.tilEmail.error = "Email inválido"
            return false
        }

        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es requerida"
            return false
        }

        if (!viewModel.validatePassword(password)) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }

        binding.tilPassword.error = null

        // ==== LÓGICA NUEVA AÑADIDA ====
        // Si "confirmPassword" no es null (o sea, estamos en modo registro), validamos
        if (confirmPassword != null) {
            if (confirmPassword.isEmpty()) {
                binding.tilConfirmPassword.error = "Confirma la contraseña"
                return false
            }

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                binding.tilPassword.error = "Las contraseñas no coinciden" // Opcional
                return false
            }
            // Si coinciden, limpiamos cualquier error previo
            binding.tilConfirmPassword.error = null
        }

        return true
    }

    // ==== FUNCIÓN MODIFICADA ====
    private fun toggleMode() {
        isLoginMode = !isLoginMode
        binding.apply {
            if (isLoginMode) {
                tvTitle.text = "Iniciar Sesión"
                btnSubmit.text = "Iniciar Sesión"
                tvToggleMode.text = "¿No tienes cuenta? Regístrate"
                tilConfirmPassword.hide() // Oculta el campo
            } else {
                tvTitle.text = "Registrarse"
                btnSubmit.text = "Registrarse"
                tvToggleMode.text = "¿Ya tienes cuenta? Inicia sesión"
                tilConfirmPassword.show() // Muestra el campo
            }
            // Limpia errores al cambiar de modo
            tilEmail.error = null
            tilPassword.error = null
            tilConfirmPassword.error = null
        }
    }

    // ==== FUNCIÓN MODIFICADA ====
    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressBar.show()
                btnSubmit.isEnabled = false
                etEmail.isEnabled = false
                etPassword.isEnabled = false
                etConfirmPassword.isEnabled = false // Deshabilita el nuevo campo
            } else {
                progressBar.hide()
                btnSubmit.isEnabled = true
                etEmail.isEnabled = true
                etPassword.isEnabled = true
                etConfirmPassword.isEnabled = true // Habilita el nuevo campo
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
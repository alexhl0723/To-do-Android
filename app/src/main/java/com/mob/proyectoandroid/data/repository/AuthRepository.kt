package com.mob.proyectoandroid.data.repository

import com.mob.proyectoandroid.data.model.AuthRequest
import com.mob.proyectoandroid.data.model.AuthResponse
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.network.SupabaseApiService
import com.mob.proyectoandroid.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: SupabaseApiService,
    private val preferenceManager: PreferenceManager
) {

    // AuthRepository.kt

    suspend fun signUp(email: String, password: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.signUp(AuthRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // ¡LÍNEA PROBLEMÁTICA ELIMINADA!
                    // No guardamos la sesión aquí, solo en el signIn.

                    Resource.Success(authResponse)
                } else {
                    Resource.Error("Error en el registro: ${response.message()}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error signing up")
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun signIn(email: String, password: String): Resource<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.signIn(AuthRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    saveAuthData(authResponse)
                    Resource.Success(authResponse)
                } else {
                    Resource.Error("Credenciales inválidas")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error signing in")
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun signOut(): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getAccessToken()
                if (token != null) {
                    api.signOut("Bearer $token")
                }
                clearAuthData()
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error signing out")
                clearAuthData()
                Resource.Success(Unit)
            }
        }
    }

    fun isUserLoggedIn(): Boolean = preferenceManager.getAccessToken() != null

    fun getCurrentUserId(): String? = preferenceManager.getUserId()

    private fun saveAuthData(authResponse: AuthResponse) {
        preferenceManager.saveAccessToken(authResponse.accessToken)
        preferenceManager.saveUserId(authResponse.user.id)
        preferenceManager.saveUserEmail(authResponse.user.email)
    }

    private fun clearAuthData() {
        preferenceManager.clearAuthData()
    }
}
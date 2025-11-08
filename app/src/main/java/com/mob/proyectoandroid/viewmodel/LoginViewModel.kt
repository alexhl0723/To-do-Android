package com.mob.proyectoandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob.proyectoandroid.data.model.AuthResponse
import com.mob.proyectoandroid.data.model.Resource
import com.mob.proyectoandroid.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<AuthResponse>>()
    val loginState: LiveData<Resource<AuthResponse>> = _loginState

    private val _signUpState = MutableLiveData<Resource<AuthResponse>>()
    val signUpState: LiveData<Resource<AuthResponse>> = _signUpState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.signIn(email, password)
            _loginState.value = result
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading()
            val result = authRepository.signUp(email, password)
            _signUpState.value = result
        }
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }
}
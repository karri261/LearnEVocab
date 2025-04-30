package com.example.learningenglishvocab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningenglishvocab.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.getCurrentUser() != null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun checkUserStatus() {
        _isLoggedIn.value = authRepository.getCurrentUser() != null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = authRepository.loginUser(email, password)

            if (user != null) {
                if (user.isEmailVerified) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Email chưa được xác thực!"
                }
            } else {
                _errorMessage.value = "Đăng nhập thất bại! Vui lòng kiểm tra lại email hoặc mật khẩu."
            }

            _isLoading.value = false
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = authRepository.registerUser(email, password)

            if (user != null) {
                val emailSent = authRepository.sendEmailVerification(user)
                if (emailSent) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Không thể gửi email xác nhận!"
                }
            } else {
                _errorMessage.value = "Đăng ký thất bại!"
            }

            _isLoading.value = false
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                try {
                    it.sendEmailVerification().await()
                    _errorMessage.value = "Email xác nhận đã được gửi lại!"
                } catch (e: Exception) {
                    _errorMessage.value = "Gửi lại email thất bại: ${e.message}"
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _isLoggedIn.value = false
    }
}
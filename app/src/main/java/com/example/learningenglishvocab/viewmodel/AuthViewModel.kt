package com.example.learningenglishvocab.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningenglishvocab.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository(FirebaseAuth.getInstance())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // Chờ Firebase xác thực
            val currentUser = authRepository.getCurrentUser()
            _isLoggedIn.value = currentUser != null && currentUser.isEmailVerified
            Log.d("Auth", "Initialized isLoggedIn: ${_isLoggedIn.value}")
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUser()?.uid
    }

    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUser()?.email
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authRepository.loginUser(email, password)
                if (user != null) {
                    if (user.isEmailVerified) {
                        _isLoggedIn.value = true
                        onSuccess()
                    } else {
                        onError("Email chưa được xác thực!")
                    }
                } else {
                    onError("Đăng nhập thất bại! Vui lòng kiểm tra lại email hoặc mật khẩu.")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi đăng nhập không xác định.")
            } finally {
                _isLoading.value = false
            }
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
        viewModelScope.launch {
            try {
                Log.d("Auth", "Starting logout")
                authRepository.logout()
                _isLoggedIn.value = false
                Log.d("Auth", "Logout successful, isLoggedIn set to false")
            } catch (e: Exception) {
                Log.e("Auth", "Logout failed: ${e.message}")
                _errorMessage.value = "Lỗi đăng xuất: ${e.message}"
            }
        }
    }

    fun changeUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.getCurrentUser()?.uid
                if (userId != null) {
                    val success = authRepository.updateUsername(userId, newUsername)
                    if (success) {
                        onSuccess()
                    } else {
                        val isTaken = authRepository.userRepository.isUsernameTaken(newUsername, userId)
                        if (isTaken) {
                            onError("Tên người dùng đã tồn tại! Vui lòng chọn tên khác.")
                        } else {
                            onError("Không tìm thấy thông tin người dùng!")
                        }
                    }
                } else {
                    onError("Không tìm thấy người dùng hiện tại!")
                }
            } catch (e: Exception) {
                onError("Lỗi khi cập nhật tên người dùng: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Bước 1: Xác thực lại
                authRepository.reauthenticate(email, currentPassword).onSuccess {
                    // Bước 2: Thay đổi mật khẩu
                    authRepository.changePassword(newPassword).onSuccess {
                        _errorMessage.value = null
                        onSuccess()
                    }.onFailure { e ->
                        _errorMessage.value = e.message ?: "Thay đổi mật khẩu thất bại"
                        onError(_errorMessage.value!!)
                    }
                }.onFailure { e ->
                    _errorMessage.value = when {
                        e.message?.contains("password is invalid") == true -> "Mật khẩu hiện tại không đúng"
                        else -> e.message ?: "Xác thực thất bại"
                    }
                    onError(_errorMessage.value!!)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(
        email: String,
        currentPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Bước 1: Xác thực lại
                authRepository.reauthenticate(email, currentPassword).onSuccess {
                    // Bước 2: Xóa tài khoản
                    val userId = getCurrentUserId() ?: throw Exception("Không tìm thấy userId")
                    authRepository.deleteUserAccount(userId).onSuccess {
                        _errorMessage.value = null
                        _isLoggedIn.value = false
                        onSuccess()
                    }.onFailure { e ->
                        _errorMessage.value = e.message ?: "Xóa tài khoản thất bại"
                        onError(_errorMessage.value!!)
                    }
                }.onFailure { e ->
                    _errorMessage.value = when {
                        e.message?.contains("password is invalid") == true -> "Mật khẩu hiện tại không đúng"
                        else -> e.message ?: "Xác thực thất bại"
                    }
                    onError(_errorMessage.value!!)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
package com.example.aplikasistory.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistory.data.response.LoginResponse
import com.example.aplikasistory.data.response.LoginResult
import com.example.aplikasistory.data.response.RegisterResponse
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(name, email, password)
                .onStart { _registerResult.postValue(Result.Loading) }
                .catch { e ->
                    _registerResult.postValue(Result.Error(Exception(e.message ?: "Unknown error")))
                }
                .collect { result -> _registerResult.postValue(result) }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password)
                .onStart {
                    _loginResult.postValue(Result.Loading)
                }
                .catch { e ->
                    _loginResult.postValue(Result.Error(Exception(e.message ?: "Unknown error")))
                }
                .collect { result ->
                    if (result is Result.Success) {
                        // Simpan token jika login berhasil
                        result.data.loginResult?.token?.let { token ->
                            repository.saveToken(token)
                        }
                    }
                    _loginResult.postValue(result)
                }
        }
    }
}

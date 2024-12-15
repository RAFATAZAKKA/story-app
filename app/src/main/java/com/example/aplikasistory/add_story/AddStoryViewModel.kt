package com.example.aplikasistory.add_story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikasistory.data.api.ApiService
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val apiService: ApiService) : ViewModel() {

    fun uploadImageWithAuth(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {

                val response = apiService.uploadImageWithAuth(
                    token = "Bearer $token",
                    file = file,
                    description = description
                )
                if (!response.error!!) {
                    onSuccess(response.message ?: "Upload berhasil")
                } else {
                    onError(response.message ?: "Terjadi kesalahan")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error tidak diketahui")
            }
        }
    }
}
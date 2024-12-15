package com.example.aplikasistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasistory.add_story.AddStoryViewModel
import com.example.aplikasistory.data.AuthRepository
import com.example.aplikasistory.data.AuthViewModel
import com.example.aplikasistory.data.UserRepository
import com.example.aplikasistory.data.api.ApiConfig

class ViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
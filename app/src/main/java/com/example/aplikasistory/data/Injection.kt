package com.example.aplikasistory.data

import android.content.Context
import com.example.aplikasistory.DataStoreHelper
import com.example.aplikasistory.data.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Membuat instance DataStoreHelper
        val dataStoreHelper = DataStoreHelper(context)

        // Mengembalikan instance UserRepository dengan dataStoreHelper
        return UserRepository(apiService, dataStoreHelper)
    }
}
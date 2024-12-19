package com.example.aplikasistory.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.aplikasistory.DataStoreHelper
import com.example.aplikasistory.StoryPagingSource
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ErrorResponse
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.data.response.LoginResponse
import com.example.aplikasistory.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException


class UserRepository(
    private val apiService: ApiService,
    private val dataStoreHelper: DataStoreHelper
) {


    suspend fun saveToken(token: String) {
        dataStoreHelper.saveToken(token)
    }


    private suspend fun getToken(): String {
        return dataStoreHelper.getToken()
    }

    fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>> {
        return flow {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } else {
                    "Unknown error occurred"
                }
                emit(Result.Error(Exception(errorMessage)))
            } catch (e: IOException) {
                emit(Result.Error(Exception("Network error. Please check your connection.")))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun login(email: String, password: String): Flow<Result<LoginResponse>> {
        return flow {
            emit(Result.Loading)
            try {
                val response = apiService.login(email, password)
                response.loginResult?.token?.let { token ->
                    saveToken(token)
                }
                emit(Result.Success(response))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = errorBody?.let {
                    Gson().fromJson(it, ErrorResponse::class.java).message
                } ?: "Unknown error occurred"
                emit(Result.Error(Exception(errorMessage)))
            } catch (e: IOException) {
                emit(Result.Error(Exception("Network error. Please check your connection.")))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getPagedStories(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, dataStoreHelper)
            }
        ).flow
    }

    fun getStories(): Flow<Result<List<ListStoryItem>>> {
        return flow {
            emit(Result.Loading)
            try {
                val token = getToken()
                val response = apiService.getStories("Bearer $token")
                val nonNullList = response.listStory?.filterNotNull() ?: emptyList()
                emit(Result.Success(nonNullList))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun clearSession() {
        dataStoreHelper.clearToken()
    }
}

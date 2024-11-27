package com.example.aplikasistory.data

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()


    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(exception: Throwable): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading
    }
}
package com.dicoding.picodiploma.dicodingstoryapp.data.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.UserModel
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.UserPreference
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.request.LoginRequest
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.request.RegisterRequest
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.AuthResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiService
import retrofit2.HttpException
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    fun register(name: String, email: String, password: String): LiveData<Result<AuthResponse>> = liveData {
        emit(Result.Loading)
        try {
            val registerRequest = RegisterRequest(name, email, password)
            val response = apiService.register(registerRequest)  // Register via API

            if (response.error == false) {
                val loginResult = response.loginResult
                loginResult?.let {
                    // Save session
                    val user = UserModel(
                        email = email,
                        token = it.token ?: "",
                        isLogin = true
                    )
                    saveSession(user)
                }
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response.message ?: "Registration failed"))
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Exception: ${e.message}")
            val errorResponse = handleHttpException(e)
            emit(Result.Error(errorResponse))
        } catch (e: Exception) {
            Log.e("UserRepository", "General Exception: ${e.message}")
            emit(Result.Error("Error: ${e.localizedMessage}"))
        }
    }

    fun login(email: String, password: String): LiveData<Result<AuthResponse>> = liveData {
        emit(Result.Loading)
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)

            if (response.error == false) {
                val loginResult = response.loginResult
                loginResult?.let {
                    // Save session
                    val user = UserModel(
                        email = email,
                        token = it.token ?: "",
                        isLogin = true
                    )
                    saveSession(user)
                }
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response.message ?: "Login failed"))
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Exception: ${e.message}")
            val errorResponse = handleHttpException(e)
            emit(Result.Error(errorResponse))
        } catch (e: Exception) {
            Log.e("UserRepository", "General Exception: ${e.message}")
            emit(Result.Error("Error: ${e.localizedMessage}"))
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    private fun handleHttpException(e: HttpException): String {
        val errorResponse = e.response()?.errorBody()?.string()
        val gson = Gson()
        return try {
            gson.fromJson(errorResponse, AuthResponse::class.java)?.message ?: "Unknown error"
        } catch (exception: Exception) {
            "Error parsing HTTP exception response"
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}
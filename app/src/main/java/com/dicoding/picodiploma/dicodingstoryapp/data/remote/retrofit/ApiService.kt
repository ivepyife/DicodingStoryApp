package com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit

import com.dicoding.picodiploma.dicodingstoryapp.data.remote.request.LoginRequest
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.request.RegisterRequest
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.AddStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.AuthResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.DetailStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.ListStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): AuthResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): AuthResponse

    @GET("stories")
    suspend fun getAllStory(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = 0
    ): ListStoryResponse

    @GET("stories/{id}")
    suspend fun getStoryDetails(
        @Path("id") id: String
    ): DetailStoryResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): AddStoryResponse
}
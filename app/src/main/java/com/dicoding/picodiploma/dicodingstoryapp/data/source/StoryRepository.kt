package com.dicoding.picodiploma.dicodingstoryapp.data.source

import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.AddStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.DetailStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.ListStoryResponse
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService
) {
    suspend fun getStory(): Result<ListStoryResponse> {
        return try {
            val response = apiService.getAllStory(page = 1, size = 30)
            if (!response.error!!) {
                Result.Success(response)
            } else {
                Result.Error(response.message ?: "Unknown error occurred")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getStoryDetail(id: String): Result<DetailStoryResponse> {
        return try {
            val response = apiService.getStoryDetails(id)
            if (!response.error!!) {
                Result.Success(response)
            } else {
                Result.Error(response.message ?: "Unknown error occurred")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun addStory(
        description: String,
        imageFile: File,
        lat: Double? = null,
        lon: Double? = null
    ): Result<AddStoryResponse> {
        return try {
            val requestDescription = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            val requestLat = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val requestLon = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = apiService.addStory(requestDescription, multipartBody, requestLat, requestLon)
            if (!response.error!!) {
                Result.Success(response)
            } else {
                Result.Error(response.message ?: "Unknown error occurred")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, AddStoryResponse::class.java)
            Result.Error(errorResponse.message ?: "Upload failed")
        } catch (e: Exception) {
            Result.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getStoryWithLocation(): Result<ListStoryResponse> {
        return try {
            val response = apiService.getAllStory(location = 1)
            if (!response.error!!) {
                Result.Success(response)
            } else {
                Result.Error(response.message ?: "Unknown error occurred")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An error occurred")
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService)
            }.also { instance = it }
    }
}
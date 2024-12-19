package com.dicoding.picodiploma.dicodingstoryapp.data.source

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.dicodingstoryapp.data.local.entity.StoryEntity
import com.dicoding.picodiploma.dicodingstoryapp.data.local.room.StoryDatabase
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

@OptIn(androidx.paging.ExperimentalPagingApi::class)
class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
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
    fun getStoryPaged(): LiveData<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, storyDatabase)
            }.also { instance = it }
    }
}
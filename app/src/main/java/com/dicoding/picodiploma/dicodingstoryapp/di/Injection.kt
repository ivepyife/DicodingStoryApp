package com.dicoding.picodiploma.dicodingstoryapp.di

import android.content.Context
import com.dicoding.picodiploma.dicodingstoryapp.data.local.room.StoryDatabase
import com.dicoding.picodiploma.dicodingstoryapp.data.source.StoryRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.source.UserRepository
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.UserPreference
import com.dicoding.picodiploma.dicodingstoryapp.data.pref.dataStore
import com.dicoding.picodiploma.dicodingstoryapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return UserRepository.getInstance(pref, apiService)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        val storyDatabase = StoryDatabase.getInstance(context)
        return StoryRepository.getInstance(apiService, storyDatabase)
    }

}
package com.dicoding.picodiploma.dicodingstoryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val photoUrl: String? = null,
    val createdAt: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)
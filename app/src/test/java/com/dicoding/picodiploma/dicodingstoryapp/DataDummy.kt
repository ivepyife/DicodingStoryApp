package com.dicoding.picodiploma.dicodingstoryapp

import com.dicoding.picodiploma.dicodingstoryapp.data.remote.response.StoryItem
import java.util.UUID
import kotlin.random.Random

object DataDummy {
    fun generateDummyQuoteResponse(): List<StoryItem> {
        val items: MutableList<StoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoryItem(
                id = UUID.randomUUID().toString(),
                createdAt = "2026-12-30T15:26:33Z",
                description = "Description $i",
                name = "sample name $i",
                lon = Random.nextDouble(100.0, 140.0),
                lat = Random.nextDouble(-10.0, 10.0),
                photoUrl = "https://picsum.photos/id/$i/200/300"
            )
            items.add(story)
        }
        return items
    }
}
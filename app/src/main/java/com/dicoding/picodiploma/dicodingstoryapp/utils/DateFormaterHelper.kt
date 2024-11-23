package com.dicoding.picodiploma.dicodingstoryapp.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateFormaterHelper {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun formatDate(dateString: String): String {
        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
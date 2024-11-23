package com.dicoding.picodiploma.dicodingstoryapp.data.pref

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)
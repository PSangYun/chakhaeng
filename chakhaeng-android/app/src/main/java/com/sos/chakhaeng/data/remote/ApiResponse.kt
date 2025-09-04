package com.sos.chakhaeng.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)


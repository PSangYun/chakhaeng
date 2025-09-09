package com.sos.chakhaeng.data.network.dto

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)
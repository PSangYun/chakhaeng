package com.sos.chakhaeng.data.network.dto

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)

// 디버깅을 위한 장치
inline fun <T> ApiResponse<T>.getOrThrow(): T {
    if (success && data != null) return data
    throw IllegalArgumentException("${code ?: ""} ${message ?: ""}".trim())
}
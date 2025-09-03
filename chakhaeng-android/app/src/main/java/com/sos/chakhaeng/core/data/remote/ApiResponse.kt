package com.sos.chakhaeng.core.data.remote

import com.sos.chakhaeng.core.data.model.response.UserDto

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)


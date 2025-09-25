package com.sos.chakhaeng.data.network.dto.response.profile

import com.google.gson.annotations.SerializedName

data class BadgeDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("isUnlocked")
    val isUnlocked: Boolean
)
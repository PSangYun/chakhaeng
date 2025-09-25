package com.sos.chakhaeng.data.network.dto.response.profile

import com.google.gson.annotations.SerializedName

data class UserProfileDTO(
    @SerializedName("name")
    val name: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String
)

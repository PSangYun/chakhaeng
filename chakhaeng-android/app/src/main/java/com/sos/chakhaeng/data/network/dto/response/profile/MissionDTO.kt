package com.sos.chakhaeng.data.network.dto.response.profile

import com.google.gson.annotations.SerializedName

data class MissionDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("iconRes")
    val iconRes: String,

    @SerializedName("isCompleted")
    val isCompleted: Boolean,

    @SerializedName("currentProgress")
    val currentProgress: Int,

    @SerializedName("targetProgress")
    val targetProgress: Int,

    @SerializedName("rewardName")
    val rewardName: String
)
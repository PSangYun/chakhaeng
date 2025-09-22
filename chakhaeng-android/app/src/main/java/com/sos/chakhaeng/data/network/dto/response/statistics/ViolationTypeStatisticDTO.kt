package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class ViolationTypeStatisticDTO(
    @SerializedName("violationType")
    val violationType: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("percentage")
    val percentage: Int
)

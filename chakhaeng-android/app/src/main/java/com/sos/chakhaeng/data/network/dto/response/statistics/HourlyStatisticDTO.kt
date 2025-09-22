package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class HourlyStatisticDTO(
    @SerializedName("hour")
    val hour: Int,

    @SerializedName("count")
    val count: Int
)

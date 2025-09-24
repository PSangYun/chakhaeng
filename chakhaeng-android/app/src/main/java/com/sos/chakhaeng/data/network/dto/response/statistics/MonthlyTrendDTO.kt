package com.sos.chakhaeng.data.network.dto.response.statistics

import com.google.gson.annotations.SerializedName

data class MonthlyTrendDTO(
    @SerializedName("month")
    val month: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("changeFromPreviousMonth")
    val changeFromPreviousMonth: Int
)

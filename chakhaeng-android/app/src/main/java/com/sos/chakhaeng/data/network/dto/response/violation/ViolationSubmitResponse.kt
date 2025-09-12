package com.sos.chakhaeng.data.network.dto.response.violation

import com.google.gson.annotations.SerializedName

data class ViolationSubmitResponse (
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String
)
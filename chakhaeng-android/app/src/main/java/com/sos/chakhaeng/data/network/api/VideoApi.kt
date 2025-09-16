package com.sos.chakhaeng.data.network.api

import com.sos.chakhaeng.data.network.dto.ApiResponse
import com.sos.chakhaeng.data.network.dto.request.violation.CompleteUploadRequest
import com.sos.chakhaeng.data.network.dto.request.violation.CreateUploadUrlRequest
import com.sos.chakhaeng.data.network.dto.response.violation.UploadUrl
import com.sos.chakhaeng.data.network.dto.response.violation.CompleteUploadResponse
import com.sos.chakhaeng.data.network.dto.response.violation.CreateUploadUrlResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {

    @POST("videos/upload-url")
    suspend fun createUploadUrl(
        @Body body: CreateUploadUrlRequest
    ): ApiResponse<CreateUploadUrlResponse>

    @POST("videos/videos/complete")
    suspend fun completeUpload(
        @Body body: CompleteUploadRequest
    ): ApiResponse<CompleteUploadResponse>

    @GET("videos/{videoKey}/play-url")
    suspend fun getPlayUrl(
        @Path("videoKey") videoKey: String
    ) : UploadUrl

    @GET("videos/download-url")
    suspend fun downloadUrl(
    @Query("objectKey") objectKey : String
    ) : UploadUrl
}
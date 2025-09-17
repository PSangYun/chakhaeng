package com.sos.chakhaeng.data.repository

import android.net.Uri
import com.sos.chakhaeng.data.manager.VideoUploadManager
import com.sos.chakhaeng.data.network.api.VideoApi
import com.sos.chakhaeng.data.network.dto.getOrThrow
import com.sos.chakhaeng.data.network.dto.request.violation.CompleteUploadRequest
import com.sos.chakhaeng.data.network.dto.request.violation.CreateUploadUrlRequest
import com.sos.chakhaeng.data.network.dto.response.violation.UploadResult
import com.sos.chakhaeng.data.network.dto.response.violation.UploadUrl
import com.sos.chakhaeng.domain.model.StreamingUrl
import com.sos.chakhaeng.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named

class VideoRepositoryImpl @Inject constructor(
    private val videoApi: VideoApi,
    private val videoUploadManager: VideoUploadManager,
    @Named("s3") private val s3Client: OkHttpClient
) : VideoRepository {

    override suspend fun uploadVideo(
        uri: Uri,
        onProgress: (sent: Long, total: Long?) -> Unit
    ): Result<UploadResult> = runCatching {
        withContext(Dispatchers.IO) {
            val info = videoUploadManager.resolveFileInfo(uri)

            val presign = videoApi.createUploadUrl(
                CreateUploadUrlRequest(
                    filename = info.fileName,
                    contentType = info.contentType
                )
            ).getOrThrow()

            val body = videoUploadManager.streamingBody(
                uri = uri,
                mime = info.contentType,
                length = info.sizeBytes,
                onProgress = onProgress
            )
            s3Client.newCall(
                Request.Builder()
                    .url(presign.uploadUrl)
                    .put(body)
                    .header("Content-Type", info.contentType)
                    .build()
            ).execute().use { resp ->
                check(resp.isSuccessful) { "S3 업로드 실패: HTTP ${resp.code}" }
            }

            val complete = videoApi.completeUpload(
                CompleteUploadRequest(
                    objectKey = presign.objectKey,
                    originalName = info.fileName,
                    contentType = info.contentType,
                    sizeBytes = info.sizeBytes ?: 0L,
                    durationSec = 0
                )
            ).getOrThrow()

            val playUrl: UploadUrl = videoApi.downloadUrl(complete.objectKey)

            UploadResult(playUrl, complete)
        }
    }

    override suspend fun getStreamingVideoUrl(objectKey: String) = StreamingUrl(videoApi.downloadUrl(objectKey).downloadUrl)
}
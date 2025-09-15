package com.sos.chakhaeng.data.manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.ui.autofill.ContentType
import com.sos.chakhaeng.data.network.api.VideoApi
import com.sos.chakhaeng.data.network.dto.getOrThrow
import com.sos.chakhaeng.data.network.dto.request.violation.CompleteUploadRequest
import com.sos.chakhaeng.data.network.dto.request.violation.CreateUploadUrlRequest
import com.sos.chakhaeng.data.network.dto.request.violation.UploadResult
import com.sos.chakhaeng.data.network.dto.response.violation.CompleteUploadResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import javax.inject.Inject
import javax.inject.Named

class VideoUploadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: VideoApi,

    @Named("s3") private val okHttp: OkHttpClient
){

    /**
     * Uri 하나면 끝: 1) presigned 발급 → 2) S3 PUT → 3) 완료 콜백
     * @return 서버의 완료 응답 data (id/objectKey/originalName/status/createdAt)
     */
    suspend fun uploadVideo(
        uri: Uri,
        onProgress: (sent: Long, total: Long?) -> Unit = { _, _ -> }
        ): Result<UploadResult> = runCatching {
            withContext(Dispatchers.IO) {
                val cr = context.contentResolver

                val filename = queryDisplayName(cr, uri) ?: buildAutoFileName(cr, uri)
                val contentType = detectContentType(cr, uri) ?: "video/mp4"
                val size = querySize(cr, uri)

                val presign = api.createUploadUrl(
                    CreateUploadUrlRequest(filename, contentType)
                ).getOrThrow()

                val body = streamingBody(cr, uri, contentType, size, onProgress)
                okHttp.newCall(
                    Request.Builder()
                        .url(presign.uploadUrl)
                        .put(body)
                        .header("Content-Type", contentType)
                        .build()
                ).execute().use { resp ->
                    check(resp.isSuccessful) { "S3에 업로드를 실패했습니다 : ${resp.code}"}
                }

                val complete = api.completeUpload(
                    CompleteUploadRequest(
                        objectKey = presign.objectKey,
                        originalName = filename,
                        contentType = contentType,
                        sizeBytes = 0,
                        durationSec = 0
                    )
                ).getOrThrow()

                val playUrl = api.downloadUrl(complete.objectKey)

                Log.d("TAG", "uploadVideo: ${playUrl.downloadUrl}")

                UploadResult(playUrl.downloadUrl)
            }
    }

    private fun streamingBody(
        cr: ContentResolver,
        uri: Uri,
        mime: String,
        length: Long?,
        onProgress: (Long, Long?) -> Unit
    ): RequestBody = object : RequestBody() {
        override fun contentType(): MediaType? {
            return mime.toMediaTypeOrNull()
        }

        override fun contentLength(): Long {
            return length ?: -1
        }

        override fun writeTo(sink: BufferedSink) {
            cr.openInputStream(uri)?.use { input ->
                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded  = 0L
                while (true) {
                    val read = input.read(buf)
                    if (read == -1) break
                    sink.write(buf, 0, read)
                    uploaded += read
                    onProgress(uploaded, length)
                }
            } ?: error("InputStream을 열 수 없습니다. : $uri")
        }
    }

    private fun queryDisplayName(cr: ContentResolver, uri: Uri): String? =
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c->
            val idx = c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst()) c.getString(idx) else null
        }

    private fun querySize(cr: ContentResolver, uri: Uri): Long? =
        cr.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            val idx = c.getColumnIndexOrThrow(OpenableColumns.SIZE)
            if (c.moveToFirst()) c.getLong(idx) else null
        }

    private fun detectContentType(cr: ContentResolver, uri: Uri): String? {
        cr.getType(uri)?.let { return it }
        val name = queryDisplayName(cr, uri) ?: return null
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { return it }
        }
        return null
    }

    private fun buildAutoFileName(cr: ContentResolver, uri: Uri): String {
        val guessed = detectContentType(cr, uri)
        val ext = when (guessed) {
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            "video/3gpp" -> "3gp"
            "video/x-matroska" -> "mkv"
            else -> "mp4"
        }
        return "video_${System.currentTimeMillis()}.$ext"
    }



}
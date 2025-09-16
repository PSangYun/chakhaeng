package com.sos.chakhaeng.data.manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import javax.inject.Inject

class VideoUploadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class FileInfo(
        val fileName: String,
        val contentType: String,
        val sizeBytes: Long?
    )

    /** 파일명/타입/크기 등 메타를 로컬에서 추론 (네트워크 X) */
    fun resolveFileInfo(uri: Uri): FileInfo {
        val cr = context.contentResolver
        val name = queryDisplayName(cr, uri) ?: buildAutoFileName(cr, uri)
        val contentType = detectContentType(cr, uri) ?: "video/mp4"
        val size = querySize(cr, uri)
        return FileInfo(name, contentType, size)
    }

    /** PUT 스트리밍을 위한 RequestBody (네트워크 X) */
    fun streamingBody(
        uri: Uri,
        mime: String,
        length: Long?,
        onProgress: (sent: Long, total: Long?) -> Unit = { _, _ -> }
    ): RequestBody = object : RequestBody() {
        override fun contentType(): MediaType? = mime.toMediaTypeOrNull()
        override fun contentLength(): Long = length ?: -1
        override fun writeTo(sink: BufferedSink) {
            val cr = context.contentResolver
            cr.openInputStream(uri)?.use { input ->
                val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded = 0L
                while (true) {
                    val read = input.read(buf)
                    if (read == -1) break
                    sink.write(buf, 0, read)
                    uploaded += read
                    onProgress(uploaded, length)
                }
            } ?: error("InputStream 열기 실패: $uri")
        }
    }

    // ---- 내부 유틸 ----
    private fun queryDisplayName(cr: ContentResolver, uri: Uri): String? =
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
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

package com.sos.chakhaeng.core.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sos.chakhaeng.domain.usecase.violation.DetectViolationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DetectViolationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val detectViolationUseCase: DetectViolationUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val videoUriStr = params.inputData.getString("videoUri") ?: return@withContext Result.failure()
            val lat = params.inputData.getDouble("lat", 0.0)
            val lng = params.inputData.getDouble("lng", 0.0)
            val type = params.inputData.getString("type") ?: return@withContext Result.failure()
            val plate = params.inputData.getString("plate") ?: return@withContext Result.failure()
            val occurredAt = params.inputData.getString("occurredAt") ?: return@withContext Result.failure()

            val uri = Uri.parse(videoUriStr)

            val result = detectViolationUseCase(
                uri = uri,
                lat = lat,
                lng = lng,
                type = type,
                plate = plate,
                occurredAt = occurredAt
            )
            result.fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

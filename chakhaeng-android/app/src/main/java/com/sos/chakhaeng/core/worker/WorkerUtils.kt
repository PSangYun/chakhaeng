package com.sos.chakhaeng.core.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.LocationServices
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun enqueueViolationWork(
    context: Context,
    videoUri: Uri,
    lat: Double,
    lng: Double,
    type: String,
    plate: String
) {
    val occurredAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        .withZone(ZoneOffset.UTC)
        .format(Instant.now())

    val input = workDataOf(
        "videoUri" to videoUri.toString(),
        "lat" to lat,
        "lng" to lng,
        "type" to type,
        "plate" to plate,
        "occurredAt" to occurredAt
    )

    val request = OneTimeWorkRequestBuilder<DetectViolationWorker>()
        .setInputData(input)
        .setBackoffCriteria(
            androidx.work.BackoffPolicy.EXPONENTIAL,
            30, TimeUnit.SECONDS
        )
        .build()
    WorkManager.getInstance(context).enqueue(request)
}

fun getCurrentLocationAndEnqueue(
    context: Context,
    videoUri: Uri,
    type: String,
    plate: String
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                enqueueViolationWork(
                    context = context,
                    videoUri = videoUri,
                    lat = location.latitude,
                    lng = location.longitude,
                    type = type,
                    plate = plate
                )
            }
        }
}
package com.sos.chakhaeng.core.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.LocationServices
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

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
    Log.d("test1234","4")
    val request = OneTimeWorkRequestBuilder<DetectViolationWorker>()
        .setInputData(input)
        .setBackoffCriteria(
            androidx.work.BackoffPolicy.EXPONENTIAL,
            30, TimeUnit.SECONDS
        )
        .build()
    val wm = WorkManager.getInstance(context)
    wm.enqueueUniqueWork(
        "violation-upload-unique",
        ExistingWorkPolicy.REPLACE,
        request
    )

// 바로 1) ID로
    wm.getWorkInfoById(request.id).addListener({
        wm.getWorkInfoById(request.id).get()?.let { info ->
            Log.d("ViolationWork", "byId: state=${info.state} tags=${info.tags} runAttempt=${info.runAttemptCount}")
        }
    }, Executors.newSingleThreadExecutor())

// 바로 2) TAG로
    wm.getWorkInfosByTag("violation-upload").addListener({
        val list = wm.getWorkInfosByTag("violation-upload").get()
        Log.d("ViolationWork", "byTag size=${list.size} states=${list.map { it.state }}")
    }, Executors.newSingleThreadExecutor())
}

fun getCurrentLocationAndEnqueue(
    context: Context,
    videoUri: Uri,
    type: String,
    plate: String
) {
    val fused = LocationServices.getFusedLocationProviderClient(context)

    val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) {
        Log.w("ViolationWork", "No location permission -> enqueue without location")
        enqueueViolationWork(context, videoUri, 0.0, 0.0, type, plate)
        return
    }
    val token = com.google.android.gms.tasks.CancellationTokenSource()
    fused.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        token.token
    ).addOnSuccessListener { loc ->
        if (loc != null) {
            enqueueViolationWork(context, videoUri, loc.latitude, loc.longitude, type, plate)
        } else {
            fused.lastLocation.addOnSuccessListener { last ->
                if (last != null) {
                    enqueueViolationWork(context, videoUri, last.latitude, last.longitude, type, plate)
                } else {
                    Log.w("ViolationWork", "Location null -> enqueue without location")
                    enqueueViolationWork(context, videoUri, 0.0, 0.0, type, plate)
                }
            }.addOnFailureListener {
                Log.e("ViolationWork", "lastLocation fail: ${it.message}")
                enqueueViolationWork(context, videoUri, 0.0, 0.0, type, plate)
            }
        }
    }.addOnFailureListener {
        Log.e("ViolationWork", "getCurrentLocation fail: ${it.message}")
        enqueueViolationWork(context, videoUri, 0.0, 0.0, type, plate)
    }
    Handler(Looper.getMainLooper()).postDelayed({
        if (!token.token.isCancellationRequested) {
            token.cancel()
            Log.w("ViolationWork", "Location timeout -> enqueue without location")
            enqueueViolationWork(context, videoUri, 0.0, 0.0, type, plate)
        }
    }, 2000)
}

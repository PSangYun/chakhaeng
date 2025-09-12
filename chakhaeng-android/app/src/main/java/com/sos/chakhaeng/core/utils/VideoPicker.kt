package com.sos.chakhaeng.core.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 호출하면 시스템 포토 피커(가능 시) 또는 파일 선택기가 열려 동영상만 선택.
 * 권한 없이 동작. 선택된 Uri는 onPicked 로 콜백.
 */
@Composable
fun rememberVideoPicker(onPicked: (Uri) -> Unit): () -> Unit {
    val context = LocalContext.current

    // 1) Android Photo Picker
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onPicked)
    }

    // 2) 아주 구형 기기 대비: 시스템 파일 선택기 (video/*)
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(onPicked)
    }

    return remember(pickVisualMediaLauncher, getContentLauncher, context) {
        {
            // Photo Picker 가능하면 그걸 쓰고, 아니면 GetContent로 폴백
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                pickVisualMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            } else {
                getContentLauncher.launch("video/*")
            }
        }
    }
}
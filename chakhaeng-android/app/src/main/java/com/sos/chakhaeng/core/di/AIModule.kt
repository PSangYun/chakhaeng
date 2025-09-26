package com.sos.chakhaeng.core.di

import android.content.Context
import com.sos.chakhaeng.core.ai.Backend
import com.sos.chakhaeng.core.ai.ColorOrder
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.ai.InputRange
import com.sos.chakhaeng.core.ai.ModelSpec
import com.sos.chakhaeng.core.ai.MultiModelInterpreterDetector
import com.sos.chakhaeng.core.camera.YuvToRgbConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideDetector(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): Detector {
        val specs = listOf(
            ModelSpec(
                key = "final_best",
                assetPath = "models/final_best_float16.tflite", // ✅ 임시 YOLO 모델 파일명
                numClasses = 24,                       // COCO 80 클래스
                maxDetections = 8400,                      // YOLOv8 640 입력 기준
                preferInputSize = 640,                     // 보통 640
                inputRange = InputRange.FLOAT32_0_1,       // fp16/float32 모델이면 0~1 정규화
                colorOrder = ColorOrder.RGB,               // 일반적으로 RGB
                labelMap = null                            // /assets/labels/yolov8s.txt 있으면 자동 로드
            )
        )

        val backend = Backend.GPU // 필요 시 Backend.CPU / NNAPI

        return MultiModelInterpreterDetector(
            context = context,
            backend = backend,
            specs = specs,
            scope = scope // 🔑 ApplicationScope 전달
        )
    }

    @Provides
    @Singleton
    fun provideYuv(@ApplicationContext context: Context): YuvToRgbConverter =
        YuvToRgbConverter(context)
}
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
                key = "final",
                assetPath = "models/final_float16.tflite", // ✅ YOLO 모델 파일명
                numClasses = 26,
                maxDetections = 8400, // YOLOv8 640 기준
                preferInputSize = 640,
                inputRange = InputRange.FLOAT32_0_1,
                colorOrder = ColorOrder.RGB,
                labelMap = null
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

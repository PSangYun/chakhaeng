package com.sos.chakhaeng.core.di

import android.content.Context
import com.sos.chakhaeng.core.ai.Detector
import com.sos.chakhaeng.core.ai.YoloV8TFLiteDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideDetector(@ApplicationContext context: Context): Detector {
        return YoloV8TFLiteDetector(context)
    }
}
package com.sos.chakhaeng.core.di

import com.google.android.datatransport.runtime.dagger.multibindings.IntoSet
import com.sos.chakhaeng.domain.usecase.ai.ViolationAggregator
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkInvadeRule
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetRule
import com.sos.chakhaeng.domain.usecase.ai.rules.RedSignalConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.ViolationRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViolationRulesModule {

    // 공용 도구
    @Provides
    @Singleton
    fun provideViolationThrottle(): ViolationThrottle = ViolationThrottle()
    @Provides @Singleton fun provideViolationAggregator() = ViolationAggregator()

    // Config들 (원하면 RemoteConfig/Datastore로 치환)
    @Provides @Singleton fun provideNoHelmetConfig() = NoHelmetConfig()
    @Provides @Singleton fun provideCrosswalkConfig() = CrosswalkConfig()
    @Provides @Singleton fun provideRedSignalConfig() = RedSignalConfig()

    @Provides @IntoSet
    fun provideNoHelmetRule(
        cfg: NoHelmetConfig,
        throttle: ViolationThrottle
    ): ViolationRule = NoHelmetRule(cfg, throttle)

    @Provides @IntoSet
    fun provideCrosswalkRule(
        cfg: CrosswalkConfig
    ): ViolationRule = CrosswalkInvadeRule(cfg)
}
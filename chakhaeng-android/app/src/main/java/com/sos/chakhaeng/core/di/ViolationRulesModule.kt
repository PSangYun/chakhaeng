package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.domain.usecase.ai.ViolationAggregator
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottleConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkInvadeRule
import com.sos.chakhaeng.domain.usecase.ai.rules.IllegalMotorcycleConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.IllegalMotorcycleRule
import com.sos.chakhaeng.domain.usecase.ai.rules.LovebugConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.LovebugRule
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetRule
import com.sos.chakhaeng.domain.usecase.ai.rules.RedSignalConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.RedSignalCrosswalkRule
import com.sos.chakhaeng.domain.usecase.ai.rules.ViolationRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ViolationRulesModule {

    // 공용 도구
    @Provides @Singleton fun provideViolationAggregator() = ViolationAggregator()

    // Config들 (원하면 RemoteConfig/Datastore로 치환)
    @Provides @Singleton fun provideNoHelmetConfig() = NoHelmetConfig()
    @Provides @Singleton fun provideCrosswalkConfig() = CrosswalkConfig()
    @Provides @Singleton fun provideRedSignalConfig() = RedSignalConfig()

    @Provides @Singleton fun provideLovebugConfig() = LovebugConfig()
    @Provides @Singleton fun provideIllegalMotorcycleConfig() = IllegalMotorcycleConfig()

    @Provides
    @Singleton
    fun provideViolationThrottleConfig(): ViolationThrottleConfig = ViolationThrottleConfig(
        defaultCooldownMs = 1_000L,
        dedupIou = 0.30f,
        perTypeCooldownMs = mapOf(
            "헬멧 미착용" to 600_000L,
            "신호위반" to 600_000L,
            "킥보드 2인이상" to 600_000L,
            "무번호판" to 600_000L
        ),
        globalCooldownTypes = setOf("헬멧 미착용", "신호위반", "킥보드 2인이상", "무번호판")
    )

    @Provides @IntoSet
    fun provideNoHelmetRule(
        cfg: NoHelmetConfig,
        throttle: ViolationThrottle
    ): ViolationRule = NoHelmetRule(cfg, throttle)

    @Provides @IntoSet
    fun provideCrosswalkRule(
        cfg: CrosswalkConfig
    ): ViolationRule = CrosswalkInvadeRule(cfg)

    @Provides @IntoSet
    fun provideRedSignalRule(
        cfg: RedSignalConfig,
        throttle: ViolationThrottle
    ): ViolationRule = RedSignalCrosswalkRule(cfg, throttle)

    @Provides @IntoSet
    fun provideLovebugRule(
        cfg: LovebugConfig,
        throttle: ViolationThrottle
    ): ViolationRule = LovebugRule(cfg, throttle)

    @Provides @IntoSet
    fun provideIllegalMotorcycleRule(
        cfg: IllegalMotorcycleConfig,
        throttle: ViolationThrottle
    ): ViolationRule = IllegalMotorcycleRule(cfg, throttle)
}
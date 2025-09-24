package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.domain.usecase.ai.ViolationAggregator
import com.sos.chakhaeng.domain.usecase.ai.ViolationThrottle
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.CrosswalkInvadeRule
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetConfig
import com.sos.chakhaeng.domain.usecase.ai.rules.NoHelmetRule
import com.sos.chakhaeng.domain.usecase.ai.rules.ViolationRule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ViolationRulesBindsModule {

    @Binds
    @IntoSet
    abstract fun bindNoHelmetRule(impl: NoHelmetRule): ViolationRule

    @Binds @IntoSet
    abstract fun bindCrosswalkRule(impl: CrosswalkInvadeRule): ViolationRule
}
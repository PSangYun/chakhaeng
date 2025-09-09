package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.repository.AuthRepositoryImpl
import com.sos.chakhaeng.data.repository.HomeRepositoryImpl
import com.sos.chakhaeng.domain.repository.AuthRepository
import com.sos.chakhaeng.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ) : HomeRepository
}
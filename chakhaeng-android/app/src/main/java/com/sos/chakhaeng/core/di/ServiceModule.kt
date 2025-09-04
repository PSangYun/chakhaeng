package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.api.AuthService
import com.sos.chakhaeng.data.api.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides @Singleton @Named("noauth")
    fun provideAuthService(@Named("noauth") retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideAuthServiceDefault(
        @Named("noauth") retrofit: Retrofit
    ): AuthService = retrofit.create(
        AuthService::class.java
    )

    @Provides @Singleton
    fun provideUserService(@Named("auth") retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)
}
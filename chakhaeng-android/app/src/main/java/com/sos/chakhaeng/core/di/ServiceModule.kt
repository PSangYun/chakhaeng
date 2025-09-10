package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.network.api.AuthApi
import com.sos.chakhaeng.data.network.api.HomeApi
import com.sos.chakhaeng.data.network.api.UserApi
import com.sos.chakhaeng.data.network.api.ViolationApi
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
    fun provideAuthService(@Named("noauth") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthServiceDefault(
        @Named("noauth") retrofit: Retrofit
    ): AuthApi = retrofit.create(
        AuthApi::class.java
    )

    @Provides
    @Singleton
    fun provideUserService(@Named("auth") retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideHomeApi(@Named("auth") retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideViolationService(
        @Named("auth") retrofit: Retrofit
    ): ViolationApi = retrofit.create(ViolationApi::class.java)
}
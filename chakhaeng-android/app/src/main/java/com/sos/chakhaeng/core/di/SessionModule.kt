package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.network.api.AuthApi
import com.sos.chakhaeng.data.datastore.TokenStore
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.core.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        tokenStore: TokenStore,
        googleAuthManager: GoogleAuthManager,
        @Named("noauth") authApi: AuthApi
    ): SessionManager = SessionManager(tokenStore, googleAuthManager, authApi)
}
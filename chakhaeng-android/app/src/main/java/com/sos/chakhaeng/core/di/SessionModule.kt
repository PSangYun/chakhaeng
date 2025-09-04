package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.api.AuthService
import com.sos.chakhaeng.datastore.TokenStore
import com.sos.chakhaeng.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.session.SessionManager
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
        @Named("noauth") authService: AuthService
    ): SessionManager = SessionManager(tokenStore, googleAuthManager, authService)
}
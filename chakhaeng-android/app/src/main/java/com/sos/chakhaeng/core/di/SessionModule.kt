package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.core.datastore.TokenStore
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.core.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        tokenStore: TokenStore,
        googleAuthManager: GoogleAuthManager
    ): SessionManager = SessionManager(tokenStore, googleAuthManager)
}
package com.sos.chakhaeng.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.sos.chakhaeng.core.datastore.PrefsTokenStore
import com.sos.chakhaeng.core.datastore.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {context.preferencesDataStoreFile("auth_session.preferences_pb")}
        )

    @Provides
    @Singleton
    fun provideTokenStore(impl: PrefsTokenStore): TokenStore = impl
}
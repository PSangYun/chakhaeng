package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.BuildConfig
import com.sos.chakhaeng.data.network.api.HomeApi
import com.sos.chakhaeng.data.network.interceptor.AuthInterceptor
import com.sos.chakhaeng.data.network.interceptor.TokenAuthenticator
import com.sos.chakhaeng.core.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun proivdeBaseUrl(): HttpUrl = BuildConfig.BASE_URL.toHttpUrl()

    @Provides
    @Singleton
    @Named("TEST")
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client =
            OkHttpClient.Builder()
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(logging)
        return client.build()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
//            redactHeader("Authorization")
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor =
        AuthInterceptor(sessionManager)

    // -------- No-Auth OkHttp (로그인/리프레시 전용) --------
    @Provides @Singleton @Named("noauth")
    fun provideNoAuthOkHttp(
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(logging)           // 순서 상관 X
        .build()


    @Provides @Singleton @Named("auth")
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        auth: AuthInterceptor,
        sessionManager: SessionManager
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logging)
        .authenticator(TokenAuthenticator(sessionManager))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("s3")
    fun provideS3OkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

    @Provides @Singleton @Named("noauth")
    fun provideNoAuthRetrofit(
        @Named("noauth") okHttp: OkHttpClient,
        baseUrl: HttpUrl
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideRetrofit(
        @Named("auth") okHttpClient: OkHttpClient,
        baseUrl: HttpUrl
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
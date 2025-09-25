package com.sos.chakhaeng.core.di

import com.sos.chakhaeng.data.repository.AuthRepositoryImpl
import com.sos.chakhaeng.data.repository.FakeProfileRepositoryImpl
import com.sos.chakhaeng.data.repository.HomeRepositoryImpl
import com.sos.chakhaeng.data.repository.ReportRepositoryImpl
import com.sos.chakhaeng.data.repository.FakeStatisticsRepositoryImpl
import com.sos.chakhaeng.data.repository.VideoRepositoryImpl
import com.sos.chakhaeng.domain.repository.ReportRepository
import com.sos.chakhaeng.data.repository.ViolationRepositoryImpl
import com.sos.chakhaeng.domain.repository.AuthRepository
import com.sos.chakhaeng.domain.repository.HomeRepository
import com.sos.chakhaeng.domain.repository.ProfileRepository
import com.sos.chakhaeng.domain.repository.StatisticsRepository
import com.sos.chakhaeng.domain.repository.VideoRepository
import com.sos.chakhaeng.domain.repository.ViolationRepository
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

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ) : ReportRepository

    @Binds
    @Singleton
    abstract fun bindViolationRepository(
        violationRepositoryImpl: ViolationRepositoryImpl
    ) : ViolationRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ) : VideoRepository

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        fakeStatisticsRepositoryImpl: FakeStatisticsRepositoryImpl
    ) : StatisticsRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        fakeProfileRepositoryImpl: FakeProfileRepositoryImpl
    ) : ProfileRepository

}
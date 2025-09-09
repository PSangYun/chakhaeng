package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.model.home.TodayStats

interface HomeRepository {
    suspend fun getTodayStats(): Result<TodayStats>

    suspend fun getRecentViolation(): Result<List<RecentViolation>>
}
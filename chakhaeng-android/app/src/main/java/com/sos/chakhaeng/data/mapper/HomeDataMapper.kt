package com.sos.chakhaeng.data.mapper

import com.sos.chakhaeng.data.network.dto.response.home.RecentViolationDTO
import com.sos.chakhaeng.data.network.dto.response.home.TodayStatsDTO
import com.sos.chakhaeng.domain.model.home.RecentViolation
import com.sos.chakhaeng.domain.model.home.TodayStats
import java.time.Instant

object HomeDataMapper {

    fun TodayStatsDTO.toEntity(): TodayStats =
        TodayStats(
            todayDetectionCnt = todayDetected,
            todayReportCnt = todayReported
        )

    fun RecentViolationDTO.toEntity(): RecentViolation =
        RecentViolation(
            violationId = violationId,
            type = type,
            typeLabel = typeLabel,
            location = locationText,
            carNumber = plate,
            timestamp = Instant.parse(occurredAt).toEpochMilli()
        )
}
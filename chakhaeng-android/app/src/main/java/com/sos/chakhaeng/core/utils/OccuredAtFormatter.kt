package com.sos.chakhaeng.core.utils

import java.time.*
import java.time.format.DateTimeFormatter

object OccurredAtFormatter {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    /** occurredAt ISO-8601을 (date, time)으로 분리. 기본은 시스템 시간대 */
    fun split(iso: String, zone: ZoneId = ZoneId.systemDefault()): Pair<String, String> {
        return runCatching {
            val instant = parseToInstant(iso)
            val zdt = instant.atZone(zone)
            dateFmt.format(zdt) to timeFmt.format(zdt)
        }.getOrElse {
            // 파싱 실패 시: 날짜엔 원문, 시간은 빈 값
            iso to ""
        }
    }

    private fun parseToInstant(iso: String): Instant {
        return runCatching { Instant.parse(iso) }
            .recoverCatching { OffsetDateTime.parse(iso).toInstant() }
            .recoverCatching { ZonedDateTime.parse(iso).toInstant() }
            .recoverCatching { LocalDateTime.parse(iso).atZone(ZoneOffset.UTC).toInstant() }
            .getOrThrow()
    }
}
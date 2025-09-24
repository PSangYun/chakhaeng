package com.sos.chakhaeng.core.utils

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime

object TimeAgo {

    /** occurredAt ~ now 사이의 경과를 "n분전 / n시간전 / n일전"으로 반환 */
    fun from(occurredAt: Instant, now: Instant = Instant.now()): String {
        val secs = Duration.between(occurredAt, now).seconds.coerceAtLeast(0)
        return when {
            secs < 3600 -> { // 1시간 이내 -> 분
                val minutes = (secs / 60).toInt().coerceAtLeast(1)
                "${minutes}분전"
            }
            secs < 86_400 -> { // 24시간 이내 -> 시간
                val hours = (secs / 3600).toInt().coerceAtLeast(1)
                "${hours}시간전"
            }
            else -> { // 그 이상 -> 일
                val days = (secs / 86_400).toInt().coerceAtLeast(1)
                "${days}일전"
            }
        }
    }

    fun from(occurredAtIso: String, now: Instant = Instant.now()): String {
        val inst = parseIsoToInstant(occurredAtIso) ?: return "방금전"
        return from(inst, now)
    }

    private fun parseIsoToInstant(s: String): Instant? {
        return runCatching { Instant.parse(s) }.getOrElse {
            runCatching { OffsetDateTime.parse(s).toInstant() }.getOrElse {
                runCatching { ZonedDateTime.parse(s).toInstant() }.getOrNull()
            }
        }
    }
}
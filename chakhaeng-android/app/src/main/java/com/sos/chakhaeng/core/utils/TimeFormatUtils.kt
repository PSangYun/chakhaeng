package com.sos.chakhaeng.core.utils
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

// 내부 포맷터들
private val FMT_HHMM      = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
private val FMT_HHMMSS    = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.KOREAN)
private val FMT_KO_HM     = DateTimeFormatter.ofPattern("a h시 m분", Locale.KOREAN)
private val FMT_KO_HMS    = DateTimeFormatter.ofPattern("a h시 m분 s초", Locale.KOREAN)

fun String.toEpochMillisOrNull(): Long? = runCatching {
    LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()
}.getOrNull()

fun Long.toYmd(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String.toHourMinute(): Pair<Int, Int> = runCatching {
    val p = split(":"); (p.getOrNull(0)?.toInt() ?: 0) to (p.getOrNull(1)?.toInt() ?: 0)
}.getOrElse { 0 to 0 }

/** "HH:mm[:ss]" 문자열을 LocalTime으로 안전 파싱 (실패 시 null) */
fun String?.parseTimeOrNull(): LocalTime? {
    if (this.isNullOrBlank()) return null
    return runCatching { LocalTime.parse(this, FMT_HHMMSS) }.getOrElse {
        runCatching { LocalTime.parse(this, FMT_HHMM) }.getOrNull()
    }
}

/** LocalTime → "오전/오후 h시 m분" (예: 오후 1시 54분) */
fun LocalTime.toKoreanAmPm(): String = this.format(FMT_KO_HM)

/** LocalTime → "오전/오후 h시 m분 s초" */
fun LocalTime.toKoreanAmPmWithSeconds(): String = this.format(FMT_KO_HMS)

/** "HH:mm[:ss]" → "오전/오후 h시 m분" (값이 없으면 대시 반환) */
fun formatKoreanTime(value: String?, emptyText: String = "—"): String =
    value.parseTimeOrNull()?.format(FMT_KO_HM) ?: emptyText

/** "HH:mm[:ss]" → "오전/오후 h시 m분 s초" (값이 없으면 대시 반환) */
fun formatKoreanTimeWithSeconds(value: String?, emptyText: String = "—"): String =
    value.parseTimeOrNull()?.format(FMT_KO_HMS) ?: emptyText

/** 시·분(·초) → "HH:mm:ss" 표준 문자열 */
fun toHms(hours: Int, minutes: Int, seconds: Int = 0): String =
    "%02d:%02d:%02d".format(hours, minutes, seconds)

/** LocalTime → "HH:mm:ss" */
fun LocalTime.toHms(): String = this.format(FMT_HHMMSS)

// ===== ISO-8601 UTC 'Z' 헬퍼 =====
private val FMT_ISO_Z: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(ZoneOffset.UTC)

/** Instant → "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" */
fun Instant.toIsoZ(): String = FMT_ISO_Z.format(this)

/** epochMillis → ISO-Z */
fun Long.toIsoZ(): String = Instant.ofEpochMilli(this).toIsoZ()

/** LocalDateTime(시스템 타임존 기준) → ISO-Z */
fun LocalDateTime.toIsoZ(): String =
    this.atZone(ZoneId.systemDefault()).toInstant().toIsoZ()

/** 현재 시각 → ISO-Z */
fun nowIsoZ(): String = Instant.now().toIsoZ()

/** 최근 X분 범위 [from,to]를 ISO-Z로 반환 */
fun lastMinutesRangeIsoZ(minutes: Long): Pair<String, String> {
    val to = Instant.now()
    val from = to.minusSeconds(minutes * 60)
    return from.toIsoZ() to to.toIsoZ()
}

/** "yyyy-MM-dd'T'HH:mm:ss[.SSS]Z" → Instant (실패 시 null) */
fun String.toInstantOrNull(): Instant? = runCatching {
    Instant.parse(this) // 표준 ISO-8601 파서
}.getOrNull()

package com.sos.chakhaeng.domain.model.location

data class Location(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        // 서울시청 기본 좌표
        val DEFAULT = Location(37.5665, 126.9780)

        // 유효성 검사
        fun isValid(latitude: Double, longitude: Double): Boolean {
            return latitude in -90.0..90.0 && longitude in -180.0..180.0
        }
    }
}

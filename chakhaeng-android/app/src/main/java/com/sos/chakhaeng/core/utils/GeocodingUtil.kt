package com.sos.chakhaeng.core.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

/**
 * Geocoding 유틸리티 클래스
 * 주소를 좌표로 변환하는 기능을 제공합니다
 */
object GeocodingUtil {

    /**
     * 주소를 좌표(LatLng)로 변환
     * @param context Android Context
     * @param address 변환할 주소 문자열
     * @return LatLng 좌표 또는 null (변환 실패 시)
     */
    suspend fun getLocationFromAddress(
        context: Context,
        address: String
    ): LatLng? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null

        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // Android API 33 이상에서는 Geocoder.GeocodeListener 사용 권장
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // 새로운 API 사용
                return@withContext getLocationFromAddressNew(geocoder, address)
            } else {
                // 기존 API 사용
                return@withContext getLocationFromAddressLegacy(geocoder, address)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Android API 33 이상용 Geocoding
     */
    @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.TIRAMISU)
    private suspend fun getLocationFromAddressNew(
        geocoder: Geocoder,
        address: String
    ): LatLng? = withContext(Dispatchers.IO) {
        try {
            // TODO: 새로운 비동기 API 구현
            // 현재는 기존 방식으로 대체
            return@withContext getLocationFromAddressLegacy(geocoder, address)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * 기존 Geocoding API 사용
     */
    private fun getLocationFromAddressLegacy(
        geocoder: Geocoder,
        address: String
    ): LatLng? {
        return try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList?.isNotEmpty() == true) {
                val location = addressList[0]
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 좌표를 주소로 변환 (역 Geocoding)
     * @param context Android Context
     * @param lat 위도
     * @param lng 경도
     * @return 주소 문자열 또는 null
     */
    suspend fun getAddressFromLocation(
        context: Context,
        lat: Double,
        lng: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList = geocoder.getFromLocation(lat, lng, 1)

            if (addressList?.isNotEmpty() == true) {
                val address = addressList[0]
                // 상세 주소 조합
                buildString {
                    address.adminArea?.let { append("$it ") } // 시/도
                    address.subAdminArea?.let { append("$it ") } // 시/군/구
                    address.thoroughfare?.let { append("$it ") } // 도로명
                    address.subThoroughfare?.let { append(it) } // 건물번호
                }.trim()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 한국 주요 지역의 대략적인 좌표를 반환하는 fallback 함수
     */
    fun getApproximateLocation(address: String): LatLng? {
        return when {
            // 서울 지역
            address.contains("강남구") -> LatLng(37.4979, 127.0276)
            address.contains("서초구") -> LatLng(37.4837, 127.0324)
            address.contains("송파구") -> LatLng(37.5145, 127.1066)
            address.contains("강동구") -> LatLng(37.5301, 127.1238)
            address.contains("마포구") -> LatLng(37.5663, 126.9019)
            address.contains("용산구") -> LatLng(37.5311, 126.9810)
            address.contains("성동구") -> LatLng(37.5631, 127.0370)
            address.contains("광진구") -> LatLng(37.5384, 127.0822)
            address.contains("동대문구") -> LatLng(37.5744, 127.0395)
            address.contains("중랑구") -> LatLng(37.6061, 127.0925)
            address.contains("성북구") -> LatLng(37.5894, 127.0167)
            address.contains("강북구") -> LatLng(37.6398, 127.0257)
            address.contains("도봉구") -> LatLng(37.6687, 127.0471)
            address.contains("노원구") -> LatLng(37.6542, 127.0568)
            address.contains("은평구") -> LatLng(37.6027, 126.9291)
            address.contains("서대문구") -> LatLng(37.5791, 126.9368)
            address.contains("종로구") -> LatLng(37.5735, 126.9788)
            address.contains("중구") -> LatLng(37.5640, 126.9970)
            address.contains("영등포구") -> LatLng(37.5264, 126.8962)
            address.contains("동작구") -> LatLng(37.5124, 126.9393)
            address.contains("관악구") -> LatLng(37.4781, 126.9515)
            address.contains("금천구") -> LatLng(37.4569, 126.9010)
            address.contains("구로구") -> LatLng(37.4955, 126.8872)
            address.contains("양천구") -> LatLng(37.5168, 126.8664)
            address.contains("강서구") -> LatLng(37.5509, 126.8495)

            // 경기도 주요 지역
            address.contains("수원") -> LatLng(37.2636, 127.0286)
            address.contains("성남") -> LatLng(37.4201, 127.1262)
            address.contains("고양") -> LatLng(37.6584, 126.8320)
            address.contains("안양") -> LatLng(37.3943, 126.9568)
            address.contains("부천") -> LatLng(37.5034, 126.7660)
            address.contains("의정부") -> LatLng(37.7384, 127.0338)
            address.contains("안산") -> LatLng(37.3218, 126.8309)

            // 기타 주요 도시
            address.contains("부산") -> LatLng(35.1796, 129.0756)
            address.contains("대구") -> LatLng(35.8714, 128.6014)
            address.contains("인천") -> LatLng(37.4563, 126.7052)
            address.contains("광주") -> LatLng(35.1595, 126.8526)
            address.contains("대전") -> LatLng(36.3504, 127.3845)
            address.contains("울산") -> LatLng(35.5384, 129.3114)

            // 테헤란로 (구체적인 도로명)
            address.contains("테헤란로") -> LatLng(37.5048, 127.0408)

            else -> null
        }
    }
}
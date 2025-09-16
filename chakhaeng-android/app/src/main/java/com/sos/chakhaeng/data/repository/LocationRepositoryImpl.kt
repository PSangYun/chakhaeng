package com.sos.chakhaeng.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.sos.chakhaeng.domain.model.location.Address as DomainAddress
import com.sos.chakhaeng.domain.model.location.Location
import com.sos.chakhaeng.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    override suspend fun getLocationFromAddress(address: DomainAddress): Location? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // Android API 33 이상에서는 새로운 API 사용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return@withContext getLocationFromAddressNew(geocoder, address.fullAddress)
            } else {
                return@withContext getLocationFromAddressLegacy(geocoder, address.fullAddress)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getAddressFromLocation(location: Location): DomainAddress? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            // Android API 33 이상에서는 새로운 API 사용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return@withContext getAddressFromLocationNew(geocoder, location)
            } else {
                return@withContext getAddressFromLocationLegacy(geocoder, location)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Android API 33 이상용 새로운 Geocoding API
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getLocationFromAddressNew(
        geocoder: Geocoder,
        address: String
    ): Location? = suspendCancellableCoroutine { continuation ->
        try {
            geocoder.getFromLocationName(address, 1) { addressList ->
                if (addressList.isNotEmpty()) {
                    val location = addressList[0]
                    continuation.resume(
                        Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                } else {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    /**
     * Android API 33 이상용 새로운 역 Geocoding API
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getAddressFromLocationNew(
        geocoder: Geocoder,
        location: Location
    ): DomainAddress? = suspendCancellableCoroutine { continuation ->
        try {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addressList ->
                if (addressList.isNotEmpty()) {
                    val androidAddress = addressList[0]
                    continuation.resume(
                        DomainAddress(
                            fullAddress = buildAddressString(androidAddress),
                            city = androidAddress.adminArea,
                            district = androidAddress.subAdminArea,
                            street = androidAddress.thoroughfare,
                            buildingNumber = androidAddress.subThoroughfare,
                            postalCode = androidAddress.postalCode
                        )
                    )
                } else {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    /**
     * 기존 Geocoding API (Android API 32 이하)
     */
    @Suppress("DEPRECATION")
    private fun getLocationFromAddressLegacy(
        geocoder: Geocoder,
        address: String
    ): Location? {
        return try {
            val addressList = geocoder.getFromLocationName(address, 1)
            if (addressList?.isNotEmpty() == true) {
                val location = addressList[0]
                Location(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 기존 역 Geocoding API (Android API 32 이하)
     */
    @Suppress("DEPRECATION")
    private fun getAddressFromLocationLegacy(
        geocoder: Geocoder,
        location: Location
    ): DomainAddress? {
        return try {
            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addressList?.isNotEmpty() == true) {
                val androidAddress = addressList[0]
                DomainAddress(
                    fullAddress = buildAddressString(androidAddress),
                    city = androidAddress.adminArea,
                    district = androidAddress.subAdminArea,
                    street = androidAddress.thoroughfare,
                    buildingNumber = androidAddress.subThoroughfare,
                    postalCode = androidAddress.postalCode
                )
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Android Address 객체를 문자열로 변환
     */
    private fun buildAddressString(address: Address): String {
        return buildString {
            address.adminArea?.let { append("$it ") }
            address.subAdminArea?.let { append("$it ") }
            address.thoroughfare?.let { append("$it ") }
            address.subThoroughfare?.let { append(it) }
        }.trim()
    }
}
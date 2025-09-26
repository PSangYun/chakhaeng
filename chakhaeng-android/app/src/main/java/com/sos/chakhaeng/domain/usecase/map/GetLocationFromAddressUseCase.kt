package com.sos.chakhaeng.domain.usecase.map

import android.util.Log
import com.sos.chakhaeng.domain.model.location.Address
import com.sos.chakhaeng.domain.model.location.Location
import com.sos.chakhaeng.domain.repository.LocationRepository
import javax.inject.Inject

class GetLocationFromAddressUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(addressString: String): Location? {
        if (addressString.isBlank()) return null

        val address = Address.fromString(addressString)

        return try {
            locationRepository.getLocationFromAddress(address)
        } catch (e: Exception) {
            Log.d("test21337",e.message,e)
            null
        }
    }
}
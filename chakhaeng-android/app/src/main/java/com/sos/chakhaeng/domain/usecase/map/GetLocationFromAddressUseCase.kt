package com.sos.chakhaeng.domain.usecase.map

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
            null
        }
    }
}
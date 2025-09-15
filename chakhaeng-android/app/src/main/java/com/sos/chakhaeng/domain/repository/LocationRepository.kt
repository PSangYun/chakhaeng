package com.sos.chakhaeng.domain.repository

import com.sos.chakhaeng.domain.model.location.Address
import com.sos.chakhaeng.domain.model.location.Location

interface LocationRepository {
    suspend fun getLocationFromAddress(address: Address): Location?
    suspend fun getAddressFromLocation(location: Location): Address?
}
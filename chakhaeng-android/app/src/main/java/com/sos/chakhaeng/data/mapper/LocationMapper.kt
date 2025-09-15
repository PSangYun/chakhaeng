package com.sos.chakhaeng.data.mapper

import com.google.android.gms.maps.model.LatLng
import com.sos.chakhaeng.domain.model.location.Location

object LocationMapper {

    fun toLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    fun toDomain(latLng: LatLng): Location {
        return Location(
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
    }
}
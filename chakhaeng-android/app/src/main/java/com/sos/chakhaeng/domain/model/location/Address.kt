package com.sos.chakhaeng.domain.model.location

data class Address(
    val fullAddress: String,
    val city: String? = null,
    val district: String? = null,
    val street: String? = null,
    val buildingNumber: String? = null,
    val postalCode: String? = null
) {
    companion object {
        fun fromString(address: String): Address {
            return Address(fullAddress = address)
        }
    }
}
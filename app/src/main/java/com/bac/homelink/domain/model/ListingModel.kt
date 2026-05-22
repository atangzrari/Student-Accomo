package com.bac.homelink.domain.model

data class ListingModel(
    val id: Int = 0,
    val title: String,
    val description: String,
    val pricePerMonth: Int,
    val depositAmount: Int,
    val securityAmount: Int = 0,
    val location: String,
    val address: String,
    val accommodationType: String,
    val roomCount: String,
    val sharingArrangement: String,
    val amenities: List<String>,
    val availabilityDate: String,
    val imageUrl: String,
    val imageUrl2: String = "",
    val imageUrl3: String = "",
    val landlordName: String,
    val landlordPhone: String,
    val landlordEmail: String,
    val status: ListingStatus = ListingStatus.AVAILABLE,
    val reservedByUserId: Int = 0,
    val reservationReference: String = "",
    val reservedStudentName: String = "",
    val reservedStudentId: String = "",
    val reservedStudentEmail: String = "",
    val reservedMoveInDate: String = "",
    val bedroomsCount: Int = 1,
    val bathroomsCount: Int = 1,
    val additionalNotes: String = "",
    val isFavourite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ListingStatus { AVAILABLE, RESERVED, UNAVAILABLE }

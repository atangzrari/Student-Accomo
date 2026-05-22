package com.bac.homelink.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val pricePerMonth: Int,
    val depositAmount: Int,
    val securityAmount: Int = 0,
    val location: String,
    val fullAddress: String,
    val accommodationType: String,
    val roomCount: String,
    val sharingArrangement: String,
    val amenities: String,         // comma-separated from fixed list
    val availabilityDate: String,
    val imageUrl: String,
    val imageUrl2: String = "",
    val imageUrl3: String = "",
    val landlordName: String,
    val landlordPhone: String,
    val landlordEmail: String,
    val status: String = "AVAILABLE",
    val reservedByUserId: Int = 0,
    val reservationReference: String = "",
    val reservedStudentName: String = "",
    val reservedStudentId: String = "",
    val reservedStudentEmail: String = "",
    val reservedMoveInDate: String = "",
    val bedroomsCount: Int = 1,
    val bathroomsCount: Int = 1,
    val additionalNotes: String = "",
    val favouritedByUserIds: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val address: String get() = fullAddress
}

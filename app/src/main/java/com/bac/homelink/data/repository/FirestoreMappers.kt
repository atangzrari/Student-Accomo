package com.bac.homelink.data.repository

import com.bac.homelink.data.entities.Listing
import com.bac.homelink.data.entities.Reservation
import com.bac.homelink.data.entities.User
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus
import com.bac.homelink.domain.model.ReservationModel
import com.bac.homelink.domain.model.UserModel
import com.bac.homelink.domain.model.UserRole
import kotlin.math.absoluteValue

private fun Any?.asInt(default: Int = 0): Int = when (this) {
    is Number -> toInt()
    is String -> toIntOrNull() ?: default
    else -> default
}

private fun Any?.asLong(default: Long = 0L): Long = when (this) {
    is Number -> toLong()
    is String -> toLongOrNull() ?: default
    else -> default
}

internal fun stableFirestoreIntId(seed: String): Int {
    val id = seed.lowercase().trim().hashCode().absoluteValue
    return if (id == 0) 1 else id
}

internal fun UserModel.toFirestoreMap(passwordHash: String): Map<String, Any> = mapOf(
    "id" to id,
    "studentId" to studentId,
    "fullName" to fullName,
    "email" to email,
    "phone" to phone,
    "passwordHash" to passwordHash,
    "role" to role.name,
    "institution" to institution,
    "profileImageUrl" to profileImageUrl,
    "savedFilterPrice" to savedFilterPrice,
    "savedFilterLocation" to savedFilterLocation,
    "savedFilterDate" to savedFilterDate,
    "createdAt" to System.currentTimeMillis()
)

internal fun Map<String, Any?>.toUserEntity(): User = User(
    id = this["id"].asInt(),
    studentId = this["studentId"] as? String ?: "",
    fullName = this["fullName"] as? String ?: "",
    email = this["email"] as? String ?: "",
    phone = this["phone"] as? String ?: "",
    passwordHash = this["passwordHash"] as? String ?: "",
    role = this["role"] as? String ?: UserRole.STUDENT.name,
    institution = this["institution"] as? String ?: "BAC",
    profileImageUrl = this["profileImageUrl"] as? String ?: "",
    savedFilterPrice = this["savedFilterPrice"].asInt(),
    savedFilterLocation = this["savedFilterLocation"] as? String ?: "",
    savedFilterDate = this["savedFilterDate"] as? String ?: "",
    createdAt = this["createdAt"].asLong(System.currentTimeMillis())
)

internal fun ListingModel.toFirestoreMap(favouritedByUserIds: String = ""): Map<String, Any> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "pricePerMonth" to pricePerMonth,
    "depositAmount" to depositAmount,
    "securityAmount" to securityAmount,
    "location" to location,
    "fullAddress" to address,
    "accommodationType" to accommodationType,
    "roomCount" to roomCount,
    "sharingArrangement" to sharingArrangement,
    "amenities" to amenities,
    "availabilityDate" to availabilityDate,
    "imageUrl" to imageUrl,
    "imageUrl2" to imageUrl2,
    "imageUrl3" to imageUrl3,
    "landlordName" to landlordName,
    "landlordPhone" to landlordPhone,
    "landlordEmail" to landlordEmail,
    "status" to status.name,
    "reservedByUserId" to reservedByUserId,
    "reservationReference" to reservationReference,
    "reservedStudentName" to reservedStudentName,
    "reservedStudentId" to reservedStudentId,
    "reservedStudentEmail" to reservedStudentEmail,
    "reservedMoveInDate" to reservedMoveInDate,
    "bedroomsCount" to bedroomsCount,
    "bathroomsCount" to bathroomsCount,
    "additionalNotes" to additionalNotes,
    "favouritedByUserIds" to favouritedByUserIds,
    "createdAt" to createdAt
)

internal fun Map<String, Any?>.toListingEntity(): Listing {
    val amenitiesValue = this["amenities"]
    val amenities = when (amenitiesValue) {
        is List<*> -> amenitiesValue.filterIsInstance<String>().joinToString(", ")
        is String -> amenitiesValue
        else -> ""
    }
    return Listing(
        id = this["id"].asInt(),
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        pricePerMonth = this["pricePerMonth"].asInt(),
        depositAmount = this["depositAmount"].asInt(),
        securityAmount = this["securityAmount"].asInt(),
        location = this["location"] as? String ?: "",
        fullAddress = this["fullAddress"] as? String ?: this["address"] as? String ?: "",
        accommodationType = this["accommodationType"] as? String ?: "",
        roomCount = this["roomCount"] as? String ?: "",
        sharingArrangement = this["sharingArrangement"] as? String ?: "",
        amenities = amenities,
        availabilityDate = this["availabilityDate"] as? String ?: "",
        imageUrl = this["imageUrl"] as? String ?: "",
        imageUrl2 = this["imageUrl2"] as? String ?: "",
        imageUrl3 = this["imageUrl3"] as? String ?: "",
        landlordName = this["landlordName"] as? String ?: "",
        landlordPhone = this["landlordPhone"] as? String ?: "",
        landlordEmail = this["landlordEmail"] as? String ?: "",
        status = this["status"] as? String ?: ListingStatus.AVAILABLE.name,
        reservedByUserId = this["reservedByUserId"].asInt(),
        reservationReference = this["reservationReference"] as? String ?: "",
        reservedStudentName = this["reservedStudentName"] as? String ?: "",
        reservedStudentId = this["reservedStudentId"] as? String ?: "",
        reservedStudentEmail = this["reservedStudentEmail"] as? String ?: "",
        reservedMoveInDate = this["reservedMoveInDate"] as? String ?: "",
        bedroomsCount = this["bedroomsCount"].asInt(1),
        bathroomsCount = this["bathroomsCount"].asInt(1),
        additionalNotes = this["additionalNotes"] as? String ?: "",
        favouritedByUserIds = this["favouritedByUserIds"] as? String ?: "",
        createdAt = this["createdAt"].asLong(System.currentTimeMillis())
    )
}

internal fun ReservationModel.toFirestoreMap(): Map<String, Any> = mapOf(
    "id" to id,
    "listingId" to listingId,
    "userId" to userId,
    "referenceNumber" to referenceNumber,
    "depositAmountPaid" to depositAmountPaid,
    "paymentMethod" to paymentMethod,
    "status" to status,
    "studentName" to studentName,
    "studentId" to studentId,
    "listingTitle" to listingTitle,
    "listingLocation" to listingLocation,
    "moveInDate" to moveInDate,
    "createdAt" to createdAt
)

internal fun Map<String, Any?>.toReservationEntity(): Reservation = Reservation(
    id = this["id"].asInt(),
    listingId = this["listingId"].asInt(),
    userId = this["userId"].asInt(),
    referenceNumber = this["referenceNumber"] as? String ?: "",
    depositAmountPaid = this["depositAmountPaid"].asInt(),
    paymentMethod = this["paymentMethod"] as? String ?: "SIMULATED",
    status = this["status"] as? String ?: "CONFIRMED",
    studentName = this["studentName"] as? String ?: "",
    studentId = this["studentId"] as? String ?: "",
    listingTitle = this["listingTitle"] as? String ?: "",
    listingLocation = this["listingLocation"] as? String ?: "",
    moveInDate = this["moveInDate"] as? String ?: "",
    createdAt = this["createdAt"].asLong(System.currentTimeMillis())
)

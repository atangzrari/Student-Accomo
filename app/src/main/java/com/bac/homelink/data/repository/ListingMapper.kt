package com.bac.homelink.data.repository

import com.bac.homelink.data.entities.Listing
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus

fun Listing.toDomain(): ListingModel = ListingModel(
    id                 = id,
    title              = title,
    description        = description,
    pricePerMonth      = pricePerMonth,
    depositAmount      = depositAmount,
    securityAmount     = securityAmount,
    location           = location,
    address            = fullAddress,
    accommodationType  = accommodationType,
    roomCount          = roomCount,
    sharingArrangement = sharingArrangement,
    amenities          = amenities.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    availabilityDate   = availabilityDate,
    imageUrl           = imageUrl,
    imageUrl2          = imageUrl2,
    imageUrl3          = imageUrl3,
    landlordName       = landlordName,
    landlordPhone      = landlordPhone,
    landlordEmail      = landlordEmail,
    status             = when (status) {
        "RESERVED"    -> ListingStatus.RESERVED
        "UNAVAILABLE" -> ListingStatus.UNAVAILABLE
        else          -> ListingStatus.AVAILABLE
    },
    reservedByUserId   = reservedByUserId,
    reservationReference = reservationReference,
    reservedStudentName = reservedStudentName,
    reservedStudentId = reservedStudentId,
    reservedStudentEmail = reservedStudentEmail,
    reservedMoveInDate = reservedMoveInDate,
    bedroomsCount      = bedroomsCount,
    bathroomsCount     = bathroomsCount,
    additionalNotes    = additionalNotes,
    createdAt          = createdAt
)

fun ListingModel.toEntity(): Listing = Listing(
    id                 = id,
    title              = title,
    description        = description,
    pricePerMonth      = pricePerMonth,
    depositAmount      = depositAmount,
    securityAmount     = securityAmount,
    location           = location,
    fullAddress        = address,
    accommodationType  = accommodationType,
    roomCount          = roomCount,
    sharingArrangement = sharingArrangement,
    amenities          = amenities.joinToString(", "),
    availabilityDate   = availabilityDate,
    imageUrl           = imageUrl,
    imageUrl2          = imageUrl2,
    imageUrl3          = imageUrl3,
    landlordName       = landlordName,
    landlordPhone      = landlordPhone,
    landlordEmail      = landlordEmail,
    status             = status.name,
    reservedByUserId   = reservedByUserId,
    reservationReference = reservationReference,
    reservedStudentName = reservedStudentName,
    reservedStudentId = reservedStudentId,
    reservedStudentEmail = reservedStudentEmail,
    reservedMoveInDate = reservedMoveInDate,
    bedroomsCount      = bedroomsCount,
    bathroomsCount     = bathroomsCount,
    additionalNotes    = additionalNotes,
    createdAt          = createdAt
)

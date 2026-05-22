package com.bac.homelink.data.repository
import com.bac.homelink.data.entities.Reservation
import com.bac.homelink.domain.model.ReservationModel

fun Reservation.toDomain():ReservationModel = ReservationModel(id=id,listingId=listingId,userId=userId,
    referenceNumber=referenceNumber,depositAmountPaid=depositAmountPaid,paymentMethod=paymentMethod,
    status=status,studentName=studentName,studentId=studentId,listingTitle=listingTitle,
    listingLocation=listingLocation,moveInDate=moveInDate,createdAt=createdAt)

fun ReservationModel.toEntity():Reservation = Reservation(id=id,listingId=listingId,userId=userId,
    referenceNumber=referenceNumber,depositAmountPaid=depositAmountPaid,paymentMethod=paymentMethod,
    status=status,studentName=studentName,studentId=studentId,listingTitle=listingTitle,
    listingLocation=listingLocation,moveInDate=moveInDate,createdAt=createdAt)

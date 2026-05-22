package com.bac.homelink.domain.model
data class ReservationModel(val id:Int=0, val listingId:Int, val userId:Int, val referenceNumber:String,
    val depositAmountPaid:Int, val paymentMethod:String, val status:String="CONFIRMED",
    val studentName:String, val studentId:String, val listingTitle:String, val listingLocation:String,
    val moveInDate:String, val createdAt:Long=System.currentTimeMillis())

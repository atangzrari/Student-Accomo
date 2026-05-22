package com.bac.homelink.data.entities
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName = "reservations")
data class Reservation(@PrimaryKey(autoGenerate=true) val id:Int=0, val listingId:Int, val userId:Int,
    val referenceNumber:String, val depositAmountPaid:Int, val paymentMethod:String="SIMULATED",
    val status:String="CONFIRMED", val studentName:String, val studentId:String, val listingTitle:String,
    val listingLocation:String, val moveInDate:String, val createdAt:Long=System.currentTimeMillis())

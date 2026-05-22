package com.bac.homelink.data.entities
import androidx.room.Entity; import androidx.room.PrimaryKey
@Entity(tableName = "users")
data class User(@PrimaryKey(autoGenerate=true) val id:Int=0, val studentId:String, val fullName:String,
    val email:String, val phone:String, val passwordHash:String, val role:String="STUDENT",
    val institution:String="BAC", val profileImageUrl:String="", val savedFilterPrice:Int=0,
    val savedFilterLocation:String="", val savedFilterDate:String="", val notificationsEnabled:Boolean=true,
    val createdAt:Long=System.currentTimeMillis())

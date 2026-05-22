package com.bac.homelink.domain.model
data class UserModel(val id:Int=0, val studentId:String, val fullName:String, val email:String,
    val phone:String, val role:UserRole=UserRole.STUDENT, val institution:String="BAC",
    val profileImageUrl:String="", val savedFilterPrice:Int=0, val savedFilterLocation:String="",
    val savedFilterDate:String="")
enum class UserRole { STUDENT, PROVIDER }

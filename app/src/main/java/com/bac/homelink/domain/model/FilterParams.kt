package com.bac.homelink.domain.model
data class FilterParams(val maxPrice:Int=0, val location:String="", val availabilityDate:String="",
    val accommodationType:String="", val sharingArrangement:String="", val searchQuery:String="") {
    val isActive:Boolean get() = maxPrice>0||location.isNotEmpty()||availabilityDate.isNotEmpty()||
        accommodationType.isNotEmpty()||sharingArrangement.isNotEmpty()
}

package com.bac.homelink.domain.repository

import com.bac.homelink.domain.model.FilterParams
import com.bac.homelink.domain.model.ListingModel
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun getAvailableListings(): Flow<List<ListingModel>>
    fun getListingById(id: Int): Flow<ListingModel?>
    fun getListingsByLandlord(email: String): Flow<List<ListingModel>>
    fun filterListings(params: FilterParams): Flow<List<ListingModel>>
    fun getFavouriteListings(userId: Int): Flow<List<ListingModel>>
    suspend fun addListing(listing: ListingModel): Long
    suspend fun updateListing(listing: ListingModel)
    suspend fun deleteListing(listing: ListingModel)
    suspend fun updateListingStatus(listingId: Int, status: String, userId: Int)
    suspend fun toggleFavourite(listingId: Int, userId: Int)
    suspend fun getMatchingListings(maxPrice: Int, location: String): List<ListingModel>
}

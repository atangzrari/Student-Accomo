package com.bac.homelink.domain.usecase.listing
import com.bac.homelink.domain.model.*
import com.bac.homelink.domain.repository.ListingRepository
import kotlinx.coroutines.flow.*; import javax.inject.Inject

class GetAvailableListingsUseCase @Inject constructor(private val repo:ListingRepository) {
    operator fun invoke(params:FilterParams=FilterParams()):Flow<List<ListingModel>> =
        (if(params.isActive) repo.filterListings(params) else repo.getAvailableListings())
            .map { listings -> if(params.searchQuery.isBlank()) listings
                else listings.filter { val q=params.searchQuery.lowercase()
                    it.title.lowercase().contains(q)||it.location.lowercase().contains(q)||
                    it.accommodationType.lowercase().contains(q)||it.amenities.any{a->a.lowercase().contains(q)} } }
}

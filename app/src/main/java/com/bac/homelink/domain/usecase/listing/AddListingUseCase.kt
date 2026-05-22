package com.bac.homelink.domain.usecase.listing
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.repository.ListingRepository
import javax.inject.Inject

class AddListingUseCase @Inject constructor(private val repo:ListingRepository) {
    suspend operator fun invoke(listing:ListingModel):Result<Long> = runCatching {
        require(listing.title.isNotBlank()){"Title is required"}
        require(listing.pricePerMonth>0){"Price must be > 0"}
        require(listing.location.isNotBlank()){"Location is required"}
        require(listing.accommodationType.isNotBlank()){"Accommodation type required"}
        repo.addListing(listing)
    }
}

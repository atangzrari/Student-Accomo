package com.bac.homelink.domain.usecase.listing
import com.bac.homelink.domain.repository.ListingRepository; import javax.inject.Inject
class ToggleFavouriteUseCase @Inject constructor(private val repo:ListingRepository) {
    suspend operator fun invoke(listingId:Int,userId:Int) = repo.toggleFavourite(listingId,userId)
}

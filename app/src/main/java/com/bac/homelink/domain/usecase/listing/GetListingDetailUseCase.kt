package com.bac.homelink.domain.usecase.listing
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow; import javax.inject.Inject

class GetListingDetailUseCase @Inject constructor(private val repo:ListingRepository) {
    operator fun invoke(id:Int):Flow<ListingModel?> = repo.getListingById(id)
}

package com.bac.homelink.presentation.landlord.addlisting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.data.dao.UserDao
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.usecase.listing.AddListingUseCase
import com.bac.homelink.domain.usecase.listing.GetListingDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val addListingUseCase: AddListingUseCase,
    private val getListingDetailUseCase: GetListingDetailUseCase,
    private val listingRepository: com.bac.homelink.domain.repository.ListingRepository,
    private val session: SessionManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>?>(null)
    val state: StateFlow<UiState<Unit>?> = _state.asStateFlow()

    private val _successEvent = MutableSharedFlow<Unit>()
    val successEvent: SharedFlow<Unit> = _successEvent.asSharedFlow()

    private val _existingListing = MutableStateFlow<ListingModel?>(null)
    val existingListing: StateFlow<ListingModel?> = _existingListing.asStateFlow()

    val isEditMode: Boolean get() = _existingListing.value != null

    /** Load a listing for editing */
    fun loadForEditing(listingId: Int) {
        if (listingId <= 0) return
        viewModelScope.launch {
            val listing = getListingDetailUseCase(listingId).first()
            _existingListing.value = listing
        }
    }

    fun submitListing(listing: ListingModel) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            if (isEditMode) {
                // Update existing listing
                runCatching { listingRepository.updateListing(listing) }
                    .onSuccess { _state.value = UiState.Success(Unit); _successEvent.emit(Unit) }
                    .onFailure { _state.value = UiState.Error(it.message ?: "Update failed") }
            } else {
                // Add new listing
                addListingUseCase(listing)
                    .onSuccess { _state.value = UiState.Success(Unit); _successEvent.emit(Unit) }
                    .onFailure { _state.value = UiState.Error(it.message ?: "Failed to add listing") }
            }
        }
    }

    fun buildListing(
        id: Int = 0,
        title: String, description: String, price: Int, deposit: Int, security: Int,
        location: String, address: String, accType: String, roomCount: String, sharing: String,
        amenities: String, date: String, imageUrl: String, imageUrl2: String, imageUrl3: String,
        notes: String
    ): ListingModel {
        val phone = runBlocking {
            userDao.getUserByEmailSync(session.getUserEmail())?.phone ?: ""
        }
        return ListingModel(
            id                 = id,
            title              = title,
            description        = description.ifBlank { accType },
            pricePerMonth      = price,
            depositAmount      = if (deposit > 0) deposit else price,
            securityAmount     = security,
            location           = location,
            address            = address.ifBlank { "$location, Gaborone" },
            accommodationType  = accType,
            roomCount          = roomCount,
            sharingArrangement = sharing,
            amenities          = amenities.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            availabilityDate   = date,
            imageUrl           = imageUrl,
            imageUrl2          = imageUrl2,
            imageUrl3          = imageUrl3,
            landlordName       = session.getUserName(),
            landlordPhone      = phone,
            landlordEmail      = session.getUserEmail(),
            additionalNotes    = notes
        )
    }
}

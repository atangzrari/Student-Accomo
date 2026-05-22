package com.bac.homelink.presentation.landlord.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandlordViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val session: SessionManager
) : ViewModel() {

    val myListings: StateFlow<UiState<List<ListingModel>>> =
        listingRepository.getListingsByLandlord(session.getUserEmail())
            .map { list -> if (list.isEmpty()) UiState.Empty else UiState.Success(list) }
            .onStart { emit(UiState.Loading) }
            .catch { emit(UiState.Error(it.message ?: "Error")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val landlordName: String get() = session.getUserName().split(" ").firstOrNull() ?: "Landlord"
    val landlordEmail: String get() = session.getUserEmail()

    fun deleteListing(listing: ListingModel) {
        viewModelScope.launch {
            try {
                listingRepository.deleteListing(listing)
                _toastEvent.emit("Listing deleted successfully")
            } catch (e: Exception) {
                _toastEvent.emit("Failed to delete listing")
            }
        }
    }

    fun toggleAvailability(listing: ListingModel) {
        viewModelScope.launch {
            val newStatus = if (listing.status == com.bac.homelink.domain.model.ListingStatus.AVAILABLE)
                "UNAVAILABLE" else "AVAILABLE"
            listingRepository.updateListingStatus(listing.id, newStatus, 0)
            _toastEvent.emit("Listing marked as $newStatus")
        }
    }
}

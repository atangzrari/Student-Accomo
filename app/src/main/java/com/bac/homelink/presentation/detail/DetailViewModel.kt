package com.bac.homelink.presentation.detail
import androidx.lifecycle.*
import com.bac.homelink.core.ui.UiState; import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.usecase.listing.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getListingDetailUseCase:GetListingDetailUseCase,
    private val toggleFavouriteUseCase:ToggleFavouriteUseCase,
    private val session:SessionManager,
    savedStateHandle:SavedStateHandle):ViewModel() {

    private val listingId:Int = savedStateHandle.get<Int>("listing_id") ?: -1

    val listing:StateFlow<UiState<ListingModel>> = getListingDetailUseCase(listingId)
        .map { it?.let { m -> UiState.Success(m) } ?: UiState.Error("Listing not found") }
        .onStart { emit(UiState.Loading) }.catch { emit(UiState.Error(it.message?:"Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun toggleFavourite() { viewModelScope.launch { toggleFavouriteUseCase(listingId,session.getUserId()) } }
}

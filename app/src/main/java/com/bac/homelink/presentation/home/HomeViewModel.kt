package com.bac.homelink.presentation.home
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState; import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.domain.model.*
import com.bac.homelink.domain.usecase.listing.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi; import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getListingsUseCase:GetAvailableListingsUseCase,
    private val toggleFavouriteUseCase:ToggleFavouriteUseCase,
    private val session:SessionManager):ViewModel() {

    private val _filterParams = MutableStateFlow(FilterParams())
    val filterParams:StateFlow<FilterParams> = _filterParams.asStateFlow()
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val listings:StateFlow<UiState<List<ListingModel>>> = combine(_filterParams, _searchQuery.debounce(300))
        { filter, query -> filter.copy(searchQuery=query) }
        .flatMapLatest { params -> getListingsUseCase(params) }
        .map<List<ListingModel>,UiState<List<ListingModel>>> { if(it.isEmpty()) UiState.Empty else UiState.Success(it) }
        .onStart { emit(UiState.Loading) }
        .catch { emit(UiState.Error(it.message?:"Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val stats:StateFlow<HomeStats> = listings.map { state ->
        val list = (state as? UiState.Success)?.data ?: emptyList()
        HomeStats(available=list.size, minPrice=list.minOfOrNull{it.pricePerMonth}?:0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeStats())

    fun applyFilter(params:FilterParams) {
        _filterParams.value = params
        _searchQuery.value = params.searchQuery
    }
    fun saveFilterPreferences(params:FilterParams, alertsEnabled:Boolean) {
        viewModelScope.launch {
            session.saveFilterPreferences(
                params.maxPrice,
                params.location,
                params.availabilityDate,
                params.accommodationType,
                params.sharingArrangement,
                params.searchQuery,
                alertsEnabled
            )
        }
    }
    fun clearFilter() { _filterParams.value = FilterParams(); _searchQuery.value = "" }
    fun onSearchQuery(q:String) { _searchQuery.value=q }
    fun toggleFavourite(listingId:Int) { viewModelScope.launch { toggleFavouriteUseCase(listingId,session.getUserId()) } }
    fun loadSavedFilter() {
        val saved = FilterParams(
            maxPrice = session.getSavedFilterPrice(),
            location = session.getSavedFilterLocation(),
            availabilityDate = session.getSavedFilterDate(),
            accommodationType = session.getSavedFilterType(),
            sharingArrangement = session.getSavedFilterSharing(),
            searchQuery = session.getSavedFilterQuery()
        )
        _filterParams.value = saved
        _searchQuery.value = saved.searchQuery
    }
    fun savedFilterAlerts():Boolean = session.getSavedFilterAlerts()
    val userName:String get() = session.getUserName().split(" ").firstOrNull()?:"Student"
    val userId:Int get() = session.getUserId()
}
data class HomeStats(val available:Int=0, val minPrice:Int=0)

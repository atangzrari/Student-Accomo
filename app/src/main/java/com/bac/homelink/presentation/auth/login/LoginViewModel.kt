package com.bac.homelink.presentation.auth.login
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState; import com.bac.homelink.domain.model.*
import com.bac.homelink.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase:LoginUseCase):ViewModel() {
    private val _loginState = MutableStateFlow<UiState<UserModel>?>(null)
    val loginState:StateFlow<UiState<UserModel>?> = _loginState.asStateFlow()
    private val _navEvent = MutableSharedFlow<LoginNavEvent>()
    val navEvent:SharedFlow<LoginNavEvent> = _navEvent.asSharedFlow()

    fun login(email:String,password:String) {
        viewModelScope.launch {
            _loginState.value=UiState.Loading
            loginUseCase(email,password)
                .onSuccess { user -> _loginState.value=UiState.Success(user)
                    _navEvent.emit(if(user.role==UserRole.PROVIDER) LoginNavEvent.ToLandlordDashboard else LoginNavEvent.ToHome) }
                .onFailure { _loginState.value=UiState.Error(it.message?:"Login failed") }
        }
    }
}
sealed class LoginNavEvent { data object ToHome:LoginNavEvent(); data object ToLandlordDashboard:LoginNavEvent() }

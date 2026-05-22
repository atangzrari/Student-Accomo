package com.bac.homelink.presentation.auth.register
import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState; import com.bac.homelink.domain.model.*
import com.bac.homelink.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch; import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val registerUseCase:RegisterUseCase):ViewModel() {
    private val _state = MutableStateFlow<UiState<UserModel>?>(null)
    val registerState:StateFlow<UiState<UserModel>?> = _state.asStateFlow()
    private val _navEvent = MutableSharedFlow<RegisterNavEvent>()
    val navEvent:SharedFlow<RegisterNavEvent> = _navEvent.asSharedFlow()

    fun register(fullName:String,studentId:String,email:String,phone:String,password:String,confirmPassword:String,role:UserRole) {
        viewModelScope.launch {
            _state.value=UiState.Loading
            val generatedStudentId = "STU${System.currentTimeMillis().toString().takeLast(8)}"
            val user = UserModel(studentId=if(role==UserRole.STUDENT) generatedStudentId else "LANDLORD",fullName=fullName,email=email,phone=phone,role=role)
            registerUseCase(user,password,confirmPassword)
                .onSuccess { u -> _state.value=UiState.Success(u)
                    _navEvent.emit(if(role==UserRole.PROVIDER) RegisterNavEvent.ToLandlordDashboard else RegisterNavEvent.ToHome) }
                .onFailure { _state.value=UiState.Error(it.message?:"Registration failed") }
        }
    }
}
sealed class RegisterNavEvent { data object ToHome:RegisterNavEvent(); data object ToLandlordDashboard:RegisterNavEvent() }

package com.bac.homelink.presentation.reservation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.ui.UiState
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.data.dao.UserDao
import com.bac.homelink.domain.model.ReservationModel
import com.bac.homelink.domain.usecase.reservation.CreateReservationUseCase
import com.bac.homelink.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDetails(
    val fullName: String,
    val studentId: String,
    val phone: String,
    val email: String
)

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val createReservationUseCase: CreateReservationUseCase,
    private val session: SessionManager,
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _reservationState = MutableStateFlow<UiState<String>?>(null)
    val reservationState: StateFlow<UiState<String>?> = _reservationState.asStateFlow()

    private val _navEvent = MutableSharedFlow<String>()
    val navEvent: SharedFlow<String> = _navEvent.asSharedFlow()

    private val _studentDetails = MutableStateFlow<StudentDetails?>(null)
    val studentDetails: StateFlow<StudentDetails?> = _studentDetails.asStateFlow()

    init {
        loadStudentDetails()
    }

    private fun loadStudentDetails() {
        viewModelScope.launch {
            val user = userDao.getUserByIdSync(session.getUserId())
            if (user != null) {
                _studentDetails.value = StudentDetails(
                    fullName  = user.fullName,
                    studentId = user.studentId,
                    phone     = user.phone,
                    email     = user.email
                )
            }
        }
    }

    fun submitReservation(
        listingId: Int,
        listingTitle: String,
        listingLocation: String,
        depositAmount: Int,
        moveInDate: String,
        paymentMethod: String,
        cardNumber: String = "",
        cardExpiry: String = "",
        cardCvv: String = ""
    ) {
        val student = _studentDetails.value ?: return
        val cardDigits = cardNumber.filter { it.isDigit() }
        val cvvDigits = cardCvv.filter { it.isDigit() }

        if (cardDigits.length != 16) {
            _reservationState.value = UiState.Error("Card number must be 16 numbers")
            return
        }
        if (cardExpiry.isBlank()) {
            _reservationState.value = UiState.Error("Card expiry is required")
            return
        }
        if (cvvDigits.length != 3) {
            _reservationState.value = UiState.Error("CVV must be 3 numbers")
            return
        }

        viewModelScope.launch {
            _reservationState.value = UiState.Loading
            delay(700)
            val reservation = ReservationModel(
                listingId       = listingId,
                userId          = session.getUserId(),
                referenceNumber = "",
                depositAmountPaid = depositAmount,
                paymentMethod   = paymentMethod,
                studentName     = student.fullName,
                studentId       = student.studentId,
                listingTitle    = listingTitle,
                listingLocation = listingLocation,
                moveInDate      = moveInDate
            )
            createReservationUseCase(reservation)
                .onSuccess { ref ->
                    _reservationState.value = UiState.Success(ref)
                    NotificationHelper.sendReservationConfirmationNotification(context, ref, listingTitle)
                    _navEvent.emit(ref)
                }
                .onFailure {
                    _reservationState.value = UiState.Error(it.message ?: "Payment failed")
                }
        }
    }
}

package com.bac.homelink.domain.usecase.reservation
import com.bac.homelink.domain.model.ReservationModel
import com.bac.homelink.domain.repository.*; import javax.inject.Inject

class CreateReservationUseCase @Inject constructor(
    private val reservationRepo:ReservationRepository, private val listingRepo:ListingRepository) {
    suspend operator fun invoke(reservation:ReservationModel):Result<String> = runCatching {
        require(reservation.studentName.isNotBlank()){"Student name required"}
        require(reservation.studentId.isNotBlank()){"Student ID required"}
        val ref = reservationRepo.createReservation(reservation).getOrThrow()
        listingRepo.updateListingStatus(reservation.listingId,"RESERVED",reservation.userId)
        ref
    }
}

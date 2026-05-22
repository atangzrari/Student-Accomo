package com.bac.homelink.domain.repository
import com.bac.homelink.domain.model.ReservationModel; import kotlinx.coroutines.flow.Flow
interface ReservationRepository {
    fun getReservationsByUser(userId:Int):Flow<List<ReservationModel>>
    suspend fun createReservation(reservation:ReservationModel):Result<String>
    suspend fun cancelReservation(reservationId:Int)
}

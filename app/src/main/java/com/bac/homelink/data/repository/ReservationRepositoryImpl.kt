package com.bac.homelink.data.repository
import com.bac.homelink.data.dao.ListingDao
import com.bac.homelink.data.dao.ReservationDao
import com.bac.homelink.domain.model.ListingStatus
import com.bac.homelink.domain.model.ReservationModel
import com.bac.homelink.domain.repository.ReservationRepository
import com.bac.homelink.utils.ReferenceGenerator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject; import javax.inject.Singleton

@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val dao:ReservationDao,
    private val listingDao: ListingDao,
    private val firestore:FirebaseFirestore
):ReservationRepository {
    override fun getReservationsByUser(userId:Int):Flow<List<ReservationModel>> = callbackFlow {
        val listener = firestore.collection("reservations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val reservations = snapshot?.documents
                    ?.mapNotNull { it.data?.toReservationEntity()?.toDomain() }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(reservations)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createReservation(reservation:ReservationModel):Result<String> = runCatching {
        val ref = ReferenceGenerator.generate()
        val id = stableFirestoreIntId(ref)
        val confirmed = reservation.copy(id = id, referenceNumber=ref)
        val entity = confirmed.toEntity()
        val listingRef = firestore.collection("listings").document(reservation.listingId.toString())
        val reservationRef = firestore.collection("reservations").document(ref)

        firestore.runTransaction { transaction ->
            val listing = transaction.get(listingRef)
            if (!listing.exists()) {
                throw IllegalStateException("This house is no longer available")
            }
            val status = listing.getString("status") ?: ListingStatus.AVAILABLE.name
            if (status != ListingStatus.AVAILABLE.name) {
                throw IllegalStateException("This house has already been reserved")
            }

            transaction.set(reservationRef, confirmed.toFirestoreMap(), SetOptions.merge())
            transaction.set(
                listingRef,
                mapOf(
                    "status" to ListingStatus.RESERVED.name,
                    "reservedByUserId" to reservation.userId,
                    "reservationReference" to ref,
                    "reservedStudentName" to reservation.studentName,
                    "reservedStudentId" to reservation.studentId,
                    "reservedStudentEmail" to "",
                    "reservedMoveInDate" to reservation.moveInDate
                ),
                SetOptions.merge()
            )
        }.await()

        dao.insertReservation(entity)
        listingDao.updateListingStatus(reservation.listingId, ListingStatus.RESERVED.name, reservation.userId)
        ref
    }

    override suspend fun cancelReservation(reservationId:Int) {
        firestore.collection("reservations")
            .whereEqualTo("id", reservationId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.reference
            ?.set(mapOf("status" to "CANCELLED"), SetOptions.merge())
            ?.await()
    }
}

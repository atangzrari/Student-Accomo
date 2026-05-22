package com.bac.homelink.data.repository

import com.bac.homelink.data.dao.ListingDao
import com.bac.homelink.domain.model.FilterParams
import com.bac.homelink.domain.model.ListingModel
import com.bac.homelink.domain.model.ListingStatus
import com.bac.homelink.domain.repository.ListingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingRepositoryImpl @Inject constructor(
    private val dao: ListingDao,
    private val firestore: FirebaseFirestore
) : ListingRepository {

    override fun getAvailableListings(): Flow<List<ListingModel>> = listingFlow {
        firestore.collection("listings")
    }

    override fun getListingById(id: Int): Flow<ListingModel?> = callbackFlow {
        val listener = firestore.collection("listings").document(id.toString())
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.data?.toListingEntity()?.toDomain())
            }
        awaitClose { listener.remove() }
    }

    override fun getListingsByLandlord(email: String): Flow<List<ListingModel>> = listingFlow {
        firestore.collection("listings")
            .whereEqualTo("landlordEmail", email)
    }

    override fun filterListings(params: FilterParams): Flow<List<ListingModel>> = callbackFlow {
        val listener = firestore.collection("listings")
            .addSnapshotListener { snapshot, _ ->
                val listings = snapshot?.documents
                    ?.mapNotNull { it.data?.toListingEntity()?.toDomain() }
                    ?.filter { listing ->
                        (params.maxPrice == 0 || listing.pricePerMonth <= params.maxPrice) &&
                            (params.location.isBlank() || listing.location.contains(params.location, ignoreCase = true)) &&
                            (params.availabilityDate.isBlank() || listing.availabilityDate <= params.availabilityDate) &&
                            (params.accommodationType.isBlank() || listing.accommodationType.contains(params.accommodationType, ignoreCase = true)) &&
                            (params.sharingArrangement.isBlank() || listing.sharingArrangement.contains(params.sharingArrangement, ignoreCase = true))
                    }
                    ?.sortedBy { it.pricePerMonth }
                    ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    override fun getFavouriteListings(userId: Int): Flow<List<ListingModel>> = callbackFlow {
        val userIdText = userId.toString()
        val listener = firestore.collection("listings")
            .addSnapshotListener { snapshot, _ ->
                val listings = snapshot?.documents
                    ?.mapNotNull { it.data?.toListingEntity() }
                    ?.filter { listing ->
                        listing.favouritedByUserIds.split(",").map { it.trim() }.contains(userIdText)
                    }
                    ?.map { it.toDomain().copy(isFavourite = true) }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(listings)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addListing(listing: ListingModel): Long {
        val id = if (listing.id > 0) listing.id else stableFirestoreIntId(
            "${listing.landlordEmail}-${listing.title}-${listing.createdAt}"
        )
        val listingWithId = listing.copy(id = id)
        val entity = listingWithId.toEntity()
        if (dao.insertListing(entity) <= 0) dao.updateListing(entity)
        firestore.collection("listings")
            .document(id.toString())
            .set(listingWithId.toFirestoreMap(), SetOptions.merge())
            .await()
        return id.toLong()
    }

    override suspend fun updateListing(listing: ListingModel) {
        dao.updateListing(listing.toEntity())
        val existingFavourites = firestore.collection("listings")
            .document(listing.id.toString())
            .get()
            .await()
            .getString("favouritedByUserIds")
            ?: ""
        firestore.collection("listings")
            .document(listing.id.toString())
            .set(listing.toFirestoreMap(existingFavourites), SetOptions.merge())
            .await()
    }

    override suspend fun deleteListing(listing: ListingModel) {
        dao.deleteListing(listing.toEntity())
        firestore.collection("listings").document(listing.id.toString()).delete().await()
    }

    override suspend fun updateListingStatus(listingId: Int, status: String, userId: Int) {
        dao.updateListingStatus(listingId, status, userId)
        firestore.collection("listings")
            .document(listingId.toString())
            .set(mapOf("status" to status, "reservedByUserId" to userId), SetOptions.merge())
            .await()
    }

    override suspend fun toggleFavourite(listingId: Int, userId: Int) {
        dao.toggleFavourite(listingId, userId)
        val doc = firestore.collection("listings").document(listingId.toString())
        val snapshot = doc.get().await()
        val current = snapshot.getString("favouritedByUserIds").orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
        val id = userId.toString()
        if (current.contains(id)) current.remove(id) else current.add(id)
        doc.set(mapOf("favouritedByUserIds" to current.joinToString(",")), SetOptions.merge()).await()
    }

    override suspend fun getMatchingListings(maxPrice: Int, location: String): List<ListingModel> =
        firestore.collection("listings")
            .whereEqualTo("status", ListingStatus.AVAILABLE.name)
            .get()
            .await()
            .documents
            .mapNotNull { it.data?.toListingEntity()?.toDomain() }
            .filter { listing ->
                (maxPrice == 0 || listing.pricePerMonth <= maxPrice) &&
                    (location.isBlank() || listing.location.contains(location, ignoreCase = true))
            }

    private fun listingFlow(queryBuilder: () -> Query): Flow<List<ListingModel>> = callbackFlow {
        val listener = queryBuilder().addSnapshotListener { snapshot, _ ->
            val listings = snapshot?.documents
                ?.mapNotNull { it.data?.toListingEntity()?.toDomain() }
                ?.sortedByDescending { it.createdAt }
                ?: emptyList()
            trySend(listings)
        }
        awaitClose { listener.remove() }
    }
}

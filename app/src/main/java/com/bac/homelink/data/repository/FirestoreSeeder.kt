package com.bac.homelink.data.repository

import com.bac.homelink.data.database.SeedData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun seedIfEmpty() {
        seedUsers()
        seedListings()
    }

    private suspend fun seedUsers() {
        val batch = firestore.batch()
        SeedData.generateUsers().forEach { user ->
            val id = stableFirestoreIntId(user.email)
            val seededUser = user.copy(id = id)
            val doc = firestore.collection("users").document(id.toString())
            batch.set(doc, seededUser.toDomain().toFirestoreMap(seededUser.passwordHash), SetOptions.merge())
        }
        batch.commit().await()
    }

    private suspend fun seedListings() {
        val batch = firestore.batch()
        SeedData.generateListings().forEach { listing ->
            val id = stableFirestoreIntId("${listing.landlordEmail}-${listing.title}")
            val seededListing = listing.copy(id = id)
            val doc = firestore.collection("listings").document(id.toString())
            batch.set(doc, seededListing.toDomain().toFirestoreMap(), SetOptions.merge())
        }
        batch.commit().await()
    }
}

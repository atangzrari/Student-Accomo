package com.bac.homelink

import android.app.Application
import android.util.Log
import com.bac.homelink.data.repository.FirestoreSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HomeLinkApp : Application() {
    @Inject lateinit var firestoreSeeder: FirestoreSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            runCatching { firestoreSeeder.seedIfEmpty() }
                .onFailure { Log.w("HomeLinkApp", "Firestore seed failed", it) }
        }
    }
}

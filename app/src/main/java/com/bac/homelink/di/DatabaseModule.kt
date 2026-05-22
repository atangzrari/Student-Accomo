package com.bac.homelink.di
import android.content.Context
import com.bac.homelink.data.dao.*
import com.bac.homelink.data.database.AppDatabase
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent; import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx:Context):AppDatabase =
        AppDatabase.getInstance(ctx)
    @Provides fun provideUserDao(db:AppDatabase):UserDao = db.userDao()
    @Provides fun provideListingDao(db:AppDatabase):ListingDao = db.listingDao()
    @Provides fun provideReservationDao(db:AppDatabase):ReservationDao = db.reservationDao()
}

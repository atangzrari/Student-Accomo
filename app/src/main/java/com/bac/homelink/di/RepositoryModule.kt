package com.bac.homelink.di
import com.bac.homelink.data.repository.*
import com.bac.homelink.domain.repository.*
import dagger.Binds; import dagger.Module; import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent; import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindListingRepo(impl:ListingRepositoryImpl):ListingRepository
    @Binds @Singleton abstract fun bindAuthRepo(impl:AuthRepositoryImpl):AuthRepository
    @Binds @Singleton abstract fun bindReservationRepo(impl:ReservationRepositoryImpl):ReservationRepository
    @Binds @Singleton abstract fun bindChatRepo(impl:ChatRepositoryImpl):ChatRepository
}

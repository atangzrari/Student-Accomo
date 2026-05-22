package com.bac.homelink.data.dao
import androidx.room.*
import com.bac.homelink.data.entities.Reservation
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertReservation(r:Reservation):Long
    @Update suspend fun updateReservation(r:Reservation)
    @Query("SELECT * FROM reservations WHERE userId=:userId ORDER BY createdAt DESC")
    fun getReservationsByUser(userId:Int):Flow<List<Reservation>>
    @Query("SELECT * FROM reservations WHERE referenceNumber=:ref LIMIT 1")
    suspend fun getReservationByRef(ref:String):Reservation?
}

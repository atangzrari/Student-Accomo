package com.bac.homelink.data.dao
import androidx.room.*
import com.bac.homelink.data.entities.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertListing(listing:Listing):Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertListings(listings:List<Listing>)
    @Update suspend fun updateListing(listing:Listing)
    @Delete suspend fun deleteListing(listing:Listing)

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAvailableListings():Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE id=:id LIMIT 1")
    fun getListingById(id:Int):Flow<Listing?>

    @Query("SELECT * FROM listings WHERE id=:id LIMIT 1")
    suspend fun getListingByIdSync(id:Int):Listing?

    @Query("SELECT * FROM listings WHERE landlordEmail=:email ORDER BY createdAt DESC")
    fun getListingsByLandlordEmail(email:String):Flow<List<Listing>>

    @Query("""SELECT * FROM listings WHERE
        favouritedByUserIds LIKE '%,' || :userId || ',%' OR
        favouritedByUserIds LIKE :userId || ',%' OR
        favouritedByUserIds LIKE '%,' || :userId OR
        favouritedByUserIds = CAST(:userId AS TEXT)""")
    fun getFavouriteListings(userId:Int):Flow<List<Listing>>

    @Query("""SELECT * FROM listings
        WHERE (:maxPrice=0 OR pricePerMonth<=:maxPrice)
        AND (:location='' OR LOWER(location) LIKE '%' || LOWER(:location) || '%')
        AND (:availDate='' OR availabilityDate<=:availDate)
        AND (:accType='' OR LOWER(accommodationType) LIKE '%' || LOWER(:accType) || '%')
        AND (:sharing='' OR LOWER(sharingArrangement) LIKE '%' || LOWER(:sharing) || '%')
        ORDER BY pricePerMonth ASC""")
    fun filterListings(maxPrice:Int, location:String, availDate:String, accType:String="", sharing:String=""):Flow<List<Listing>>

    @Query("UPDATE listings SET status=:status, reservedByUserId=:userId WHERE id=:listingId")
    suspend fun updateListingStatus(listingId:Int, status:String, userId:Int)

    @Query("SELECT * FROM listings WHERE id=:id LIMIT 1")
    suspend fun getForFavToggle(id:Int):Listing?

    @Transaction
    suspend fun toggleFavourite(listingId:Int, userId:Int) {
        val listing = getForFavToggle(listingId) ?: return
        val ids = listing.favouritedByUserIds.split(",").map{it.trim()}.filter{it.isNotEmpty()}.toMutableList()
        val u = userId.toString()
        if(ids.contains(u)) ids.remove(u) else ids.add(u)
        updateListing(listing.copy(favouritedByUserIds=ids.joinToString(",")))
    }

    @Query("SELECT COUNT(*) FROM listings") suspend fun getListingCount():Int

    @Query("""SELECT * FROM listings WHERE status='AVAILABLE'
        AND (:maxPrice=0 OR pricePerMonth<=:maxPrice)
        AND (:location='' OR LOWER(location) LIKE '%' || LOWER(:location) || '%')""")
    suspend fun getMatchingListings(maxPrice:Int, location:String):List<Listing>
}

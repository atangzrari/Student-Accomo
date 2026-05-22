package com.bac.homelink.data.dao
import androidx.room.*
import com.bac.homelink.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertUser(user:User):Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertUsers(users:List<User>)
    @Update suspend fun updateUser(user:User)
    @Query("SELECT * FROM users WHERE email=:email AND passwordHash=:passwordHash LIMIT 1")
    suspend fun login(email:String, passwordHash:String):User?
    @Query("SELECT * FROM users WHERE email=:email LIMIT 1") suspend fun getUserByEmail(email:String):User?
    @Query("SELECT * FROM users WHERE email=:email LIMIT 1") suspend fun getUserByEmailSync(email:String):User?
    @Query("SELECT * FROM users WHERE id=:id LIMIT 1") suspend fun getUserByIdSync(id:Int):User?
    @Query("SELECT * FROM users WHERE role='STUDENT' ORDER BY fullName ASC") fun getAllStudents():Flow<List<User>>
    @Query("SELECT COUNT(*) FROM users") suspend fun getUserCount():Int
}

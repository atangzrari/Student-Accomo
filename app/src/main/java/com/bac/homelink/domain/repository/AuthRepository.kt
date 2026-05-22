package com.bac.homelink.domain.repository
import com.bac.homelink.domain.model.UserModel; import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    suspend fun login(email:String, password:String):Result<UserModel>
    suspend fun register(user:UserModel, password:String):Result<UserModel>
    fun getCurrentUser():Flow<UserModel?>
    suspend fun logout()
    suspend fun updateUserProfile(user:UserModel)
}

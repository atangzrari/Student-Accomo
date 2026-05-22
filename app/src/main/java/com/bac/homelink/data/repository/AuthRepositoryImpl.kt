package com.bac.homelink.data.repository
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.data.dao.UserDao
import com.bac.homelink.domain.model.UserModel
import com.bac.homelink.domain.repository.AuthRepository
import com.bac.homelink.utils.HashUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject; import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dao:UserDao,
    private val session:SessionManager,
    private val firestore:FirebaseFirestore
):AuthRepository {
    override suspend fun login(email:String,password:String):Result<UserModel> {
        val passwordHash = HashUtils.sha256(password)
        val remoteUser = firestore.collection("users")
            .whereEqualTo("email", email.trim())
            .whereEqualTo("passwordHash", passwordHash)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.data
            ?.toUserEntity()

        val user = remoteUser ?: dao.login(email, passwordHash)
            ?: return Result.failure(Exception("Invalid email or password"))

        upsertLocalUser(user)
        session.saveSession(user.id,user.fullName,user.email,user.role)
        return Result.success(user.toDomain())
    }
    override suspend fun register(user:UserModel,password:String):Result<UserModel> {
        val email = user.email.trim()
        if(dao.getUserByEmail(email)!=null) return Result.failure(Exception("Email already registered"))

        val id = if (user.id > 0) user.id else stableFirestoreIntId(email)
        val registeredUser = user.copy(id = id, email = email)
        val passwordHash = HashUtils.sha256(password)
        val entity = registeredUser.toEntity(passwordHash)
        upsertLocalUser(entity)

        session.saveSession(id,registeredUser.fullName,registeredUser.email,registeredUser.role.name)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore.collection("users")
                    .document(id.toString())
                    .set(registeredUser.toFirestoreMap(passwordHash), SetOptions.merge())
                    .await()
            }
        }
        return Result.success(registeredUser)
    }
    override fun getCurrentUser():Flow<UserModel?> = callbackFlow {
        val id = session.getUserId()
        if(id==-1){
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(id.toString())
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.data?.toUserEntity()?.toDomain()
                trySend(user)
            }
        awaitClose { listener.remove() }
    }
    override suspend fun logout() = session.logout()
    override suspend fun updateUserProfile(user:UserModel) {
        val e = dao.getUserByIdSync(user.id) ?: return
        val updated = e.copy(fullName=user.fullName,phone=user.phone,profileImageUrl=user.profileImageUrl)
        dao.updateUser(updated)
        firestore.collection("users")
            .document(user.id.toString())
            .set(updated.toDomain().toFirestoreMap(updated.passwordHash), SetOptions.merge())
            .await()
    }

    private suspend fun upsertLocalUser(user: com.bac.homelink.data.entities.User) {
        if (dao.insertUser(user) <= 0) dao.updateUser(user)
    }
}

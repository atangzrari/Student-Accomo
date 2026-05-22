package com.bac.homelink.domain.usecase.auth
import com.bac.homelink.domain.model.UserModel
import com.bac.homelink.domain.repository.AuthRepository; import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo:AuthRepository) {
    suspend operator fun invoke(email:String,password:String):Result<UserModel> {
        if(email.isBlank()) return Result.failure(IllegalArgumentException("Email required"))
        if(password.length<4) return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        return repo.login(email.trim(),password)
    }
}

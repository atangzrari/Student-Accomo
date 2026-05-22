package com.bac.homelink.domain.usecase.auth
import android.util.Patterns
import com.bac.homelink.domain.model.UserModel
import com.bac.homelink.domain.repository.AuthRepository; import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repo:AuthRepository) {
    suspend operator fun invoke(user:UserModel,password:String,confirmPassword:String):Result<UserModel> {
        if(user.fullName.isBlank()) return Result.failure(IllegalArgumentException("Name required"))
        if(!Patterns.EMAIL_ADDRESS.matcher(user.email).matches()) return Result.failure(IllegalArgumentException("Valid email required"))
        if(password.length<4) return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
        if(password!=confirmPassword) return Result.failure(IllegalArgumentException("Passwords do not match"))
        return repo.register(user,password)
    }
}

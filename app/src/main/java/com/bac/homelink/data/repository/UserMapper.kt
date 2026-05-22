package com.bac.homelink.data.repository
import com.bac.homelink.data.entities.User
import com.bac.homelink.domain.model.*

fun User.toDomain():UserModel = UserModel(id=id,studentId=studentId,fullName=fullName,email=email,
    phone=phone,role=if(role=="PROVIDER")UserRole.PROVIDER else UserRole.STUDENT,
    institution=institution,profileImageUrl=profileImageUrl,savedFilterPrice=savedFilterPrice,
    savedFilterLocation=savedFilterLocation,savedFilterDate=savedFilterDate)

fun UserModel.toEntity(passwordHash:String=""):User = User(id=id,studentId=studentId,
    fullName=fullName,email=email,phone=phone,passwordHash=passwordHash,role=role.name,institution=institution,
    profileImageUrl=profileImageUrl,savedFilterPrice=savedFilterPrice,
    savedFilterLocation=savedFilterLocation,savedFilterDate=savedFilterDate)

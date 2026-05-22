package com.bac.homelink.utils
import java.security.MessageDigest
object HashUtils {
    fun sha256(input:String):String = MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray()).joinToString(""){"%02x".format(it)}
}
object ReferenceGenerator {
    fun generate():String = "ACF-${System.currentTimeMillis()%100000}-${(1000..9999).random()}"
}

package com.bac.homelink.models

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "STUDENT",   // "STUDENT" or "PROVIDER"
    val message: String = "",
    val timestamp: Long = 0L,
    val listingId: Int = 0,
    val isRead: Boolean = false
)

/** A chat conversation summary shown in the chat list screen */
data class ChatConversation(
    val chatRoomId: String,
    val listingId: Int,
    val listingTitle: String,
    val otherPartyName: String,
    val otherPartyEmail: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0
)

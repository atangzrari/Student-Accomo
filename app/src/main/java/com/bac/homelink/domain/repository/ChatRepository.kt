package com.bac.homelink.domain.repository

import com.bac.homelink.models.ChatConversation
import com.bac.homelink.models.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(chatRoomId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatRoomId: String, message: ChatMessage): Result<Unit>
    fun getConversations(userEmail: String, role: String): Flow<List<ChatConversation>>

    companion object {
        /**
         * Chat room ID is deterministic: based on listingId + studentEmail
         * This ensures both student AND landlord see the SAME room
         */
        fun buildChatRoomId(listingId: Int, studentEmail: String): String {
            val cleanEmail = studentEmail.replace(".", "_").replace("@", "_at_")
            return "listing_${listingId}_${cleanEmail}"
        }
    }
}

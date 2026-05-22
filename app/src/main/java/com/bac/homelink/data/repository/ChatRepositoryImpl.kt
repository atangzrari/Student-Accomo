package com.bac.homelink.data.repository

import com.bac.homelink.domain.repository.ChatRepository
import com.bac.homelink.models.ChatConversation
import com.bac.homelink.models.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getMessages(chatRoomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore
            .collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    ChatMessage(
                        id          = doc.id,
                        senderId    = doc.getString("senderId") ?: "",
                        senderName  = doc.getString("senderName") ?: "",
                        senderRole  = doc.getString("senderRole") ?: "STUDENT",
                        message     = doc.getString("message") ?: "",
                        timestamp   = doc.getLong("timestamp") ?: 0L,
                        listingId   = doc.getLong("listingId")?.toInt() ?: 0,
                        isRead      = doc.getBoolean("isRead") ?: false
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(chatRoomId: String, message: ChatMessage): Result<Unit> {
        return runCatching {
            val listingSnapshot = firestore.collection("listings")
                .document(message.listingId.toString())
                .get()
                .await()
            val listingTitle = listingSnapshot.getString("title") ?: "Listing"
            val landlordName = listingSnapshot.getString("landlordName").orEmpty()
            val landlordEmail = listingSnapshot.getString("landlordEmail").orEmpty()
            val studentEmail = studentEmailFromRoomId(chatRoomId)
            val existingRoom = firestore.collection("chats").document(chatRoomId).get().await()
            val existingStudentName = existingRoom.getString("studentName").orEmpty()
            val studentName = when {
                message.senderRole == "STUDENT" && message.senderName.isNotBlank() -> message.senderName
                existingStudentName.isNotBlank() -> existingStudentName
                else -> ""
            }

            val data = mapOf(
                "senderId"   to message.senderId,
                "senderName" to message.senderName,
                "senderRole" to message.senderRole,
                "message"    to message.message,
                "timestamp"  to message.timestamp,
                "listingId"  to message.listingId,
                "isRead"     to false
            )
            // Write message to shared room
            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(data)
                .await()

            // Update metadata on the room doc so both parties can list conversations
            firestore.collection("chats")
                .document(chatRoomId)
                .set(mapOf(
                    "lastMessage"   to message.message,
                    "lastTimestamp" to message.timestamp,
                    "listingId"     to message.listingId,
                    "listingTitle"  to listingTitle,
                    "chatRoomId"    to chatRoomId,
                    "studentName"   to studentName,
                    "studentEmail"  to studentEmail,
                    "landlordName"  to landlordName,
                    "landlordEmail" to landlordEmail
                ), com.google.firebase.firestore.SetOptions.merge())
                .await()

            Unit
        }
    }

    override fun getConversations(userEmail: String, role: String): Flow<List<ChatConversation>> =
        callbackFlow {
            // Listen to all chat rooms that contain this user's email in the ID
            val cleanEmail = userEmail.replace(".", "_").replace("@", "_at_")
            val listener = firestore.collection("chats")
                .addSnapshotListener { snapshot, _ ->
                    val conversations = snapshot?.documents
                        ?.filter { doc ->
                            val roomId = doc.id
                            if (role == "STUDENT") {
                                roomId.contains(cleanEmail)
                            } else {
                                doc.getString("landlordEmail") == userEmail
                            }
                        }
                        ?.map { doc ->
                            val roomId = doc.id
                            val isProvider = role == "PROVIDER"
                            val otherPartyName = if (isProvider) {
                                doc.getString("studentName").takeUnless { it.isNullOrBlank() }
                                    ?: "Chat with Student"
                            } else {
                                doc.getString("landlordName").takeUnless { it.isNullOrBlank() }
                                    ?: "Chat with Landlord"
                            }
                            val otherPartyEmail = if (isProvider) {
                                doc.getString("studentEmail") ?: ""
                            } else {
                                doc.getString("landlordEmail") ?: ""
                            }
                            ChatConversation(
                                chatRoomId      = roomId,
                                listingId       = doc.getLong("listingId")?.toInt() ?: 0,
                                listingTitle    = doc.getString("listingTitle") ?: "Listing",
                                otherPartyName  = otherPartyName,
                                otherPartyEmail = otherPartyEmail,
                                lastMessage     = doc.getString("lastMessage") ?: "",
                                lastTimestamp   = doc.getLong("lastTimestamp") ?: 0L
                            )
                        } ?: emptyList()
                    trySend(conversations.sortedByDescending { it.lastTimestamp })
                }
            awaitClose { listener.remove() }
        }

    private fun studentEmailFromRoomId(chatRoomId: String): String {
        val encoded = chatRoomId.replaceFirst(Regex("^listing_\\d+_"), "")
        return encoded.replace("_at_", "@").replace("_", ".")
    }
}

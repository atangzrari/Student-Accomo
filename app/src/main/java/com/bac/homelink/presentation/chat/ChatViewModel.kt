package com.bac.homelink.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bac.homelink.core.utils.SessionManager
import com.bac.homelink.domain.repository.ChatRepository
import com.bac.homelink.models.ChatConversation
import com.bac.homelink.models.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _chatRoomId = MutableStateFlow("")
    private val _listingId  = MutableStateFlow(0)

    /** Must call before observing messages */
    fun initRoom(listingId: Int, studentEmail: String) {
        _listingId.value = listingId
        _chatRoomId.value = ChatRepository.buildChatRoomId(listingId, studentEmail)
    }

    /** If current user IS the student, studentEmail = their email.
     *  If current user is LANDLORD opening a student convo, pass the student email. */
    fun initRoomFromStudent(listingId: Int) {
        val studentEmail = session.getUserEmail()   // student opens from listing
        initRoom(listingId, studentEmail)
    }

    fun initRoomFromConversation(chatRoomId: String, listingId: Int) {
        _chatRoomId.value = chatRoomId
        _listingId.value  = listingId
    }

    val messages: StateFlow<List<ChatMessage>>
        get() = chatRepository.getMessages(_chatRoomId.value)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val conversations: StateFlow<List<ChatConversation>>
        get() = chatRepository.getConversations(session.getUserEmail(), session.getUserRole())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank() || _chatRoomId.value.isEmpty()) return
        viewModelScope.launch {
            val msg = ChatMessage(
                senderId   = session.getUserId().toString(),
                senderName = session.getUserName(),
                senderRole = session.getUserRole(),
                message    = text,
                timestamp  = System.currentTimeMillis(),
                listingId  = _listingId.value
            )
            chatRepository.sendMessage(_chatRoomId.value, msg)
        }
    }

    val currentUserId: Int   get() = session.getUserId()
    val currentUserRole: String get() = session.getUserRole()
    val currentUserEmail: String get() = session.getUserEmail()
}

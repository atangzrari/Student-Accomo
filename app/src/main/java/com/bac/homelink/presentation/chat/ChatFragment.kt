package com.bac.homelink.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.homelink.adapters.ChatAdapter
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: ActivityChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listingId    = arguments?.getInt("listing_id", -1) ?: -1
        val chatRoomId   = arguments?.getString("chat_room_id", "") ?: ""
        val landlordName = arguments?.getString("landlord_name", "Landlord") ?: "Landlord"

        // If a specific room ID passed (from conversation list), use it directly
        if (chatRoomId.isNotEmpty()) {
            viewModel.initRoomFromConversation(chatRoomId, listingId)
        } else {
            // Student opening chat from listing detail — uses their own email
            viewModel.initRoomFromStudent(listingId)
        }

        adapter = ChatAdapter(emptyList(), viewModel.currentUserId)

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        viewModel.messages.collectWithLifecycle(viewLifecycleOwner) { messages ->
            adapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.smoothScrollToPosition(messages.size - 1)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etMessage.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

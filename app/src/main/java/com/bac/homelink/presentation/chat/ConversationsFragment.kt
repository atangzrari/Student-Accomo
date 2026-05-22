package com.bac.homelink.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bac.homelink.R
import com.bac.homelink.core.extensions.collectWithLifecycle
import com.bac.homelink.models.ChatConversation
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ConversationsFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_conversations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_conversations)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        val tvTitle = view.findViewById<TextView>(R.id.tv_chat_title)

        tvTitle.text = if (viewModel.currentUserRole == "PROVIDER")
            "Student Enquiries" else "My Conversations"

        val adapter = ConversationAdapter { conversation ->
            val bundle = Bundle().apply {
                putString("chat_room_id", conversation.chatRoomId)
                putInt("listing_id", conversation.listingId)
                putString("landlord_name", conversation.otherPartyName)
                putString("landlord_email", conversation.otherPartyEmail)
            }
            findNavController().navigate(R.id.action_conversations_to_chat, bundle)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewModel.conversations.collectWithLifecycle(viewLifecycleOwner) { conversations ->
            adapter.submitList(conversations)
            tvEmpty.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
            rv.visibility = if (conversations.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}

class ConversationAdapter(
    private val onClick: (ChatConversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.VH>() {

    private var items = listOf<ChatConversation>()

    fun submitList(newList: List<ChatConversation>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(conv: ChatConversation) {
            itemView.findViewById<TextView>(R.id.tv_other_party).text = conv.otherPartyName
            itemView.findViewById<TextView>(R.id.tv_listing_title).text = conv.listingTitle
            itemView.findViewById<TextView>(R.id.tv_last_message).text = conv.lastMessage
            itemView.findViewById<TextView>(R.id.tv_time).text =
                if (conv.lastTimestamp > 0)
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conv.lastTimestamp))
                else ""
            itemView.setOnClickListener { onClick(conv) }
        }
    }
}

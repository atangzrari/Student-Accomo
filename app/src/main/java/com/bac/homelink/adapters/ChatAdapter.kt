package com.bac.homelink.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bac.homelink.R
import com.bac.homelink.databinding.ItemChatMessageBinding
import com.bac.homelink.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var messages: List<ChatMessage>,
    private val currentUserId: Int
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: ChatMessage) {
            val isMine = msg.senderId == currentUserId.toString()
            binding.tvMessage.text = msg.message
            binding.tvSender.text  = if (isMine) "You" else msg.senderName
            binding.tvTime.text    = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(msg.timestamp))

            if (isMine) {
                binding.root.gravity = Gravity.END
                binding.tvMessage.setBackgroundResource(R.drawable.bg_chat_sent)
                binding.tvMessage.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                binding.root.gravity = Gravity.START
                binding.tvMessage.setBackgroundResource(R.drawable.bg_chat_received)
                binding.tvMessage.setTextColor(
                    binding.root.context.getColor(R.color.text_primary)
                )
            }
        }
    }
}

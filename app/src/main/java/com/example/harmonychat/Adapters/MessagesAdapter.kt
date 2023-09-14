package com.example.harmonychat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.harmonychat.Models.Message
import com.example.harmonychat.R
import com.example.harmonychat.databinding.ItemReceiveBinding
import com.example.harmonychat.databinding.ItemSentBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.ArrayList

class MessagesAdapter(private val context: Context, private val messages: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private val ITEM_RECEIVE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) {
            if (message.message == "photo") {
                holder.binding.chatImage.visibility = View.VISIBLE
                holder.binding.message.visibility = View.GONE
                Glide.with(context).load(message.imageUrl).into(holder.binding.chatImage)
            }
            holder.binding.message.text = message.message
        } else if (holder is ReceiverViewHolder) {
            if (message.message == "photo") {
                holder.binding.chatImage.visibility = View.VISIBLE
                holder.binding.message.visibility = View.GONE
                Glide.with(context).load(message.imageUrl).into(holder.binding.chatImage)
            }
            holder.binding.message.text = message.message
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSentBinding.bind(itemView)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemReceiveBinding.bind(itemView)
    }
}
package com.example.harmonychat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.harmonychat.ChatActivity
import com.example.harmonychat.Models.User
import com.example.harmonychat.R
import com.example.harmonychat.databinding.RowConversationBinding
import java.util.ArrayList

class UsersAdapter(private val context: Context, private val users: ArrayList<User>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.binding.username.text = user.name
        Glide.with(context)
            .load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profile)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("profileImage", user.profileImage)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowConversationBinding = RowConversationBinding.bind(itemView)
    }
}

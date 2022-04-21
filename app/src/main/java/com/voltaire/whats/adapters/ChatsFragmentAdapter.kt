package com.voltaire.whats.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.voltaire.whats.ChatActivity
import com.voltaire.whats.R
import com.voltaire.whats.model.User

class ChatsFragmentAdapter (
    private var contactList : ArrayList<User>) :
    RecyclerView.Adapter<ChatsFragmentAdapter.ChatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_contact_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(contactList[position])

        holder.itemView.setOnClickListener {
            val intent = Intent (holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("User", contactList[position])
            holder.itemView.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    inner class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var contactName: TextView = itemView.findViewById(R.id.contactName)
        var contactPhoto: ImageView = itemView.findViewById(R.id.contactPhoto)

        fun bind(user: User) {
            contactName.text = user.name

            Picasso.get()
                .load(user.urlPhoto)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(contactPhoto)
        }
    }
}
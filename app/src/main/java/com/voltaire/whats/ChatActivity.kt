package com.voltaire.whats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.voltaire.whats.adapters.ChatsFragmentAdapter
import com.voltaire.whats.databinding.ActivityChatBinding
import com.voltaire.whats.model.Message
import com.voltaire.whats.model.User

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var recyclerView: RecyclerView
    private val database = FirebaseAuth.getInstance()
    private lateinit var user: User
    private lateinit var me: User
    private lateinit var documentChange: DocumentChange


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra("User")!!
        binding.toolbarMsg.title = user.name

        adapter = MessagesAdapter()
        recyclerView = binding.rvMsgs
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        FirebaseFirestore.getInstance().collection("users")
            .document(database.currentUser?.email.toString())
            .get()
            .addOnSuccessListener { it: DocumentSnapshot? ->
                me = it?.toObject(User::class.java)!!

                fetchMessages()
            }

        binding.toolbarMsg.setNavigationOnClickListener {
            finish()
        }

        binding.btnSendMsg.setOnClickListener {
            sendMessage()
        }
    }

    private fun fetchMessages() {
        if (me != null) {
            val fromEmail = me.email
            val toEmail = user.email

            FirebaseFirestore.getInstance().collection("conversations")
                .document(fromEmail)
                .collection(toEmail)
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    if (value?.documentChanges != null) {
                        value.documentChanges.forEach {
                            val newMessage = it.document.toObject(Message::class.java)
                            adapter.listMessages.add(newMessage)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }

    private fun sendMessage() {

        val txtMsg = binding.txtMsgSend.text.toString()
        binding.txtMsgSend.text = null

        val fromEmail = database.currentUser?.email.toString()
        val toEmail = user.email
        val time = System.currentTimeMillis()
        val newMessage = Message(txtMsg, time, toEmail, fromEmail)

        if (!newMessage.text.isBlank() && newMessage.text.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("conversations")
                .document(fromEmail.toString())
                .collection(toEmail.toString())
                .add(newMessage)
                .addOnSuccessListener {
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    println("deu merda")
                }
            FirebaseFirestore.getInstance().collection("conversations")
                .document(toEmail.toString())
                .collection(fromEmail.toString())
                .add(newMessage)
                .addOnSuccessListener {
                    adapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    println("deu merda")
                }
        }
    }

    inner class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>() {

        val listMessages = mutableListOf<Message>()

        override fun getItemViewType(position: Int): Int {
            if (listMessages[position].fromEmail == me.email) {
                return 0
            } else
            return 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
            val left = MessagesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_msg_receive, parent, false)
            )

            val right = MessagesViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_msg_send, parent, false)
            )

            if (viewType == 0) {
                return right
            } else {
                return left
            }

        }

        override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
            holder.bind(listMessages[position])

        }

        override fun getItemCount(): Int {
            return listMessages.size
        }

        inner class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val txt_msg: TextView = itemView.findViewById(R.id.messageUser)
            val photoMsgUser: ImageView = itemView.findViewById(R.id.userMessagePhoto)


            fun bind(message: Message) {

                txt_msg.text = message.text

                if (listMessages[adapterPosition].fromEmail == me.email) {
                    Picasso.get()
                        .load(me.urlPhoto)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(photoMsgUser)

                } else {
                    Picasso.get()
                        .load(user.urlPhoto)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(photoMsgUser)

                }

            }
        }
    }
}
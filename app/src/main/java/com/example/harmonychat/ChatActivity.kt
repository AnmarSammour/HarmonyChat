package com.example.harmonychat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.harmonychat.Adapters.MessagesAdapter
import com.example.harmonychat.Models.Message
import com.example.harmonychat.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var messages: ArrayList<Message>
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var senderUId: String
    private lateinit var receiverUId: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading")
        dialog.setCancelable(false)

        messages = ArrayList()
        messagesAdapter = MessagesAdapter(this, messages)

        binding.messsagerecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messsagerecyclerView.adapter = messagesAdapter

        supportActionBar?.hide()

        val name = intent.getStringExtra("name")
        receiverUId = intent.getStringExtra("uid") ?: ""
        val profileImage = intent.getStringExtra("profileImage")
        senderUId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        senderRoom = senderUId + receiverUId
        receiverRoom = receiverUId + senderUId

        binding.activename.text = name
        Glide.with(this).load(profileImage).into(binding.profiledp)

        database.reference.child("chats")
            .child(senderRoom)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in dataSnapshot.children) {
                        val message = snapshot1.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    messagesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        binding.attachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val mimeTypes = arrayOf("image/*", "video/*","application/pdf", "application/msword")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(intent, 25)
        }

        binding.sendBtn.setOnClickListener {
            val messageTxt = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUId, date.time)
            binding.messageBox.text.clear()
            database.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .push()
                .setValue(message)
                .addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .push()
                        .setValue(message)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && resultCode == RESULT_OK) {
            data?.data?.let { selectedFile ->
                val fileType = contentResolver.getType(selectedFile)
                if (fileType != null) {
                    val calendar = Calendar.getInstance()
                    val reference = storage.reference.child("chats").child(calendar.timeInMillis.toString())
                    dialog.show()
                    reference.putFile(selectedFile)
                        .addOnCompleteListener { task: Task<UploadTask.TaskSnapshot?> ->
                            dialog.dismiss()
                            if (task.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { uri: Uri? ->
                                    val fileUrl = uri.toString()
                                    val date = Date()
                                    val message = Message("", senderUId, date.time)
                                    if (fileType.startsWith("image/")) {
                                        message.imageUrl = fileUrl
                                        message.message = "photo"
                                    } else if (fileType.startsWith("video/")) {
                                        message.videoUrl = fileUrl
                                        message.message = "video"
                                    } else if (fileType == "application/pdf") {
                                        message.pdfUrl = fileUrl
                                        message.message = "pdf"
                                    } else if (fileType == "application/msword") {
                                        message.docUrl = fileUrl
                                        message.message = "doc"
                                    }
                                    database.reference.child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(message)
                                        .addOnSuccessListener {
                                            database.reference.child("chats")
                                                .child(receiverRoom)
                                                .child("messages")
                                                .push()
                                                .setValue(message)
                                        }
                                    Toast.makeText(this@ChatActivity, "File uploaded successfully", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@ChatActivity, "Failed to upload file", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}

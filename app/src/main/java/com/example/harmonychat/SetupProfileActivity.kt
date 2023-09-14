package com.example.harmonychat

import androidx.appcompat.app.AppCompatActivity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.example.harmonychat.Models.User
import com.example.harmonychat.databinding.ActivitySetupProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class SetupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImage: Uri
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialog = ProgressDialog(this)
        dialog.setMessage("Updating Profile")
        dialog.setCancelable(false)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        supportActionBar?.hide()

        binding.profileimage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding.setupBtn.setOnClickListener {
            val name = binding.nameBox.text.toString()
            if (name.isEmpty()) {
                binding.nameBox.error = "Please Enter a Name"
                return@setOnClickListener
            }
            dialog.show()
            if (selectedImage != null) {
                val reference = storage.reference.child("Profiles").child(auth.uid!!)
                reference.putFile(selectedImage!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                val uid = auth.uid!!
                                val phone = auth.currentUser!!.phoneNumber
                                val name = binding.nameBox.text.toString()
                                val user = User(uid, name, phone, imageUrl)

                                database.reference
                                    .child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        dialog.dismiss()
                                        val intent =
                                            Intent(this@SetupProfileActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }
                    }
            } else {
                val uid = auth.uid!!
                val phone = auth.currentUser!!.phoneNumber
                val user = User(uid, name, phone, "No Image")

                database.reference
                    .child("users")
                    .child(uid)
                    .setValue(user)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        val intent =
                            Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.profileimage.setImageURI(data?.data)
        selectedImage = data?.data!!
    }
}
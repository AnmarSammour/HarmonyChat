package com.example.harmonychat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.harmonychat.Adapters.TopStatusAdapter
import com.example.harmonychat.Models.Status
import com.example.harmonychat.Models.User
import com.example.harmonychat.Models.UserStatus
import com.example.harmonychat.databinding.ActivityStatusBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private lateinit var navigationView: BottomNavigationView
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: TopStatusAdapter
    private lateinit var userStatuses: ArrayList<UserStatus>
    private lateinit var dialog: ProgressDialog
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userStatuses = ArrayList()
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading Status")
        dialog.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        navigationView = binding.bottomNavigationViewStatus

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener)
        navigationView.menu.findItem(R.id.status).isChecked = true

        database.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

        adapter = TopStatusAdapter(this, userStatuses)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.statuslist.layoutManager = layoutManager
        binding.statuslist.adapter = adapter

        binding.addtostatus.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 75)
        }

        database.reference.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear()
                    for (storySnapshot in snapshot.children) {
                        val status = UserStatus()
                        status.name = storySnapshot.child("name").getValue(String::class.java)
                        status.profileImage = storySnapshot.child("profileImage").getValue(String::class.java)
                        status.lastUpdated = storySnapshot.child("lastupdated").getValue(Long::class.java)!!

                        val statuses = ArrayList<Status>()
                        for (statusSnapshot in storySnapshot.child("statuses").children) {
                            val sampleStatus = statusSnapshot.getValue(Status::class.java)
                            statuses.add(sampleStatus!!)
                        }
                        status.statuses = statuses
                        userStatuses.add(status)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private val navigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.chat -> {
                val intent = Intent(this@StatusActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (data.data != null) {
                dialog.show()
                val storage = FirebaseStorage.getInstance()
                val date = Date()
                val reference: StorageReference = storage.reference.child("status").child(date.time.toString())

                reference.putFile(data.data!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl
                                .addOnSuccessListener { uri ->
                                    val userStatus = UserStatus()
                                    userStatus.name = user.name
                                    userStatus.profileImage = user.profileImage
                                    userStatus.lastUpdated = date.time

                                    val obj = HashMap<String, Any>()
                                    obj["name"] = userStatus.name!!
                                    obj["profileImage"] = userStatus.profileImage!!
                                    obj["lastupdated"] = userStatus.lastUpdated

                                    val imageUrl = uri.toString()
                                    val status = Status(imageUrl, userStatus.lastUpdated)

                                    database.reference
                                        .child("stories")
                                        .child(FirebaseAuth.getInstance().uid!!)
                                        .updateChildren(obj)

                                    database.reference
                                        .child("stories")
                                        .child(FirebaseAuth.getInstance().uid!!)
                                        .child("statuses")
                                        .push()
                                        .setValue(status)

                                    dialog.dismiss()

                                    // هنا يمكنك إجراء الإجراءات اللازمة لإعادة الصورة إلى الحالة الخاصة بالمستخدم
                                    // يمكنك استخدام imageUrl لذلك
                                    // قد تحتاج إلى إنشاء نشاط جديد لعرض الصورة أو إجراء إجراءات أخرى.
                                }
                        }
                    }
            }
        }
    }
}

package com.example.harmonychat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harmonychat.Adapters.UsersAdapter
import com.example.harmonychat.Models.User
import com.example.harmonychat.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private val users: ArrayList<User> = ArrayList()
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomNavigationView = binding.bottomNavigationViewMain
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        database = FirebaseDatabase.getInstance()
        usersAdapter = UsersAdapter(this, users)
        binding.chatsrecyclerview.layoutManager = LinearLayoutManager(this)
        binding.chatsrecyclerview.adapter = usersAdapter

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(User::class.java)
                    users.add(user!!)
                }
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here
            }
        }

        database.reference.child("users").addValueEventListener(valueEventListener)
    }

    private val navigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.status -> {
                    val intent = Intent(this@MainActivity, StatusActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.topmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                showDeleteAccountDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Account Deletion")
        builder.setMessage("Are you sure you want to delete your account?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Delete the account here
            currentUser?.delete()
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        signOutAndRedirectToLogin()
                        Toast.makeText(
                            this@MainActivity,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to delete account. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun signOutAndRedirectToLogin() {
        firebaseAuth.signOut()
        val intent = Intent(this@MainActivity, Phone::class.java)
        startActivity(intent)
        finish()
    }

}

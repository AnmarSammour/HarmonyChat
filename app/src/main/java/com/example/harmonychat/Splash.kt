package com.example.harmonychat


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.logo)

        Handler().postDelayed({
            val intent = Intent(this@Splash, Phone::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}
package com.example.oktodo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.oktodo.metro.MetroActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var metro = findViewById<CardView>(R.id.sub_view)

        metro.setOnClickListener {
            intent = Intent(this, MetroActivity::class.java)
            startActivity(intent)
        }
    }
}
package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)
        val kirjauduButton: Button = findViewById(R.id.kirjauduButton)
        val kirjautumattaButton: Button = findViewById(R.id.kirjautumattaButton)

        kirjauduButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java);
            startActivity(intent);
        }

        kirjautumattaButton.setOnClickListener {
            val intent = Intent(this, OmaKulutusarvioActivity::class.java);
            startActivity(intent);
        }
    }
}
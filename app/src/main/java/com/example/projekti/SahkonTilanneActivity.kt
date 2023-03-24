package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SahkonTilanneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sahkon_tilanne)
        val nextButton:Button = findViewById(R.id.kulutusarvioButton)
        val settingsButton:Button = findViewById(R.id.settingsButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, OmaKulutusarvioActivity::class.java);
            startActivity(intent);
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }
    }
}
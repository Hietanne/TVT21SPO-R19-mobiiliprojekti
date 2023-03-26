package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class OmaKulutusarvioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oma_kulutusarvio)
        val settingsButton:Button = findViewById(R.id.settingsButton)

        val nextButton: Button = findViewById(R.id.sahkontilanneButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, SahkonTilanneActivity::class.java);
            startActivity(intent);
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }
    }
}
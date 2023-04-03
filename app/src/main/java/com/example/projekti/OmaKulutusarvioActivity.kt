package com.example.projekti

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class OmaKulutusarvioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oma_kulutusarvio)

        //Tarkastaa onko käyttäjä kirjauduttu jos on niin debug ikkunaan ilmoitetaan ja ilmoitetaan jos ei ole
        val user = Firebase.auth.currentUser
        if (user != null) {
            // User is signed in
            Log.d("OmaKulutusarvioActivity", "Kirjauduttu sisään")
        } else {
            // No user is signed in
            Log.d("OmaKulutusarvioActivity", "Ei kirjauduttu")
        }

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
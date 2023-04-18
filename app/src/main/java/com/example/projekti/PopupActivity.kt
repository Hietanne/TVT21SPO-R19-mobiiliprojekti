package com.example.projekti

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)
        val kirjauduButton: Button = findViewById(R.id.kirjauduButton)
        val kirjautumattaButton: Button = findViewById(R.id.kirjautumattaButton)


        // Tällä if/else lauseella tarkastetaan onko käyttäjä luotu jos on nii avaa suoraan oma kulutusarvio ikkunan.
        val user = Firebase.auth.currentUser
        if (user != null) {
            // User is signed in
            Log.d(ContentValues.TAG, "Kirjauduttu sisään")
            val intent = Intent(this, OmaKulutusarvioActivity::class.java);
            startActivity(intent);
        } else {
            // No user is signed in
            Log.d(ContentValues.TAG, "Ei kirjauduttu")
        }

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
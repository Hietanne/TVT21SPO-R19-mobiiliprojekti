package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import android.content.SharedPreferences
import android.view.View

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("myPreferences", MODE_PRIVATE)

        val isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)



        setTheme(if (isDarkModeEnabled) R.style.Theme_Projekti_Dark else R.style.Theme_Projekti)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton:Button = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressed()
        }

        val darkModeButton: SwitchCompat = findViewById(R.id.darkModeButton)
        darkModeButton.isChecked = isDarkModeEnabled

        darkModeButton.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("darkMode", isChecked).apply()
            recreate()
        }

        val auth = FirebaseAuth.getInstance()

        // Hae signout-nappi layout-tiedostosta
        val signoutButton = findViewById<Button>(R.id.signout)

        // Piilota signout-nappi, jos käyttäjä ei ole kirjautunut
        if (auth.currentUser == null) {
            signoutButton.visibility = View.GONE
        }

        // Aseta klikkikuuntelija signout-napille
        signoutButton.setOnClickListener {
            auth.signOut()
            signoutButton.visibility = View.GONE
        }

    }
}
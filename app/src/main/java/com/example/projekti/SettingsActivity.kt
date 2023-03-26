package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import android.content.SharedPreferences

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


    }
}
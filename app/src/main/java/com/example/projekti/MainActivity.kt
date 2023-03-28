package com.example.projekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        handleTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //button that takes you to the SahkonTilanneActivity
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, SahkonTilanneActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleTheme() {
        val sharedPreferences = getSharedPreferences("myPreferences", MODE_PRIVATE)
        val isDarkModeOn = sharedPreferences.getBoolean("darkMode", false)
        if (isDarkModeOn) {
            setTheme(R.style.Theme_Projekti_Dark)

        } else {
            setTheme(R.style.Theme_Projekti)
        }
    }
}
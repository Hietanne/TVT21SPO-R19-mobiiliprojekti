package com.example.projekti

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import android.content.SharedPreferences
import android.view.View

class SettingsActivity : AppCompatActivity() {
    private val updateUserRequestCode = 1

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("myPreferences", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeRadioGroup: RadioGroup = findViewById(R.id.themeRadioGroup)
        val darkRadioButton: RadioButton = findViewById(R.id.darkModeButton)
        val lightRadioButton: RadioButton = findViewById(R.id.lightModeButton)
        val systemRadioButton: RadioButton = findViewById(R.id.systemModeButton)

        when (sharedPreferences.getInt("currentThemeMode", -1)) {
            2 -> { darkRadioButton.isChecked = true }
            1 -> { lightRadioButton.isChecked = true }
            else -> { systemRadioButton.isChecked = true }
        }

        themeRadioGroup.setOnCheckedChangeListener{ _, checkedID ->
            if (lightRadioButton.id == checkedID) {
                sharedPreferences.edit().putInt("currentThemeMode", 1).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (darkRadioButton.id == checkedID) {
                sharedPreferences.edit().putInt("currentThemeMode", 2).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                sharedPreferences.edit().putInt("currentThemeMode", -1).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        val auth = FirebaseAuth.getInstance()

        val signoutButton = findViewById<Button>(R.id.signout)

        if (auth.currentUser == null) {
            signoutButton.visibility = View.GONE
        }

        val editUserButton = findViewById<Button>(R.id.edit_user_button)

        if (auth.currentUser != null) {
            editUserButton.visibility = View.VISIBLE
        }

        signoutButton.setOnClickListener {
            auth.signOut()
            signoutButton.visibility = View.GONE
            editUserButton.visibility = View.GONE
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        editUserButton.setOnClickListener {
            val intent = Intent(this, UpdateUserActivity::class.java)
            startActivityForResult(intent, updateUserRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == updateUserRequestCode) {
            val editUserButton = findViewById<Button>(R.id.edit_user_button)
            val signoutButton = findViewById<Button>(R.id.signout)

            val auth = FirebaseAuth.getInstance()

            if (auth.currentUser != null) {
                editUserButton.visibility = View.VISIBLE
                signoutButton.visibility = View.VISIBLE
            } else {
                editUserButton.visibility = View.GONE
                signoutButton.visibility = View.GONE
            }
        }
    }
}
package com.example.projekti

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

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

        val backButton:Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
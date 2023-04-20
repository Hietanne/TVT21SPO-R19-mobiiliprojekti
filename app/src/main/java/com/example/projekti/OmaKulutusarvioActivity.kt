package com.example.projekti

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.InputStreamReader
import javax.net.ssl.HttpsURLConnection
import com.google.gson.Gson
import java.net.URL


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

        val energyUsageInput:EditText = findViewById(R.id.energyUsageInput)
        energyUsageInput.addTextChangedListener {
            val energyUsage = energyUsageInput.text.toString()
            val energyUsageDouble = energyUsage.toDoubleOrNull()

            if (energyUsageDouble != null ) {
                val textPriceToday: TextView = findViewById(R.id.textViewPriceToday)
                textPriceToday.text

                val averageCost = energyUsageDouble * textPriceToday.text.toString().toDouble()

                val textAverageCost: TextView = findViewById(R.id.textViewAverageCost)
                averageCost / 100
                textAverageCost.text = averageCost.toString()
            }
        }
        getDailyAveragePrice().start()

    }

    private fun getDailyAveragePrice() : Thread {
        data class Price(
            val price: Double,
            val date: Long
        )
        data class Averages(
            val yesterdayDayTime: Double?,
            val todayDayTime: Double?,
            val tomorrowDayTime: Double?,
            val yesterdayNightTime: Double?,
            val todayNightTime: Double?,
            val weekDayTime: Double?,
            val weekNightTime: Double?
        )
        data class HighestPrice(
            val price: Double,
            val date: Long
        )
        data class Prices(
            val prices: List<Price>,
            val highestPrice: HighestPrice,
            val averages: Averages
        )

        return Thread {
            val url = URL("https://porssisahko.net/api/internal/latest.json")
            val connection = url.openConnection() as HttpsURLConnection

            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, charset("UTF-8"))
                val request = Gson().fromJson(inputStreamReader, Prices::class.java)
                val todayDayTime = request.averages.todayDayTime
                val textPriceToday: TextView = findViewById(R.id.textViewPriceToday)
                textPriceToday.text = todayDayTime.toString()
                inputStreamReader.close()
                inputSystem.close()
            }
        }
    }

}
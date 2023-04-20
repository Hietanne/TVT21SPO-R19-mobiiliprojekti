package com.example.projekti

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


class OmaKulutusarvioActivity : AppCompatActivity() {
    private val database = Database()

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

        val settingsButton: Button = findViewById(R.id.settingsButton)

        val nextButton: Button = findViewById(R.id.sahkontilanneButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, SahkonTilanneActivity::class.java);
            startActivity(intent);
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }

        val energyUsageInput: EditText = findViewById(R.id.energyUsageInput)
        energyUsageInput.addTextChangedListener {
            val energyUsage = energyUsageInput.text.toString()
            val energyUsageDouble = energyUsage.toDoubleOrNull()

            if (energyUsageDouble != null) {
                val textPriceToday: TextView = findViewById(R.id.textViewPriceToday)
                val priceToday = textPriceToday.text.toString().toDoubleOrNull()

                if (priceToday != null) {
                    val averageCost = energyUsageDouble * priceToday
                    val textAverageCost: TextView = findViewById(R.id.textViewAverageCost)
                    textAverageCost.text = String.format("%.3f", averageCost)
                }
            }
        }

        getDailyAveragePrice().start()

        val sendConsumptionButton: Button = findViewById(R.id.sendConsumptionButton)

        // Piilota "Lähetä kulutus" -nappi, jos käyttäjä ei ole kirjautunut sisään
        if (user == null) {
            sendConsumptionButton.visibility = View.GONE
        }

        sendConsumptionButton.setOnClickListener {
            val energyUsage = energyUsageInput.text.toString()
            val energyUsageInt = energyUsage.toIntOrNull()

            if (user != null && energyUsageInt != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    database.setKulutusByUid(user.uid, energyUsageInt)
                }
            }
        }

        // Hae käyttäjän kulutus ja aseta se tekstikenttään
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                database.getKulutusByUid(user.uid) { kulutus ->
                    if (kulutus != null) {
                        runOnUiThread {
                            energyUsageInput.setText(kulutus)
                            // Päivitä kustannusarvio
                            val energyUsageDouble = kulutus.toDoubleOrNull()
                            val textPriceToday: TextView = findViewById(R.id.textViewPriceToday)
                            val priceToday = textPriceToday.text.toString().toDoubleOrNull()

                            if (energyUsageDouble != null && priceToday != null) {
                                val averageCost = energyUsageDouble * priceToday
                                val textAverageCost: TextView =
                                    findViewById(R.id.textViewAverageCost)
                                textAverageCost.text = String.format("%.3f", averageCost)
                            }
                        }
                    }
                }
            }
        }
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
                textPriceToday.text = String.format("%.3f", todayDayTime)
                inputStreamReader.close()
                inputSystem.close()
            }
        }
    }

}
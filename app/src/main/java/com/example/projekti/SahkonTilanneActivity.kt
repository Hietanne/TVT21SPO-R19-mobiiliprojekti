package com.example.projekti

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

data class Price(
    val price: String,
    val start: String,
    val end: String
)

class SahkonTilanneActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val pricesList = mutableListOf<Price>()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        handleTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sahkon_tilanne)
        val nextButton:Button = findViewById(R.id.kulutusarvioButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, PopupActivity::class.java);
            startActivity(intent);
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }

        lifecycleScope.launch {
            try {
                fetchPrices()
                displayPrices(pricesList)
            } catch (e: Exception) {
                Log.e("fetchPrices", "Exception thrown", e)
            }
        }
    }

    private suspend fun fetchPrices() {
        val request = Request.Builder()
            .url("https://api.porssisahko.net/v1/latest-prices.json")
            .build()

        withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            val responseBody = response.body?.string()
            Log.d("fetchPrices", "Response body: $responseBody")
            val jsonObject = JSONTokener(responseBody).nextValue() as JSONObject
            val jsonArray = jsonObject.getJSONArray("prices")
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val price = jsonArray.getJSONObject(i)
                    val priceString = price.getString("price")
                    val startString = price.getString("startDate")
                    val endString = price.getString("endDate")
                    pricesList.add(Price(priceString, startString, endString))
                }

                Log.d("fetchPrices", "List: $pricesList")
            } else {
                Log.d("fetchPrices", "JSON array is null")
            }
        }
    }
    private fun displayPrices(pricesList: List<Price>) {
        val stringBuilder = StringBuilder()
        for (price in pricesList) {
            stringBuilder.append("Price: ${price.price}\n")
            stringBuilder.append("Start date: ${price.start}\n")
            stringBuilder.append("End date: ${price.end}\n\n")
        }
        runOnUiThread {
            val pricesTextView = findViewById<TextView>(R.id.textView)
            pricesTextView.text = stringBuilder.toString()
        }
        Log.d("displayPrices", "Formatted prices:\n$stringBuilder")
    }

    private fun handleTheme() {
        val sharedPreferences = getSharedPreferences("myPreferences", MODE_PRIVATE)
        when (sharedPreferences.getInt("currentThemeMode", -1)) {
            2 -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
            1 -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
            else -> { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
        }
    }

}






package com.example.projekti

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sahkon_tilanne)
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
}






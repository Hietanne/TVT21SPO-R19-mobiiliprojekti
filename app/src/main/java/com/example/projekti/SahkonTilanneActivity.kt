package com.example.projekti

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
        val nextButton: Button = findViewById(R.id.kulutusarvioButton)
        val settingsButton: Button = findViewById(R.id.settingsButton)

        nextButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.popup_layout, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Kulutusarvio")
                .create()

            val kirjauduButton = dialogView.findViewById<Button>(R.id.kirjauduButton)
            kirjauduButton.setOnClickListener {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    startActivity(Intent(this, OmaKulutusarvioActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                dialog.dismiss() // Sulje popup-ikkuna
            }

            val kirjautumattaButton = dialogView.findViewById<Button>(R.id.kirjautumattaButton)
            kirjautumattaButton.setOnClickListener {
                startActivity(Intent(this, OmaKulutusarvioActivity::class.java))
                dialog.dismiss() // Sulje popup-ikkuna
            }

            // Jos käyttäjä on kirjautuneena Firebaseen, avataan suoraan OmaKulutusArvioActivity
            if (FirebaseAuth.getInstance().currentUser != null) {
                startActivity(Intent(this, OmaKulutusarvioActivity::class.java))
            } else {
                dialog.show() // Näytä popup-ikkuna
            }
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }

        lifecycleScope.launch {
            try {
                fetchPrices()
                displayPrices(pricesList)
                displayPricesList(pricesList)
            } catch (e: Exception) {
                Log.e("fetchPrices", "Exception thrown", e)
            }
        }
    }

    private fun displayPricesList(pricesList: List<Price>) {
        val pricesTable = findViewById<TableLayout>(R.id.prices_table)


        // Clear the table before adding rows
        pricesTable.removeAllViews()

        //make the table scrollable if there are too many rows
        pricesTable.isVerticalScrollBarEnabled = true


        // Create header row
        val headerRow = TableRow(this)
        val headerPriceCell = TextView(this).apply {
            text = "Price (C/kWh)"
            setPadding(8, 8, 8, 8)
            //make text bold
            typeface = android.graphics.Typeface.DEFAULT_BOLD

        }
        val headerStartCell = TextView(this).apply {
            text = "Start"
            setPadding(8, 8, 8, 8)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val headerEndCell = TextView(this).apply {
            text = "End"
            setPadding(8, 8, 8, 8)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        headerRow.addView(headerPriceCell)
        headerRow.addView(headerStartCell)
        headerRow.addView(headerEndCell)
        pricesTable.addView(headerRow)

        // Add rows for each price entry
        for (price in pricesList) {
            val row = TableRow(this)
            val priceCell = TextView(this).apply {
                text = price.price
                setPadding(8, 8, 8, 8)
            }
            val startCell = TextView(this).apply {
                //format the start date to a readable date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val date = dateFormat.parse(price.start)
                date.time += 3 * 60 * 60 * 1000
                val readableDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                val readableDate = readableDateFormat.format(date)
                text = readableDate
                setPadding(8, 8, 8, 8)
            }
            val endCell = TextView(this).apply {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val date = dateFormat.parse(price.end)
                //add 3 hours to the timestamp to get the correct time
                date.time += 3 * 60 * 60 * 1000
                val readableDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                val readableDate = readableDateFormat.format(date)
                text = readableDate
                setPadding(8, 8, 8, 8)
            }
            row.addView(priceCell)
            row.addView(startCell)
            row.addView(endCell)
            pricesTable.addView(row)
        }


        //add a button to toggle the sort order
        val sortButton = findViewById<Button>(R.id.sort_button)
        sortButton.setOnClickListener {
            //toggle the sort order
            val sortedPrice = pricesList.sortedBy { it.start }
            //if its already toggled then toggle it back
            if (sortedPrice == pricesList) {
                displayPricesList(pricesList.reversed())
            } else {
                displayPricesList(sortedPrice)
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


            } else {
                Log.d("fetchPrices", "JSON array is null")
            }
        }
    }


    private fun displayPrices(pricesList: List<Price>) {


        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")


        val chart = findViewById<LineChart>(R.id.chart)

        val entries = mutableListOf<Entry>()

        for (i in pricesList.size - 1 downTo 0) {
            val price = pricesList[i]
            entries.add(Entry((pricesList.size - i - 1).toFloat(), price.price.toFloat()))
        }
        val xAxis = chart.xAxis

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                //get the timestamp from the prices
                val date = dateFormat.parse(pricesList[pricesList.size - 1 - value.toInt()].start)
                //format the timestamp to a readable date
                val formatter = SimpleDateFormat("HH' h ' dd' d ' MMM")
                //add 3 hours to the timestamp to get the correct time
                date.time += 3 * 60 * 60 * 1000
                return formatter.format(date)
            }
        }
        //set xAxis to go from left to right
        xAxis.axisMinimum = 0f
        xAxis.labelRotationAngle = -45f // rotate the labels by 45 degrees counter-clockwise
        xAxis.position = XAxis.XAxisPosition.TOP // position the labels at the bottom of the chart
        xAxis.granularity = 1f // set the minimum interval between labels to 1

        val lineDataSet = LineDataSet(entries, "Price per kWh (C) HOUR.DAY.MONTH")
        lineDataSet.color = Color.parseColor("#FF0000")

        val lineData = LineData(lineDataSet)
        chart.data = lineData
        chart.description.text = "Price chart"
        chart.animateXY(1000, 1000)
        chart.invalidate()

        // Add listener to display price when a point is clicked
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    val index = e.x.toInt()
                    val price = pricesList[pricesList.size - 1 - index].price
                    Toast.makeText(applicationContext, "Price: $price", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {}
        })
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






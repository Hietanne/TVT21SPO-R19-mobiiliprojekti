package com.example.projekti

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sahkon_tilanne)
        val nextButton: Button = findViewById(R.id.kulutusarvioButton)
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
                displayPricesList(pricesList)
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

    private fun displayPricesList(pricesList: List<Price>) {
        val recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PricesAdapter(pricesList)
    }

    private class PricesAdapter(private val pricesList: List<Price>) :
        RecyclerView.Adapter<PricesAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val priceTextView: TextView = view.findViewById(R.id.priceTextView)
            val startTextView: TextView = view.findViewById(R.id.startTextView)
            val endTextView: TextView = view.findViewById(R.id.endTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.price_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val price = pricesList[position]
            holder.priceTextView.text = price.price
            holder.startTextView.text = price.start
            holder.endTextView.text = price.end
        }

        override fun getItemCount(): Int {
            return pricesList.size
        }
    }
    private fun displayPrices(pricesList: List<Price>) {



        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")


        val chart = findViewById<LineChart>(R.id.chart)

        val entries = mutableListOf<Entry>()

        for (i in pricesList.size - 1 downTo 0) {
            val price = pricesList[i]
            Log.d("displayPrices", "Price: ${price.price}, start: ${price.start}")
            entries.add(Entry((pricesList.size - i - 1).toFloat(), price.price.toFloat()))
        }
        val xAxis = chart.xAxis

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                //get the timestamp from the prices
                val date = dateFormat.parse(pricesList[pricesList.size - 1 - value.toInt()].start)
                //format the timestamp to a readable date
                val formatter = SimpleDateFormat("HH' h ' dd' d ' MMM")
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
}






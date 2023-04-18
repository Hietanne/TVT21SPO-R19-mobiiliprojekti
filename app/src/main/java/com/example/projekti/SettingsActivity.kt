package com.example.projekti


import android.app.*
import android.content.Context
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
import android.icu.util.Calendar
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.projekti.Fragmentit.NotificationFragment
import com.example.projekti.databinding.ActivitySettingsBinding
import java.text.ChoiceFormat.nextDouble
import java.time.LocalDate
import java.util.*
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class SettingsActivity : AppCompatActivity() {
    private val updateUserRequestCode = 1

    private lateinit var binding : ActivitySettingsBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Debug", "SettingsActivity onCreate") // Debug message
        val sharedPreferences: SharedPreferences = getSharedPreferences("myPreferences", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val themeRadioGroup: RadioGroup = findViewById(R.id.themeRadioGroup)
        val darkRadioButton: RadioButton = findViewById(R.id.darkModeButton)
        val lightRadioButton: RadioButton = findViewById(R.id.lightModeButton)
        val systemRadioButton: RadioButton = findViewById(R.id.systemModeButton)

        val ilmoituksetCheckBox : CheckBox = findViewById(R.id.ilmoituksetCheckbox)
        val ilmoituksetLayout : LinearLayout = findViewById(R.id.ilmoitusLayout)
        
        ilmoituksetCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if ( isChecked ) {
                ilmoituksetLayout.visibility = View.VISIBLE
                createNotificationChannel()
                binding.submitButton.setOnClickListener {
                    scheduleNotification()
                }
            } else {
                ilmoituksetLayout.visibility = View.GONE
            }
        }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification() {
        Log.d("Debug", "Scheduling notification...")
        lifecycleScope.launch {
            val intent = Intent(applicationContext, Notification::class.java)
            val title = "Pörssisähkö nyt"
            val price = fetchCurrentHourElectricityPrice()
            val message = "Pörssisähkön hinta nyt on $price c/kWh"
            intent.putExtra(titleExtra, title)
            intent.putExtra(messageExtra, message)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = getTime()
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
            showAlert(time, title, message)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchCurrentHourElectricityPrice(): String {
        return withContext(Dispatchers.IO) {
            val url = "https://api.porssisahko.net/v1/price.json"
            val dateTime = ZonedDateTime.now(ZoneId.of("Europe/Helsinki")).plusHours(3)
            val date = dateTime.toLocalDate().toString()
            val hour = dateTime.hour.toString()

            Log.d("Debug", "Fetching electricity price for date=$date and hour=$hour")

            val connection = URL("$url?date=$date&hour=$hour").openConnection() as HttpURLConnection

            var result = ""

            try {
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream.bufferedReader()
                    val responseJson = JSONObject(inputStream.use { it.readText() })
                    val price = responseJson.getDouble("price")
                    result = price.toString()
                    Log.d("Debug", "Fetched price: $result")
                } else {
                    Log.d("Debug", "HTTP response not OK, response code: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Error fetching electricity price: $e")
            } finally {
                connection.disconnect()
            }

            result
        }
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Ilmoitus ajastettu")
            .setMessage(
                "Ilmoitus lähetetään päivittäin kello " + timeFormat.format(date))
            .setPositiveButton("Ok"){_,_ ->}
            .show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(): Long {
        val minute = binding.timePicker.minute
        val hour = binding.timePicker.hour
        val day = LocalDate.now().dayOfMonth
        val month = LocalDate.now().monthValue - 1
        val year = LocalDate.now().year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis.also {
            Log.d("Debug", "Scheduled notification time: $it") // Debug message
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        Log.d("Debug", "Creating notification channel...") // Debug message
        val name = "Notif Channel"
        val desc = "A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("Debug", "Notification channel created") // Debug message
    }
}
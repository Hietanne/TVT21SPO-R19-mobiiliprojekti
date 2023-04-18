package com.example.projekti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UpdateUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<EditText>(R.id.password_edit_text)
        val updateEmailButton = findViewById<Button>(R.id.update_email_button)
        val updatePasswordButton = findViewById<Button>(R.id.update_password_button)
        val deleteUserButton = findViewById<Button>(R.id.delete_user_button)

        updateEmailButton.setOnClickListener {
            val newEmail = emailEditText.text.toString()
            updateEmail(newEmail)
        }

        updatePasswordButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            updatePassword(newPassword)
        }

        deleteUserButton.setOnClickListener {
            deleteUser()
        }

        val backToSettingsButton = findViewById<Button>(R.id.back_to_settings_button)

        backToSettingsButton.setOnClickListener {
            finish()
        }
    }

    private fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        user?.updateEmail(newEmail)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Sähköpostiosoite päivitetty.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sähköpostiosoitteen päivitys epäonnistui.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        val user = auth.currentUser
        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Salasana päivitetty.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Salasanan päivitys epäonnistui.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUser() {
        val user = auth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Käyttäjä poistettu.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Käyttäjän poisto epäonnistui.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.example.autoinsightsadmin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "loginPrefs"
    private val IS_LOGGED_IN = "isLoggedIn"

    var firstPressTime: Long = 0

    override fun onBackPressed() {
        if (firstPressTime + 2000 > System.currentTimeMillis()) {
            finishAffinity()  // This will close all activities and exit the app
        } else {
            Toast.makeText(baseContext, "Press Back again to Exit", Toast.LENGTH_SHORT).show()
        }
        firstPressTime = System.currentTimeMillis()
    }

    private lateinit var showHide: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if the user is already logged in
        if (sharedPreferences.getBoolean(IS_LOGGED_IN, false)) {
            val intent = Intent(this, UploadImageActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        showHide = findViewById<ImageView>(R.id.showHide)
        showHide.setOnClickListener {
            togglePasswordVisibility()
        }

        usernameEditText = findViewById(R.id.user)
        passwordEditText = findViewById(R.id.passWord)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            authenticateAdmin(username, password)
        }
    }

    private fun authenticateAdmin(username: String, password: String) {
        db.collection("adminCredentials")
            .document("admin") // Replace "admin" with the document ID you use
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val adminUsername = document.getString("username")
                    val adminPassword = document.getString("password")
                    if (username == adminUsername && password == adminPassword) {
                        sharedPreferences.edit().putBoolean(IS_LOGGED_IN, true).apply()
                        val intent = Intent(this, UploadImageActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finish()
                    } else {
                        showToast("Authentication failed")
                    }
                } else {
                    showToast("Admin credentials not found")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching admin credentials: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showHide.setImageResource(R.drawable.ic_show_eye)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            showHide.setImageResource(R.drawable.ic_hide_eye)
        }
    }
}

package com.example.autoinsightsadmin

import android.content.Intent
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

    private lateinit var showHide: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        showHide = findViewById<ImageView>(R.id.showHide) //
        showHide.setOnClickListener{
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
                        val intent = Intent(this, UploadImageActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

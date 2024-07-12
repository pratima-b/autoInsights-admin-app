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

class ProfileActivity : AppCompatActivity() {

    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var retypeNewPassword: EditText
    private lateinit var changePasswordButton: Button
    private val db = FirebaseFirestore.getInstance()

    private lateinit var showHideCurrent: ImageView
    private lateinit var showHideNew: ImageView
    private lateinit var showHideRetype: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_profile)

        currentPassword = findViewById(R.id.currentPassword)
        newPassword = findViewById(R.id.newPassword)
        retypeNewPassword = findViewById(R.id.retypNewPassword)
        changePasswordButton = findViewById(R.id.cnextButton)

        showHideCurrent = findViewById(R.id.showHideCurrent)
        showHideNew = findViewById(R.id.showHideNew)
        showHideRetype = findViewById(R.id.showHideRetype)

        showHideCurrent.setOnClickListener {
            togglePasswordVisibility(currentPassword, showHideCurrent)
        }

        showHideNew.setOnClickListener {
            togglePasswordVisibility(newPassword, showHideNew)
        }

        showHideRetype.setOnClickListener {
            togglePasswordVisibility(retypeNewPassword, showHideRetype)
        }

        changePasswordButton.setOnClickListener {
            val currentPass = currentPassword.text.toString()
            val newPass = newPassword.text.toString()
            val retypePass = retypeNewPassword.text.toString()

            if (newPass != retypePass) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updatePassword(currentPass, newPass)
        }

        val logout = findViewById<ImageView>(R.id.logout)
        logout.setOnClickListener {
            logoutUser()
        }

        val home = findViewById<ImageView>(R.id.home)
        home.setOnClickListener {
            val intent = Intent(this, UploadImageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun updatePassword(currentPass: String, newPass: String) {
        val adminRef = db.collection("adminCredentials").document("admin")

        adminRef.get().addOnSuccessListener { document ->
            if (document != null && document.getString("password") == currentPass) {
                adminRef.update("password", newPass)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        // Navigate to login page
                        val intent = Intent(this, UploadImageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logoutUser() {
        val intent = Intent(this, AdminLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun togglePasswordVisibility(passwordEditText: EditText, showHide: ImageView) {
        val selectionStart = passwordEditText.selectionStart
        val selectionEnd = passwordEditText.selectionEnd

        if (passwordEditText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showHide.setImageResource(R.drawable.ic_show_eye)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            showHide.setImageResource(R.drawable.ic_hide_eye)
        }

        passwordEditText.setSelection(selectionStart, selectionEnd)
    }
}

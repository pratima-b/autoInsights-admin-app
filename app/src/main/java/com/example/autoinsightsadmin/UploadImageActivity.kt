package com.example.autoinsightsadmin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class UploadImageActivity : AppCompatActivity() {

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

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var fetchedImageView: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_image)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fetchedImageView = findViewById(R.id.fetched)
        progressBar = findViewById(R.id.progressBar)
        val proceedButton: Button = findViewById(R.id.proceedbtn)

        proceedButton.setOnClickListener {
            openFileChooser()
        }

        // Load the current image
        loadCurrentImage()

        val logout = findViewById<ImageView>(R.id.logout)
        logout.setOnClickListener {
            logoutUser()
        }

        val profile = findViewById<ImageView>(R.id.profile)
        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            imageUri?.let {
                uploadImageToFirebase(it)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        progressBar.visibility = ProgressBar.VISIBLE

        val storageReference = FirebaseStorage.getInstance().reference.child("plans.png")
        val uploadTask = storageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                progressBar.visibility = ProgressBar.GONE
                showToast("Image Uploaded Successfully")
                // Update the ImageView with the new image URL
                Glide.with(this).load(uri).into(fetchedImageView)
            }
        }.addOnFailureListener {
            progressBar.visibility = ProgressBar.GONE
            showToast("Failed to upload image")
        }
    }

    private fun loadCurrentImage() {
        val storageReference = FirebaseStorage.getInstance().reference.child("plans.png")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(fetchedImageView)
        }.addOnFailureListener {
            showToast("Failed to load current image")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logoutUser() {
        sharedPreferences.edit().putBoolean(IS_LOGGED_IN, false).apply()
        // Redirect to AdminLoginActivity
        val intent = Intent(this, AdminLoginActivity::class.java)
        // Clear the back stack to prevent navigating back to the UploadImageActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}

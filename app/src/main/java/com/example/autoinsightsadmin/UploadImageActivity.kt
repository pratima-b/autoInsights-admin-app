package com.example.autoinsightsadmin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class UploadImageActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var fetchedImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_image)

        fetchedImageView = findViewById(R.id.fetched)
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
        val storageReference = FirebaseStorage.getInstance().reference.child("plans.png")
        val uploadTask = storageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                showToast("Image Uploaded Successfully")
                // Update the ImageView with the new image URL
                Glide.with(this).load(uri).into(fetchedImageView)
            }
        }.addOnFailureListener {
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
        // Clear any stored user session or authentication tokens if necessary

        // Redirect to AdminLoginActivity
        val intent = Intent(this, AdminLoginActivity::class.java)
        // Clear the back stack to prevent navigating back to the UploadImageActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

}

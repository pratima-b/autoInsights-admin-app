package com.example.autoinsightsadmin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
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


        val zoomButton = findViewById<ImageButton>(R.id.zoomButton)

        zoomButton.setOnClickListener {
            openZoomDialog()
        }


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

        val changePassword = findViewById<TextView>(R.id.changePassword)
        changePassword.setOnClickListener {
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
        // Show the progress bar before starting to fetch the image
        progressBar.visibility = ProgressBar.VISIBLE

        val storageReference = FirebaseStorage.getInstance().reference.child("plans.png")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // Load the image using Glide and set the progress bar visibility to GONE once done
            Glide.with(this)
                .load(uri)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Hide the progress bar if image loading failed
                        progressBar.visibility = ProgressBar.GONE
                        showToast("Failed to load current image")
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Hide the progress bar when the image is loaded successfully
                        progressBar.visibility = ProgressBar.GONE
                        return false
                    }
                })
                .into(fetchedImageView)
        }.addOnFailureListener {
            // Hide the progress bar if fetching the URL failed
            progressBar.visibility = ProgressBar.GONE
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

    private fun openZoomDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_zoom_image)

        val zoomedImageView = dialog.findViewById<ImageView>(R.id.zoomedImageView)
        val storageReference = FirebaseStorage.getInstance().reference.child("plans.png")

        // Load the zoomed image into zoomedImageView
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .override(1080, 800)  // Adjust size as needed
                .into(zoomedImageView)
        }.addOnFailureListener {
            showToast("Failed to load zoomed image")
        }

        var scaleFactor = 1.0f
        var lastFocusX = 0f
        var lastFocusY = 0f
        var focusX = 0f
        var focusY = 0f

        val scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 10.0f))  // Adjust boundaries as necessary
                zoomedImageView.scaleX = scaleFactor
                zoomedImageView.scaleY = scaleFactor
                adjustTranslation(zoomedImageView, focusX, focusY)
                return true
            }
        })

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                focusX -= distanceX
                focusY -= distanceY
                adjustTranslation(zoomedImageView, focusX, focusY)
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        })

        zoomedImageView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastFocusX = event.x - focusX
                    lastFocusY = event.y - focusY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!scaleDetector.isInProgress) {
                        focusX = event.x - lastFocusX
                        focusY = event.y - lastFocusY
                        adjustTranslation(zoomedImageView, focusX, focusY)
                    }
                }
            }

            true
        }

        dialog.show()
    }

    private fun adjustTranslation(view: ImageView, focusX: Float, focusY: Float) {
        val parent = view.parent as View
        val parentWidth = parent.width
        val parentHeight = parent.height

        val viewWidth = view.width * view.scaleX
        val viewHeight = view.height * view.scaleY

        val maxTranslateX = (viewWidth - parentWidth) / 2
        val maxTranslateY = (viewHeight - parentHeight) / 2

        val constrainedX = focusX.coerceIn(-maxTranslateX, maxTranslateX)
        val constrainedY = focusY.coerceIn(-maxTranslateY, maxTranslateY)

        view.translationX = constrainedX
        view.translationY = constrainedY
    }


}

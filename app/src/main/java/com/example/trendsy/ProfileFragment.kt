package com.example.trendsy

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment"

    private lateinit var profileImageView: CircleImageView
    private lateinit var userNameEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var saveProfileButton: Button
    private lateinit var chooseImageButton: Button
    private lateinit var uploadToCloudinaryButton: Button

    private var selectedImageUri: Uri? = null

    // ActivityResultLauncher for picking images
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this).load(selectedImageUri).into(profileImageView)
                Toast.makeText(requireContext(), "Image selected. Ready to upload.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        userNameEditText = view.findViewById(R.id.userNameEditText)
        emailTextView = view.findViewById(R.id.emailTextView)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        chooseImageButton = view.findViewById(R.id.chooseImageButton)
        uploadToCloudinaryButton = view.findViewById(R.id.uploadToCloudinaryButton)

        // --- DYNAMIC DATA LOADING (UPDATED SECTION) ---

        // 📝 Load User Data:
        // Replace this comment with your actual logic to fetch data from your database or API
        loadInitialProfileData()

        // ----------------------------------------------------

        // Set up listeners
        saveProfileButton.setOnClickListener { saveProfile() }
        chooseImageButton.setOnClickListener { openImageChooser() }
        uploadToCloudinaryButton.setOnClickListener { uploadImageToCloudinary() }

        return view
    }

    /**
     * Placeholder function to load user's existing profile data (name, email, image URL)
     * from a persistent source (e.g., SharedPreferences, Room DB, or API).
     */
    private fun loadInitialProfileData() {
        // 1. Get Name from storage (e.g., SharedPreferences or a User object)
        val savedUserName = "username" // <-- REPLACE with your actual retrieval code
        userNameEditText.setText(savedUserName)

        // 2. Get Email from storage (Email is usually set once at signup)
        val savedEmail = "usermail" // <-- REPLACE with your actual retrieval code
        emailTextView.text = savedEmail

        // 3. Load Profile Image if a URL exists
        val savedImageUrl: String? = null // <-- REPLACE with your actual retrieval of the image URL
        if (savedImageUrl != null && savedImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(savedImageUrl)
                .placeholder(R.drawable.ic_default_profile_pic) // Ensure you have a default placeholder drawable
                .into(profileImageView)
        }
    }


    private fun saveProfile() {
        val newUserName = userNameEditText.text.toString().trim()

        if (newUserName.isNotEmpty()) {
            // TODO: Step 1: Send API request to update the user's name on the server.
            // TODO: Step 2: Update local storage (SharedPreferences/DB) upon successful API response.
            Toast.makeText(requireContext(), "Profile Saved: $newUserName", Toast.LENGTH_SHORT).show()
        } else {
            userNameEditText.error = "User Name cannot be empty"
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun uploadImageToCloudinary() {
        selectedImageUri?.let { uri ->
            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()

            try {
                MediaManager.get().upload(uri)
                    .option("folder", "profile_images")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Toast.makeText(requireContext(), "Cloudinary upload started", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Upload started: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(TAG, "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                            val imageUrl = resultData?.get("secure_url") as? String
                            if (imageUrl != null) {
                                Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_LONG).show()
                                Log.i(TAG, "Cloudinary URL: $imageUrl")

                                // 🎯 IMPORTANT: This is where you save the new image URL.
                                // TODO: Step 1: Send API request to save 'imageUrl' to the user's profile on the server.
                                // TODO: Step 2: Update local storage (SharedPreferences/DB) upon successful API response.

                                Glide.with(this@ProfileFragment).load(imageUrl).into(profileImageView)
                            } else {
                                Toast.makeText(requireContext(), "Upload successful, but no URL returned.", Toast.LENGTH_LONG).show()
                                Log.e(TAG, "Upload succeeded but secure_url was null or missing.")
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            val errorMessage = "Upload failed: ${error.description} (Code: ${error.code})"
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                            Log.e(TAG, "Cloudinary Error - ID: $requestId, Message: $errorMessage")
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Toast.makeText(requireContext(), "Upload rescheduled: ${error.description}", Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Upload preparation failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Upload Preparation Exception", e)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Please choose an image first!", Toast.LENGTH_SHORT).show()
        }
    }
}
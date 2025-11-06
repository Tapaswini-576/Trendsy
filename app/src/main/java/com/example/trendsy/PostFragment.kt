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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Use activityViewModels for shared scope
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class PostFragment : Fragment() {

    private val tag = "PostFragment"
    private val postViewModel: PostViewModel by activityViewModels() // Shared ViewModel scope

    private lateinit var postTextContent: EditText
    private lateinit var chooseMediaButton: Button
    private lateinit var postButton: Button
    private lateinit var mediaPreview: ImageView

    private var selectedMediaUri: Uri? = null

    // Launcher for selecting both images and videos
    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedMediaUri = uri
                    // Display the thumbnail/preview
                    Glide.with(this).load(selectedMediaUri).into(mediaPreview)
                    mediaPreview.visibility = View.VISIBLE
                    postViewModel.updateStatus("Media selected: ${uri.lastPathSegment}", false)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        postTextContent = view.findViewById(R.id.postTextContent)
        chooseMediaButton = view.findViewById(R.id.chooseMediaButton)
        postButton = view.findViewById(R.id.postButton)
        mediaPreview = view.findViewById(R.id.mediaPreview)

        chooseMediaButton.setOnClickListener { openMediaChooser() }
        postButton.setOnClickListener { attemptPost() }

        return view
    }

    private fun openMediaChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Allows both image and video
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        pickMediaLauncher.launch(Intent.createChooser(intent, "Select Image or Video"))
    }

    private fun attemptPost() {
        val text = postTextContent.text.toString().trim()

        if (text.isEmpty() && selectedMediaUri == null) {
            Toast.makeText(requireContext(), "Please add text or choose media.", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent double-click
        postButton.isEnabled = false

        if (selectedMediaUri != null) {
            uploadMediaToCloudinary(text)
        } else {
            // Post without media (text-only)
            finalizePost(text, mediaUrl = null)
        }
    }

    private fun uploadMediaToCloudinary(textContent: String) {
        val uri = selectedMediaUri ?: return

        postViewModel.updateStatus("Upload starting...", false)

        try {
            MediaManager.get().upload(uri)
                .option("folder", "trendsy_media")
                // Determine resource type based on URI or extension
                // This is a simple guess, you may need a more robust mime-type check
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        postViewModel.updateStatus("Cloudinary upload started", false)
                        Log.d(tag, "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        postViewModel.updateStatus("Uploading... $progress%", false, progress)
                        Log.d(tag, "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val mediaUrl = resultData?.get("secure_url") as? String
                        if (mediaUrl != null) {
                            finalizePost(textContent, mediaUrl)
                        } else {
                            postViewModel.updateStatus("Upload failed: No URL returned.", true)
                            postButton.isEnabled = true
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        val errorMessage = "Upload failed: ${error.description}"
                        postViewModel.updateStatus(errorMessage, true)
                        Log.e(tag, "Cloudinary Error: $errorMessage")
                        postButton.isEnabled = true
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        postViewModel.updateStatus("Upload rescheduled.", false)
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            postViewModel.updateStatus("Upload failed: ${e.message}", true)
            Log.e(tag, "Upload Exception", e)
            postButton.isEnabled = true
        }
    }

    /**
     * Finalizes the post by sending data to the server and updating the app state.
     */
    private fun finalizePost(textContent: String, mediaUrl: String?) {
        // 1. Simulate getting logged-in user info (replace with real data fetching)
        val currentUserId = "user_123"
        val currentUserName = "Tapaswini"

        // 2. Create the final Post object
        val newPost = Post(
            userId = currentUserId,
            userName = currentUserName,
            textContent = textContent,
            mediaUrl = mediaUrl
        )

        // 3. Simulate API call to SAVE the post object to your server
        // TODO: Replace this with your actual API call (e.g., using Retrofit)

        // On successful server save:
        postViewModel.publishNewPost(newPost) // Notify HomeFragment
        postViewModel.updateStatus("Post successfully published!", true) // Notify NotificationFragment

        // 4. Reset Fragment UI
        postTextContent.setText("")
        mediaPreview.visibility = View.GONE
        selectedMediaUri = null
        postButton.isEnabled = true
        Toast.makeText(requireContext(), "Post Published!", Toast.LENGTH_SHORT).show()
    }
}
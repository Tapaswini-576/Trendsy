package com.example.trendsy
data class Post(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val textContent: String,
    val mediaUrl: String?, // The Cloudinary URL
    val timestamp: Long = System.currentTimeMillis()
)

// In a file named 'PostStatus.kt'
data class PostStatus(
    val message: String,
    val isSuccess: Boolean,
    val progress: Int = 0 // Used for upload progress (0-100)
)
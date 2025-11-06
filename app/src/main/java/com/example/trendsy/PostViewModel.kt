package com.example.trendsy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PostViewModel : ViewModel() {

    // LiveData for the Notification Fragment to observe upload status
    private val _uploadStatus = MutableLiveData<PostStatus>()
    val uploadStatus: LiveData<PostStatus> = _uploadStatus

    // LiveData for the Home Fragment to observe new posts
    private val _newPost = MutableLiveData<Post>()
    val newPost: LiveData<Post> = _newPost

    /**
     * Updates the status message for the Notification Fragment.
     */
    fun updateStatus(message: String, isSuccess: Boolean, progress: Int = 0) {
        _uploadStatus.postValue(PostStatus(message, isSuccess, progress))
    }

    /**
     * Publishes a successfully created post for the Home Fragment to consume.
     */
    fun publishNewPost(post: Post) {
        _newPost.postValue(post)
    }
}
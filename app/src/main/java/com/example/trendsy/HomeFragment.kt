package com.example.trendsy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Crucial for accessing the shared ViewModel
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class HomeFragment : Fragment() {

    private val tag = "HomeFragment"

    // Get the shared instance of PostViewModel using activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)

        // Initialize the adapter with an empty list
        postAdapter = PostAdapter(mutableListOf())
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsRecyclerView.adapter = postAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initial Data Load: Fetch existing posts when the fragment is created
        fetchInitialPosts()

        // 2. Real-time Updates: Observe for new posts from PostFragment
        postViewModel.newPost.observe(viewLifecycleOwner, Observer { newPost ->
            Log.d(tag, "New Post received via LiveData: ${newPost.textContent}")

            // Add the new post to the top of the feed via the adapter
            postAdapter.addPost(newPost)

            // Scroll to the top to show the user the new post immediately
            postsRecyclerView.scrollToPosition(0)

            // Update empty state visibility
            updateEmptyState()
        })
    }

    private fun fetchInitialPosts() {
        // to load existing posts from your server or Firestore database.
        Log.d(tag, "Fetching initial posts...")

        // --- Simulated Initial Data (Remove this block for production) ---
        val initialPosts = listOf(
            Post("id_001", "user_999", "Admin", "Welcome to Trendsy! Starting the feed with a sample post.", mediaUrl = null),
            Post("id_002", "user_101", "Fashionista", "Got my outfit ready for the weekend! Loving this purple theme.", mediaUrl = "https://picsum.photos/400/300?random=1", timestamp = System.currentTimeMillis() - 7200000), // 2 hours ago
            Post("id_003", "user_102", "Traveler", "Just uploaded my media via the new post feature!", mediaUrl = "https://picsum.photos/400/300?random=2", timestamp = System.currentTimeMillis() - 10800000) // 3 hours ago
        )
        // --- End Simulated Data ---

        postAdapter.setPosts(initialPosts)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (postAdapter.itemCount == 0) {
            emptyStateText.visibility = View.VISIBLE
            postsRecyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            postsRecyclerView.visibility = View.VISIBLE
        }
    }
}
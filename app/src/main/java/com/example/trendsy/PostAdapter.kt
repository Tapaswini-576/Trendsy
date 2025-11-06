package com.example.trendsy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(
    private val posts: MutableList<Post> // Use a mutable list
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: CircleImageView = itemView.findViewById(R.id.postUserAvatar)
        val userName: TextView = itemView.findViewById(R.id.postUserName)
        val timestamp: TextView = itemView.findViewById(R.id.postTimestamp)
        val textContent: TextView = itemView.findViewById(R.id.postTextContent)
        val media: ImageView = itemView.findViewById(R.id.postMedia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // 1. User Info
        holder.userName.text = post.userName

        // TODO: Load actual user avatar URL here using Glide (e.g., from a separate user data lookup)
        // Glide.with(holder.itemView.context).load(userAvatarUrl).into(holder.userAvatar)

        // 2. Text Content
        holder.textContent.text = post.textContent

        // 3. Timestamp (simple formatting for now)
        holder.timestamp.text = formatTimestamp(post.timestamp)

        // 4. Media Content
        if (post.mediaUrl.isNullOrEmpty()) {
            holder.media.visibility = View.GONE
        } else {
            holder.media.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.mediaUrl)
                .placeholder(R.drawable.ic_default_profile_pic) // Use a proper placeholder
                .into(holder.media)
        }
    }

    override fun getItemCount(): Int = posts.size

    /**
     * Adds a new post to the list (used for real-time updates from PostFragment)
     */
    fun addPost(post: Post) {
        // We add the new post to the top of the list (index 0)
        posts.add(0, post)
        notifyItemInserted(0)
        // If you're using this with a RecyclerView, you might want to trigger a scroll to the top
        // in the HomeFragment to show the user the new post.
    }

    /**
     * Replaces the entire list (used for initial data load)
     */
    fun setPosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    // Simple utility function for display
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now" // Less than 1 minute
            diff < 3600000 -> "${diff / 60000} minutes ago" // Less than 1 hour
            diff < 86400000 -> "${diff / 3600000} hours ago" // Less than 1 day
            else -> android.text.format.DateFormat.format("MMM d, yyyy", timestamp).toString()
        }
    }
}
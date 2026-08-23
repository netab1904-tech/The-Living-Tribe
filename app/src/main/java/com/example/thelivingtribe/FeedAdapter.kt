package com.example.thelivingtribe

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class FeedAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val db = FirebaseFirestore.getInstance()
    private val httpClient = OkHttpClient()

    private val supabaseUrl = "https://jjwnatpbexkfdnggdwrj.supabase.co"
    private val supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Impqd25hdHBiZXhrZmRuZ2dkd3JqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4NTIzNDEsImV4cCI6MjEwMTQyODM0MX0.2hHggEI_jSNnH9n_VbiFDmKHKt8cF_b4utQ1cpculY4"

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUser: TextView = itemView.findViewById(R.id.tvPostUser)
        val tvMission: TextView = itemView.findViewById(R.id.tvPostMission)
        val ivImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePost)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvLikesCount: TextView = itemView.findViewById(R.id.tvLikesCount)
        val btnSendCheer: MaterialButton = itemView.findViewById(R.id.btnSendCheer)
        val tvComments: TextView = itemView.findViewById(R.id.tvComments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvUser.text = post.userName
        holder.tvMission.text = "Completed: ${post.missionName}"

        if (post.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivImage)
        }

        // ניהול לייקים
        val isLiked = post.likedBy.contains(currentUserId)
        holder.tvLikesCount.text = "${post.likesCount} cheers"
        if (isLiked) {
            holder.btnLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#58634D"))
        } else {
            holder.btnLike.imageTintList = ColorStateList.valueOf(Color.parseColor("#CCD5AE"))
        }

        holder.btnLike.setOnClickListener {
            toggleLike(post)
        }

        // הצגת תגובות עידוד
        if (post.comments.isNotEmpty()) {
            holder.tvComments.visibility = View.VISIBLE
            holder.tvComments.text = post.comments.takeLast(3).joinToString("\n")
        } else {
            holder.tvComments.visibility = View.GONE
        }

        // הוספת תגובת עידוד מוכנה
        holder.btnSendCheer.setOnClickListener {
            showQuickCheerDialog(post, holder.itemView)
        }

        // כפתור מחיקה למחבר בלבד
        if (post.userId == currentUserId) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this proof?")
                    .setPositiveButton("Delete") { _, _ -> deletePost(post, holder.itemView) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    private fun toggleLike(post: Post) {
        if (post.id.isEmpty() || currentUserId.isEmpty()) return

        val postRef = db.collection("posts").document(post.id)
        val isLiked = post.likedBy.contains(currentUserId)

        if (isLiked) {
            postRef.update(
                "likesCount", FieldValue.increment(-1),
                "likedBy", FieldValue.arrayRemove(currentUserId)
            )
        } else {
            postRef.update(
                "likesCount", FieldValue.increment(1),
                "likedBy", FieldValue.arrayUnion(currentUserId)
            )
        }
    }

    private fun showQuickCheerDialog(post: Post, view: View) {
        val quickCheers = arrayOf(
            "Proud of your consistency! 🌿",
            "Keep glowing! ✨",
            "Inspiring practice! 🧘‍♀️",
            "Tribe power! 🔥",
            "Mindful living at its best! 🤍"
        )

        AlertDialog.Builder(view.context)
            .setTitle("Send a Community Cheer")
            .setItems(quickCheers) { _, which ->
                val selectedCheer = quickCheers[which]
                db.collection("posts").document(post.id)
                    .update("comments", FieldValue.arrayUnion("🌿 $selectedCheer"))
                    .addOnSuccessListener {
                        Toast.makeText(view.context, "Cheer shared with the tribe!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Post, view: View) {
        if (post.id.isEmpty()) return

        db.collection("posts").document(post.id).delete()
            .addOnSuccessListener {
                Toast.makeText(view.context, "Post deleted from feed", Toast.LENGTH_SHORT).show()
                deleteImageFromSupabase(post.imageUrl)
            }
            .addOnFailureListener { e ->
                Toast.makeText(view.context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteImageFromSupabase(imageUrl: String) {
        if (imageUrl.isEmpty()) return
        val fileName = imageUrl.substringAfterLast("/")
        val deleteUrl = "$supabaseUrl/storage/v1/object/proofs/$fileName"

        val request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {}
        })
    }

    override fun getItemCount(): Int = posts.size
}
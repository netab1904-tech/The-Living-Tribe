package com.example.thelivingtribe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvFeed: RecyclerView
    private lateinit var adapter: FeedAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        db = FirebaseFirestore.getInstance()

        rvFeed = findViewById(R.id.rvFeed)
        rvFeed.layoutManager = LinearLayoutManager(this)
        adapter = FeedAdapter(postList)
        rvFeed.adapter = adapter

        // הגדרת סרגל הניווט התחתון
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_feed
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> true
                R.id.nav_rituals -> {
                    val intent = Intent(this, DailyMissionsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        listenToFeedUpdates()
    }

    private fun listenToFeedUpdates() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading feed: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                postList.clear()
                if (value != null) {
                    for (doc in value.documents) {
                        val post = doc.toObject(Post::class.java)
                        if (post != null) {
                            post.id = doc.id
                            postList.add(post)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
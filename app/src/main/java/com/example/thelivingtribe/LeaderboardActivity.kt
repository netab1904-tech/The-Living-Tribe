package com.example.thelivingtribe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val remainingUsersList = mutableListOf<LeaderboardUser>()

    private lateinit var tvTop1Name: TextView
    private lateinit var tvTop1Points: TextView
    private lateinit var tvTop2Name: TextView
    private lateinit var tvTop2Points: TextView
    private lateinit var tvTop3Name: TextView
    private lateinit var tvTop3Points: TextView
    private lateinit var layoutTopPodium: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        db = FirebaseFirestore.getInstance()

        tvTop1Name = findViewById(R.id.tvTop1Name)
        tvTop1Points = findViewById(R.id.tvTop1Points)
        tvTop2Name = findViewById(R.id.tvTop2Name)
        tvTop2Points = findViewById(R.id.tvTop2Points)
        tvTop3Name = findViewById(R.id.tvTop3Name)
        tvTop3Points = findViewById(R.id.tvTop3Points)
        layoutTopPodium = findViewById(R.id.layoutTopPodium)

        rvLeaderboard = findViewById(R.id.rvLeaderboard)
        rvLeaderboard.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(remainingUsersList)
        rvLeaderboard.adapter = adapter

        // הגדרת סרגל הניווט התחתון
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_leaderboard
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_leaderboard -> true
                R.id.nav_rituals -> {
                    val intent = Intent(this, DailyMissionsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_feed -> {
                    val intent = Intent(this, FeedActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load leaderboard: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allUsers = mutableListOf<LeaderboardUser>()
                    for (doc in snapshot.documents) {
                        val name = doc.getString("fullName") ?: "Tribe Member"
                        val points = doc.getLong("points") ?: 0L
                        val streak = doc.getLong("streak") ?: 1L
                        allUsers.add(LeaderboardUser(doc.id, name, points, streak))
                    }

                    updateUI(allUsers)
                }
            }
    }

    private fun updateUI(users: List<LeaderboardUser>) {
        if (users.isEmpty()) {
            layoutTopPodium.visibility = View.GONE
            return
        }

        layoutTopPodium.visibility = View.VISIBLE

        // מקום 1
        if (users.isNotEmpty()) {
            tvTop1Name.text = users[0].fullName
            tvTop1Points.text = "${users[0].points} pts"
        }

        // מקום 2
        if (users.size > 1) {
            tvTop2Name.text = users[1].fullName
            tvTop2Points.text = "${users[1].points} pts"
        } else {
            tvTop2Name.text = "-"
            tvTop2Points.text = "0 pts"
        }

        // מקום 3
        if (users.size > 2) {
            tvTop3Name.text = users[2].fullName
            tvTop3Points.text = "${users[2].points} pts"
        } else {
            tvTop3Name.text = "-"
            tvTop3Points.text = "0 pts"
        }

        // שאר המשתמשים מועברים לרשימה
        remainingUsersList.clear()
        if (users.size > 3) {
            remainingUsersList.addAll(users.subList(3, users.size))
        }
        adapter.notifyDataSetChanged()
    }
}
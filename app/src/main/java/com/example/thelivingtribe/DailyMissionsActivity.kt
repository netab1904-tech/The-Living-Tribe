package com.example.thelivingtribe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID

class DailyMissionsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvGreeting: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvPoints: TextView

    private val checkBoxes = mutableListOf<CheckBox>()
    private var currentPoints = 100
    private var currentStreak = 1

    private val supabaseUrl = BuildConfig.SUPABASE_URL
    private val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
    private val httpClient = OkHttpClient()
    private var selectedMissionName: String = "Yoga Practice"

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadImageToSupabase(it, selectedMissionName) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_missions)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvGreeting = findViewById(R.id.tvGreeting)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvStreak = findViewById(R.id.tvStreak)
        tvPoints = findViewById(R.id.tvPoints)

        // כפתור התנתקות (Logout)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully 👋", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val missionNames = listOf(
            "Yoga Practice",
            "Drink 2L Water",
            "Write 5 Gratitudes",
            "Workout or Movement",
            "Act of Kindness",
            "5-min Deep Breathing",
            "Healthy Meal",
            "Morning Sunlight Walk",
            "Read 5 Pages",
            "Evening Stretch"
        )

        val buttonIds = listOf(
            R.id.btnUploadProof1, R.id.btnUploadProof2, R.id.btnUploadProof3, R.id.btnUploadProof4, R.id.btnUploadProof5,
            R.id.btnUploadProof6, R.id.btnUploadProof7, R.id.btnUploadProof8, R.id.btnUploadProof9, R.id.btnUploadProof10
        )

        for (i in buttonIds.indices) {
            findViewById<Button>(buttonIds[i])?.setOnClickListener {
                selectedMissionName = missionNames[i]
                selectImageLauncher.launch("image/*")
            }
        }

        val checkboxIds = listOf(
            R.id.cbMission1, R.id.cbMission2, R.id.cbMission3, R.id.cbMission4, R.id.cbMission5,
            R.id.cbMission6, R.id.cbMission7, R.id.cbMission8, R.id.cbMission9, R.id.cbMission10
        )

        val listener = { _: Any, _: Boolean -> updateProgressAndPoints() }
        for (id in checkboxIds) {
            val cb = findViewById<CheckBox>(id)
            if (cb != null) {
                checkBoxes.add(cb)
                cb.setOnCheckedChangeListener(listener)
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.selectedItemId = R.id.nav_rituals
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_rituals -> true
                R.id.nav_feed -> {
                    val intent = Intent(this, FeedActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        loadUserData()
    }

    private fun uploadImageToSupabase(imageUri: Uri, missionName: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading proof for: $missionName...", Toast.LENGTH_SHORT).show()

        val bytes = try {
            contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }

        if (bytes == null || bytes.isEmpty()) {
            Toast.makeText(this, "Could not read image file", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "${UUID.randomUUID()}.jpg"
        val uploadUrl = "$supabaseUrl/storage/v1/object/proofs/$fileName"

        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(uploadUrl)
            .post(requestBody)
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .addHeader("Content-Type", "image/jpeg")
            .addHeader("x-upsert", "true")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DailyMissionsActivity, "Network error. Please check connection.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val publicImageUrl = "$supabaseUrl/storage/v1/object/public/proofs/$fileName"
                        runOnUiThread {
                            savePostToFirestore(publicImageUrl, missionName)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DailyMissionsActivity, "Upload failed with code: ${resp.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun savePostToFirestore(imageUrl: String, missionName: String) {
        val userId = auth.currentUser?.uid ?: return
        val userName = tvGreeting.text.toString().replace("Good morning, ", "").replace("Good morning", "Tribe Member")

        val postMap = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "missionName" to missionName,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("posts")
            .add(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Proof photo for '$missionName' shared! 🌿", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to share post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("fullName") ?: "Tribe Member"
                    currentStreak = document.getLong("streak")?.toInt() ?: 1
                    currentPoints = document.getLong("points")?.toInt() ?: 100

                    tvGreeting.text = "Good morning, $name"
                    tvStreak.text = "🔥 $currentStreak-day streak"
                    tvPoints.text = "🌿 Tribe points: $currentPoints"
                } else {
                    val defaultData = hashMapOf(
                        "fullName" to "Tribe Member",
                        "points" to 100L,
                        "streak" to 1L
                    )
                    db.collection("users").document(userId).set(defaultData)
                    tvGreeting.text = "Good morning, Tribe Member"
                    tvStreak.text = "🔥 1-day streak"
                    tvPoints.text = "🌿 Tribe points: 100"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgressAndPoints() {
        if (checkBoxes.isEmpty()) return

        var completed = 0
        for (cb in checkBoxes) {
            if (cb.isChecked) completed++
        }

        val percent = (completed * 100) / checkBoxes.size
        tvProgressPercent.text = "$percent%"

        val updatedPoints = currentPoints + (completed * 10)
        tvPoints.text = "🌿 Tribe points: $updatedPoints"

        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("points", updatedPoints)
    }
}
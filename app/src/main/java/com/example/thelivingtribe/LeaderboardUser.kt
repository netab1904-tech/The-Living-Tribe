package com.example.thelivingtribe

data class LeaderboardUser(
    val userId: String = "",
    val fullName: String = "Tribe Member",
    val points: Long = 0,
    val streak: Long = 0
)
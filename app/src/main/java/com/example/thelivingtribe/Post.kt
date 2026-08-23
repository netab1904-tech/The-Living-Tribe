package com.example.thelivingtribe

data class Post(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val missionName: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0,
    val likesCount: Long = 0,
    val likedBy: List<String> = emptyList(),
    val comments: List<String> = emptyList()
)
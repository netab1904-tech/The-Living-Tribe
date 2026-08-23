package com.example.thelivingtribe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val users: List<LeaderboardUser>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRankNumber)
        val tvName: TextView = itemView.findViewById(R.id.tvLeaderboardName)
        val tvStreak: TextView = itemView.findViewById(R.id.tvLeaderboardStreak)
        val tvPoints: TextView = itemView.findViewById(R.id.tvLeaderboardPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val user = users[position]
        // דירוג החל מ-4 כי מקומות 1-3 מוצגים בפודיום העליון
        holder.tvRank.text = "${position + 4}"
        holder.tvName.text = user.fullName
        holder.tvStreak.text = "🔥 ${user.streak}-day streak"
        holder.tvPoints.text = "${user.points} pts"
    }

    override fun getItemCount(): Int = users.size
}
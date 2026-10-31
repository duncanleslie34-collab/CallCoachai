package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AgentTier(
    val title: String,
    val minScore: Int,
    val colorHex: Long
) {
    DIAMOND("Diamond", 90, 0xFF38BDF8),
    PLATINUM("Platinum", 85, 0xFFA855F7),
    GOLD("Gold", 80, 0xFFF59E0B),
    SILVER("Silver", 70, 0xFF94A3B8),
    BRONZE("Bronze", 0, 0xFFB45309);

    companion object {
        fun fromScore(score: Int): AgentTier {
            return when {
                score >= 90 -> DIAMOND
                score >= 85 -> PLATINUM
                score >= 80 -> GOLD
                score >= 70 -> SILVER
                else -> BRONZE
            }
        }
    }
}

enum class LeaderboardMetric(
    val label: String,
    val shortLabel: String,
    val description: String
) {
    OVERALL_QA("Overall QA", "QA Avg", "Comprehensive simulation evaluation score"),
    EMPATHY("Empathy & Tone", "Empathy", "Emotional validation, active listening, and tone warmth"),
    DE_ESCALATION("De-escalation", "De-escalation", "Customer anger reduction and hostility cooling"),
    SALES_RETENTION("Sales & Retention", "Sales & Saves", "Objection handling, bundle sales, and customer save rate"),
    ACTIVITY("Most Active", "Calls", "Total number of completed call simulations")
}

enum class LeaderboardTimeframe(
    val label: String
) {
    ALL_TIME("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week")
}

data class AgentLeaderboardEntry(
    val rank: Int,
    val traineeId: String,
    val name: String,
    val role: String,
    val department: String,
    val avatarColorHex: Long,
    val avatarInitials: String,
    val totalCalls: Int,
    val overallAvg: Int,
    val bestScore: Int,
    val empathyAvg: Int,
    val deescalationAvg: Int,
    val salesAvg: Int,
    val protocolAvg: Int,
    val winRate: Int, // percentage 0-100
    val streak: Int, // consecutive calls >= 80
    val trend: Int, // change in score percentage (+/-)
    val tier: AgentTier,
    val badges: List<String>,
    val lastCallTimestamp: Long
)

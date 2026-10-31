package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CallSessionEntity
import com.example.data.model.*
import com.example.data.repository.CallCoachRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val selectedMetric: LeaderboardMetric = LeaderboardMetric.OVERALL_QA,
    val selectedTimeframe: LeaderboardTimeframe = LeaderboardTimeframe.ALL_TIME,
    val entries: List<AgentLeaderboardEntry> = emptyList(),
    val teamAverageScore: Int = 0,
    val topScore: Int = 0,
    val totalTeamSimulations: Int = 0,
    val weeklyMvp: AgentLeaderboardEntry? = null,
    val mostImproved: AgentLeaderboardEntry? = null,
    val selectedAgentForModal: AgentLeaderboardEntry? = null
)

class LeaderboardViewModel(
    private val repository: CallCoachRepository
) : ViewModel() {

    private val _selectedMetric = MutableStateFlow(LeaderboardMetric.OVERALL_QA)
    private val _selectedTimeframe = MutableStateFlow(LeaderboardTimeframe.ALL_TIME)
    private val _selectedAgentModal = MutableStateFlow<AgentLeaderboardEntry?>(null)

    val uiState: StateFlow<LeaderboardUiState> = combine(
        repository.getAllSessions(),
        _selectedMetric,
        _selectedTimeframe,
        _selectedAgentModal
    ) { sessions, metric, timeframe, modalAgent ->
        buildLeaderboardState(sessions, metric, timeframe, modalAgent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderboardUiState(isLoading = true)
    )

    fun setMetric(metric: LeaderboardMetric) {
        _selectedMetric.value = metric
    }

    fun setTimeframe(timeframe: LeaderboardTimeframe) {
        _selectedTimeframe.value = timeframe
    }

    fun selectAgentModal(entry: AgentLeaderboardEntry?) {
        _selectedAgentModal.value = entry
    }

    private fun buildLeaderboardState(
        allSessions: List<CallSessionEntity>,
        metric: LeaderboardMetric,
        timeframe: LeaderboardTimeframe,
        modalAgent: AgentLeaderboardEntry?
    ): LeaderboardUiState {
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        val filteredSessions = when (timeframe) {
            LeaderboardTimeframe.ALL_TIME -> allSessions
            LeaderboardTimeframe.THIS_MONTH -> allSessions.filter { now - it.timestamp <= oneDayMs * 30 }
            LeaderboardTimeframe.THIS_WEEK -> allSessions.filter { now - it.timestamp <= oneDayMs * 7 }
        }

        val trainees = repository.getTrainees()

        // Group sessions by trainee
        val unrankedEntries = trainees.map { trainee ->
            val traineeSessions = filteredSessions.filter {
                it.traineeId == trainee.id || it.traineeName.equals(trainee.name, ignoreCase = true)
            }.sortedByDescending { it.timestamp }

            if (traineeSessions.isNotEmpty()) {
                val totalCalls = traineeSessions.size
                val overallAvg = (traineeSessions.sumOf { it.overallScore } / totalCalls)
                val bestScore = traineeSessions.maxOf { it.overallScore }
                val empathyAvg = (traineeSessions.sumOf { it.empathyScore } / totalCalls)
                val deescalationAvg = (traineeSessions.sumOf { it.deescalationScore } / totalCalls)
                val salesAvg = (traineeSessions.sumOf { it.retentionScore } / totalCalls)
                val protocolAvg = (traineeSessions.sumOf { it.protocolScore } / totalCalls)

                val positiveCount = traineeSessions.count {
                    it.overallScore >= 75 ||
                    it.outcome.contains("Retained", ignoreCase = true) ||
                    it.outcome.contains("Satisfied", ignoreCase = true) ||
                    it.outcome.contains("Closed", ignoreCase = true) ||
                    it.outcome.contains("Upgraded", ignoreCase = true) ||
                    it.outcome.contains("De-escalated", ignoreCase = true)
                }
                val winRate = (positiveCount * 100) / totalCalls

                // Consecutive streak of calls with score >= 80
                var currentStreak = 0
                for (s in traineeSessions) {
                    if (s.overallScore >= 80) currentStreak++ else break
                }

                // Trend vs first call
                val trend = if (traineeSessions.size >= 2) {
                    val newest = traineeSessions.first().overallScore
                    val oldest = traineeSessions.last().overallScore
                    newest - oldest
                } else {
                    4 // slight positive entry momentum
                }

                val badges = mutableListOf<String>()
                if (overallAvg >= 90) badges.add("Diamond QA Elite")
                if (empathyAvg >= 90) badges.add("Empathy Champion")
                if (deescalationAvg >= 90) badges.add("De-escalation Master")
                if (salesAvg >= 88) badges.add("Sales Closer")
                if (currentStreak >= 2) badges.add("$currentStreak Streak 🔥")
                if (trend > 5) badges.add("Rapid Climber 🚀")
                if (totalCalls >= 2) badges.add("Call Leader")

                AgentLeaderboardEntry(
                    rank = 0,
                    traineeId = trainee.id,
                    name = trainee.name,
                    role = trainee.role,
                    department = trainee.department,
                    avatarColorHex = trainee.avatarColorHex,
                    avatarInitials = trainee.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
                    totalCalls = totalCalls,
                    overallAvg = overallAvg,
                    bestScore = bestScore,
                    empathyAvg = empathyAvg,
                    deescalationAvg = deescalationAvg,
                    salesAvg = salesAvg,
                    protocolAvg = protocolAvg,
                    winRate = winRate,
                    streak = currentStreak,
                    trend = trend,
                    tier = AgentTier.fromScore(overallAvg),
                    badges = badges,
                    lastCallTimestamp = traineeSessions.first().timestamp
                )
            } else {
                // Trainee with no calls in this timeframe yet
                AgentLeaderboardEntry(
                    rank = 0,
                    traineeId = trainee.id,
                    name = trainee.name,
                    role = trainee.role,
                    department = trainee.department,
                    avatarColorHex = trainee.avatarColorHex,
                    avatarInitials = trainee.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
                    totalCalls = 0,
                    overallAvg = 0,
                    bestScore = 0,
                    empathyAvg = 0,
                    deescalationAvg = 0,
                    salesAvg = 0,
                    protocolAvg = 0,
                    winRate = 0,
                    streak = 0,
                    trend = 0,
                    tier = AgentTier.BRONZE,
                    badges = listOf("Ready for Challenge"),
                    lastCallTimestamp = 0L
                )
            }
        }

        // Sort based on selected metric
        val sorted = when (metric) {
            LeaderboardMetric.OVERALL_QA -> unrankedEntries.sortedWith(
                compareByDescending<AgentLeaderboardEntry> { it.overallAvg }
                    .thenByDescending { it.bestScore }
                    .thenByDescending { it.totalCalls }
            )
            LeaderboardMetric.EMPATHY -> unrankedEntries.sortedWith(
                compareByDescending<AgentLeaderboardEntry> { it.empathyAvg }
                    .thenByDescending { it.overallAvg }
            )
            LeaderboardMetric.DE_ESCALATION -> unrankedEntries.sortedWith(
                compareByDescending<AgentLeaderboardEntry> { it.deescalationAvg }
                    .thenByDescending { it.overallAvg }
            )
            LeaderboardMetric.SALES_RETENTION -> unrankedEntries.sortedWith(
                compareByDescending<AgentLeaderboardEntry> { it.salesAvg }
                    .thenByDescending { it.overallAvg }
            )
            LeaderboardMetric.ACTIVITY -> unrankedEntries.sortedWith(
                compareByDescending<AgentLeaderboardEntry> { it.totalCalls }
                    .thenByDescending { it.overallAvg }
            )
        }

        // Assign ranks (1-indexed)
        val rankedEntries = sorted.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        val activeEntries = rankedEntries.filter { it.totalCalls > 0 }
        val teamAverage = if (activeEntries.isNotEmpty()) {
            activeEntries.sumOf { it.overallAvg } / activeEntries.size
        } else {
            0
        }
        val topScore = if (activeEntries.isNotEmpty()) {
            activeEntries.maxOf { it.bestScore }
        } else {
            0
        }
        val totalSimulations = filteredSessions.size
        val weeklyMvp = activeEntries.maxByOrNull { it.overallAvg }
        val mostImproved = activeEntries.filter { it.trend > 0 }.maxByOrNull { it.trend }

        return LeaderboardUiState(
            isLoading = false,
            selectedMetric = metric,
            selectedTimeframe = timeframe,
            entries = rankedEntries,
            teamAverageScore = teamAverage,
            topScore = topScore,
            totalTeamSimulations = totalSimulations,
            weeklyMvp = weeklyMvp,
            mostImproved = mostImproved,
            selectedAgentForModal = modalAgent
        )
    }
}

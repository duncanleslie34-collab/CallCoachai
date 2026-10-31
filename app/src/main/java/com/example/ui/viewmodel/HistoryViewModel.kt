package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CallSessionEntity
import com.example.data.model.CallSessionConverter
import com.example.data.model.HistoricalCallReport
import com.example.data.model.ScenarioCategory
import com.example.data.repository.CallCoachRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class GradeFilter(val displayName: String) {
    ALL("All Grades"),
    EXCELLENT("Grade A (90-100%)"),
    GOOD("Grade B (80-89%)"),
    NEEDS_REVIEW("Grade C/D (<80%)")
}

enum class HistorySortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SCORE_DESC("Highest Score"),
    SCORE_ASC("Lowest Score"),
    DURATION_DESC("Longest Call")
}

data class HistorySummaryStats(
    val totalCalls: Int = 0,
    val averageScore: Int = 0,
    val highestScore: Int = 0,
    val passRatePercentage: Int = 0,
    val totalDurationMinutes: Int = 0
)

data class HistoryFilterState(
    val searchQuery: String = "",
    val selectedCategory: ScenarioCategory? = null,
    val gradeFilter: GradeFilter = GradeFilter.ALL,
    val sortOption: HistorySortOption = HistorySortOption.DATE_DESC
)

class HistoryViewModel(
    private val repository: CallCoachRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilterState())
    val filterState: StateFlow<HistoryFilterState> = _filterState.asStateFlow()

    private val rawSessions: Flow<List<CallSessionEntity>> = repository.getAllSessions()

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    val selectedSessionId: StateFlow<Long?> = _selectedSessionId.asStateFlow()

    val filteredReports: StateFlow<List<HistoricalCallReport>> = combine(
        rawSessions,
        _filterState
    ) { sessions, filter ->
        val converted = sessions.map { CallSessionConverter.toReport(it) }

        converted.filter { report ->
            val matchesQuery = filter.searchQuery.isBlank() ||
                report.entity.scenarioTitle.contains(filter.searchQuery, ignoreCase = true) ||
                report.entity.traineeName.contains(filter.searchQuery, ignoreCase = true) ||
                report.entity.outcome.contains(filter.searchQuery, ignoreCase = true) ||
                report.entity.managerNotes.contains(filter.searchQuery, ignoreCase = true)

            val matchesCategory = filter.selectedCategory == null ||
                report.entity.category.equals(filter.selectedCategory.name, ignoreCase = true)

            val matchesGrade = when (filter.gradeFilter) {
                GradeFilter.ALL -> true
                GradeFilter.EXCELLENT -> report.entity.overallScore >= 90
                GradeFilter.GOOD -> report.entity.overallScore in 80..89
                GradeFilter.NEEDS_REVIEW -> report.entity.overallScore < 80
            }

            matchesQuery && matchesCategory && matchesGrade
        }.let { list ->
            when (filter.sortOption) {
                HistorySortOption.DATE_DESC -> list.sortedByDescending { it.entity.timestamp }
                HistorySortOption.DATE_ASC -> list.sortedBy { it.entity.timestamp }
                HistorySortOption.SCORE_DESC -> list.sortedByDescending { it.entity.overallScore }
                HistorySortOption.SCORE_ASC -> list.sortedBy { it.entity.overallScore }
                HistorySortOption.DURATION_DESC -> list.sortedByDescending { it.entity.durationSeconds }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryStats: StateFlow<HistorySummaryStats> = rawSessions.map { list ->
        if (list.isEmpty()) {
            HistorySummaryStats()
        } else {
            val total = list.size
            val avg = list.map { it.overallScore }.average().toInt()
            val max = list.maxOfOrNull { it.overallScore } ?: 0
            val passed = list.count { it.overallScore >= 80 }
            val passRate = (passed * 100) / total
            val totalSeconds = list.sumOf { it.durationSeconds }
            HistorySummaryStats(
                totalCalls = total,
                averageScore = avg,
                highestScore = max,
                passRatePercentage = passRate,
                totalDurationMinutes = (totalSeconds / 60)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistorySummaryStats())

    val selectedReport: StateFlow<HistoricalCallReport?> = combine(
        rawSessions,
        _selectedSessionId
    ) { sessions, selectedId ->
        if (selectedId == null) null
        else {
            val found = sessions.find { it.id == selectedId }
            found?.let { CallSessionConverter.toReport(it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: ScenarioCategory?) {
        _filterState.update { it.copy(selectedCategory = category) }
    }

    fun setGradeFilter(filter: GradeFilter) {
        _filterState.update { it.copy(gradeFilter = filter) }
    }

    fun setSortOption(sort: HistorySortOption) {
        _filterState.update { it.copy(sortOption = sort) }
    }

    fun selectSession(id: Long) {
        _selectedSessionId.value = id
    }

    fun clearSelectedSession() {
        _selectedSessionId.value = null
    }

    fun updateManagerNote(sessionId: Long, note: String) {
        viewModelScope.launch {
            repository.updateManagerNotes(sessionId, note)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            if (_selectedSessionId.value == sessionId) {
                _selectedSessionId.value = null
            }
            repository.deleteSession(sessionId)
        }
    }
}

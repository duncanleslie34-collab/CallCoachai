package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CallSessionEntity
import com.example.data.local.ManagerCoachingNoteEntity
import com.example.data.model.TraineeProfile
import com.example.data.repository.CallCoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TraineeAnalytics(
    val totalCalls: Int = 0,
    val averageScore: Int = 0,
    val averageDeescalation: Int = 0,
    val averageEmpathy: Int = 0,
    val averageRetention: Int = 0,
    val averageSales: Int = 0,
    val averageProtocol: Int = 0,
    val retentionSuccessRate: Int = 0,
    val deescalationSuccessRate: Int = 0,
    val salesConversionRate: Int = 0,
    val highestScore: Int = 0,
    val atRiskWarning: String? = null
)

data class ManagerUiState(
    val trainees: List<TraineeProfile> = emptyList(),
    val selectedTrainee: TraineeProfile? = null,
    val isAllTraineesView: Boolean = false,
    val selectedSessionForReview: CallSessionEntity? = null,
    val newNoteText: String = "",
    val newNoteTargetArea: String = "De-escalation & Trigger Words",
    val newNotePriority: String = "High",
    val isAssigningScenarioDialogVisible: Boolean = false
)

class ManagerViewModel(
    private val repository: CallCoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ManagerUiState(
            trainees = repository.getTrainees(),
            selectedTrainee = repository.getTrainees().firstOrNull()
        )
    )
    val uiState: StateFlow<ManagerUiState> = _uiState.asStateFlow()

    val sessions: StateFlow<List<CallSessionEntity>> = combine(
        repository.getAllSessions(),
        _uiState
    ) { allSessions, state ->
        if (state.isAllTraineesView || state.selectedTrainee == null) {
            allSessions
        } else {
            allSessions.filter { it.traineeId == state.selectedTrainee.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<TraineeAnalytics> = sessions.combine(_uiState) { list, state ->
        if (list.isEmpty()) {
            TraineeAnalytics()
        } else {
            val avgScore = list.map { it.overallScore }.average().toInt()
            val avgDeesc = list.map { it.deescalationScore }.average().toInt()
            val avgEmpathy = list.map { it.empathyScore }.average().toInt()
            val avgRetention = list.map { it.retentionScore }.average().toInt()
            val avgProtocol = list.map { it.protocolScore }.average().toInt()
            val highest = list.maxOfOrNull { it.overallScore } ?: 0

            val salesSessions = list.filter { it.category == "SALES_AND_UPGRADE" || it.salesScore > 0 }
            val avgSales = if (salesSessions.isNotEmpty()) salesSessions.map { it.salesScore }.average().toInt() else 80
            val salesSuccessCount = salesSessions.count { it.salesScore >= 70 || it.outcome.contains("Upgraded", ignoreCase = true) || it.outcome.contains("Sold", ignoreCase = true) || it.outcome.contains("Satisfied", ignoreCase = true) }
            val salesRate = if (salesSessions.isNotEmpty()) ((salesSuccessCount.toDouble() / salesSessions.size) * 100).toInt() else 85

            val deescSuccessCount = list.count { it.deescalationScore >= 75 || it.outcome.contains("De-escalated", ignoreCase = true) }
            val deescRate = ((deescSuccessCount.toDouble() / list.size) * 100).toInt()

            val retentionCount = list.count { it.retentionScore >= 75 || it.outcome.contains("Retained", ignoreCase = true) }
            val retentionRate = ((retentionCount.toDouble() / list.size) * 100).toInt()

            val atRisk = if (avgScore < 70) {
                "Score below 70 threshold: Requires 1-on-1 manager de-escalation coaching."
            } else if (avgDeesc < 65) {
                "Struggles with high customer hostility: Focus on HEAT model validation."
            } else null

            TraineeAnalytics(
                totalCalls = list.size,
                averageScore = avgScore,
                averageDeescalation = avgDeesc,
                averageEmpathy = avgEmpathy,
                averageRetention = avgRetention,
                averageSales = avgSales,
                averageProtocol = avgProtocol,
                retentionSuccessRate = retentionRate,
                deescalationSuccessRate = deescRate,
                salesConversionRate = salesRate,
                highestScore = highest,
                atRiskWarning = atRisk
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TraineeAnalytics())

    val coachingNotes: StateFlow<List<ManagerCoachingNoteEntity>> = repository.getAllSessions()
        .map { emptyList<ManagerCoachingNoteEntity>() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<ManagerCoachingNoteEntity>())

    fun selectTrainee(trainee: TraineeProfile?) {
        _uiState.update {
            it.copy(
                selectedTrainee = trainee,
                isAllTraineesView = (trainee == null)
            )
        }
    }

    fun setAllTraineesView(all: Boolean) {
        _uiState.update {
            it.copy(
                isAllTraineesView = all,
                selectedTrainee = if (all) null else it.trainees.firstOrNull()
            )
        }
    }

    fun selectSessionForReview(session: CallSessionEntity?) {
        _uiState.update { it.copy(selectedSessionForReview = session) }
    }

    fun updateSessionNotes(sessionId: Long, note: String) {
        viewModelScope.launch {
            repository.updateManagerNotes(sessionId, note)
            _uiState.update { state ->
                val updated = state.selectedSessionForReview?.takeIf { it.id == sessionId }?.copy(managerNotes = note)
                state.copy(selectedSessionForReview = updated ?: state.selectedSessionForReview)
            }
        }
    }

    fun updateNewNoteText(text: String) {
        _uiState.update { it.copy(newNoteText = text) }
    }

    fun updateNewNoteTargetArea(area: String) {
        _uiState.update { it.copy(newNoteTargetArea = area) }
    }

    fun updateNewNotePriority(priority: String) {
        _uiState.update { it.copy(newNotePriority = priority) }
    }

    fun addCoachingNote() {
        val state = _uiState.value
        val traineeId = state.selectedTrainee?.id ?: return
        if (state.newNoteText.isBlank()) return

        viewModelScope.launch {
            val noteEntity = ManagerCoachingNoteEntity(
                traineeId = traineeId,
                sessionId = state.selectedSessionForReview?.id,
                note = state.newNoteText.trim(),
                targetArea = state.newNoteTargetArea,
                priority = state.newNotePriority,
                timestamp = System.currentTimeMillis()
            )
            repository.addCoachingNote(noteEntity)
            _uiState.update { it.copy(newNoteText = "") }
        }
    }

    fun deleteCoachingNote(id: Long) {
        viewModelScope.launch {
            repository.deleteCoachingNote(id)
        }
    }

    fun getNotesForSelectedTrainee() =
        repository.getCoachingNotes(_uiState.value.selectedTrainee?.id ?: "")
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_sessions")
data class CallSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val traineeId: String,
    val traineeName: String,
    val scenarioId: String,
    val scenarioTitle: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val overallScore: Int,
    val letterGrade: String,
    val outcome: String,
    val deescalationScore: Int,
    val empathyScore: Int,
    val toneScore: Int,
    val retentionScore: Int,
    val salesScore: Int = 0,
    val protocolScore: Int,
    val executiveSummary: String,
    val transcriptJson: String,
    val feedbackDetailsJson: String,
    val managerNotes: String = ""
)

@Entity(tableName = "custom_scenarios")
data class CustomScenarioEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val personaName: String,
    val emotionalState: String,
    val initialAngerLevel: Int,
    val backstory: String,
    val reasonForCalling: String,
    val desiredOutcome: String,
    val scenarioBrief: String,
    val objectivesJson: String,
    val debriefQuestionsJson: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val createdByManager: String = "Manager"
)

@Entity(tableName = "manager_coaching_notes")
data class ManagerCoachingNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val traineeId: String,
    val sessionId: Long?,
    val note: String,
    val targetArea: String,
    val priority: String, // "High", "Action Item", "Commendation"
    val timestamp: Long = System.currentTimeMillis()
)

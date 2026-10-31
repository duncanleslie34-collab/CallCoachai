package com.example.data.model

data class CustomerPersona(
    val id: String,
    val name: String,
    val age: Int,
    val occupation: String,
    val emotionalState: String, // e.g. "Furious", "Anxious", "Sarcastic", "Confused"
    val initialAngerLevel: Int, // 1 to 100
    val backstory: String,
    val reasonForCalling: String,
    val desiredOutcome: String,
    val triggerWords: List<String>, // Words that anger them: "policy", "calm down"
    val soothingWords: List<String>, // Words that help: "I take ownership", "immediate credit"
    val category: ScenarioCategory,
    val avatarInitials: String
)

enum class ScenarioCategory(val displayName: String) {
    DE_ESCALATION("De-escalation"),
    RETENTION("Customer Retention"),
    SALES_AND_UPGRADE("Spectrum Sales & Upgrade"),
    BILLING_DISPUTE("Billing & Credits"),
    TECH_SUPPORT("Technical Escalation"),
    VIP_SERVICE("Executive & VIP Care")
}

enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced / High Rage")
}

data class CallObjective(
    val id: String,
    val title: String,
    val description: String,
    val isMandatory: Boolean = true
)

data class DebriefQuestion(
    val id: String,
    val question: String,
    val guidance: String
)

data class CallScenario(
    val id: String,
    val title: String,
    val category: ScenarioCategory,
    val difficulty: DifficultyLevel,
    val persona: CustomerPersona,
    val scenarioBrief: String,
    val targetObjectives: List<CallObjective>,
    val debriefQuestions: List<DebriefQuestion>,
    val isCustom: Boolean = false,
    val createdByManager: String = "System"
)

enum class Speaker {
    CUSTOMER,
    AGENT
}

data class LiveSentimentFeedback(
    val tone: String, // "Empathetic", "Defensive", "Cold", "Professional"
    val empathyScore: Int, // 0 - 100
    val deescalationImpact: Int, // -30 (soothed) to +30 (escalated)
    val prohibitedPhraseUsed: String? = null,
    val whisperSuggestion: String,
    val customerAngerLevel: Int // 0 - 100
)

data class CallTurn(
    val id: String,
    val speaker: Speaker,
    val message: String,
    val timestampMs: Long,
    val sentimentFeedback: LiveSentimentFeedback? = null
)

data class ObjectiveResult(
    val objectiveId: String,
    val title: String,
    val passed: Boolean,
    val feedback: String
)

data class DebriefAnswerEvaluation(
    val questionId: String,
    val question: String,
    val evaluation: String,
    val rating: String // "Exemplary", "Adequate", "Needs Coaching"
)

data class CallEvaluation(
    val overallScore: Int, // 0 to 100
    val letterGrade: String, // "A+", "A", "B", "C", "D", "F"
    val callOutcome: String, // "Retained & Satisfied", "De-escalated Successfully", "Escalated to Supervisor", "Customer Churned"
    val deescalationScore: Int,
    val empathyScore: Int,
    val toneProfessionalismScore: Int,
    val retentionScore: Int,
    val salesScore: Int = 0,
    val protocolAdherenceScore: Int,
    val executiveSummary: String,
    val keyStrengths: List<String>,
    val areasForImprovement: List<String>,
    val objectiveResults: List<ObjectiveResult>,
    val debriefEvaluations: List<DebriefAnswerEvaluation>,
    val recommendedTrainingFocus: String
)

data class TraineeProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val hireWeeksAgo: Int,
    val avatarColorHex: Long = 0xFF2563EB
)

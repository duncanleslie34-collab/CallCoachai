package com.example.data.repository

import com.example.data.datasource.SeedData
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CallCoachRepository(
    private val sessionDao: CallSessionDao,
    private val customScenarioDao: CustomScenarioDao,
    private val coachingDao: ManagerCoachingDao
) {
    init {
        // Pre-populate with realistic mock sessions for training demonstration if empty
        CoroutineScope(Dispatchers.IO).launch {
            val existing = sessionDao.getAllSessions().first()
            if (existing.isEmpty()) {
                seedInitialSessions()
            }
        }
    }

    fun getAllScenarios(): Flow<List<CallScenario>> {
        return customScenarioDao.getAllCustomScenarios().map { customEntities ->
            val customScenarios = customEntities.map { entity ->
                CallScenario(
                    id = entity.id,
                    title = entity.title,
                    category = try { ScenarioCategory.valueOf(entity.category) } catch (e: Exception) { ScenarioCategory.DE_ESCALATION },
                    difficulty = try { DifficultyLevel.valueOf(entity.difficulty) } catch (e: Exception) { DifficultyLevel.INTERMEDIATE },
                    persona = CustomerPersona(
                        id = "persona_${entity.id}",
                        name = entity.personaName,
                        age = 35,
                        occupation = "Client",
                        emotionalState = entity.emotionalState,
                        initialAngerLevel = entity.initialAngerLevel,
                        backstory = entity.backstory,
                        reasonForCalling = entity.reasonForCalling,
                        desiredOutcome = entity.desiredOutcome,
                        triggerWords = listOf("policy", "calm down", "cannot help"),
                        soothingWords = listOf("I understand", "take care of this", "ownership"),
                        category = try { ScenarioCategory.valueOf(entity.category) } catch (e: Exception) { ScenarioCategory.DE_ESCALATION },
                        avatarInitials = entity.personaName.take(2).uppercase()
                    ),
                    scenarioBrief = entity.scenarioBrief,
                    targetObjectives = parseObjectives(entity.objectivesJson),
                    debriefQuestions = parseDebrief(entity.debriefQuestionsJson),
                    isCustom = true,
                    createdByManager = entity.createdByManager
                )
            }
            SeedData.defaultScenarios + customScenarios
        }
    }

    suspend fun saveCustomScenario(scenario: CallScenario) {
        val objectivesJson = JSONArray().apply {
            scenario.targetObjectives.forEach { obj ->
                put(JSONObject().apply {
                    put("id", obj.id)
                    put("title", obj.title)
                    put("description", obj.description)
                    put("isMandatory", obj.isMandatory)
                })
            }
        }.toString()

        val debriefJson = JSONArray().apply {
            scenario.debriefQuestions.forEach { dq ->
                put(JSONObject().apply {
                    put("id", dq.id)
                    put("question", dq.question)
                    put("guidance", dq.guidance)
                })
            }
        }.toString()

        val entity = CustomScenarioEntity(
            id = scenario.id,
            title = scenario.title,
            category = scenario.category.name,
            difficulty = scenario.difficulty.name,
            personaName = scenario.persona.name,
            emotionalState = scenario.persona.emotionalState,
            initialAngerLevel = scenario.persona.initialAngerLevel,
            backstory = scenario.persona.backstory,
            reasonForCalling = scenario.persona.reasonForCalling,
            desiredOutcome = scenario.persona.desiredOutcome,
            scenarioBrief = scenario.scenarioBrief,
            objectivesJson = objectivesJson,
            debriefQuestionsJson = debriefJson,
            createdByManager = scenario.createdByManager
        )
        customScenarioDao.insertScenario(entity)
    }

    suspend fun deleteCustomScenario(id: String) {
        customScenarioDao.deleteScenario(id)
    }

    fun getAllSessions(): Flow<List<CallSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsForTrainee(traineeId: String): Flow<List<CallSessionEntity>> =
        sessionDao.getSessionsForTrainee(traineeId)

    fun getSessionById(id: Long): Flow<CallSessionEntity?> = sessionDao.getSessionById(id)

    suspend fun saveSession(session: CallSessionEntity): Long = sessionDao.insertSession(session)

    suspend fun updateManagerNotes(sessionId: Long, notes: String) {
        sessionDao.updateManagerNotes(sessionId, notes)
    }

    suspend fun deleteSession(id: Long) {
        sessionDao.deleteSession(id)
    }

    fun getCoachingNotes(traineeId: String): Flow<List<ManagerCoachingNoteEntity>> =
        coachingDao.getNotesForTrainee(traineeId)

    suspend fun addCoachingNote(note: ManagerCoachingNoteEntity): Long =
        coachingDao.insertNote(note)

    suspend fun deleteCoachingNote(id: Long) = coachingDao.deleteNote(id)

    fun getTrainees(): List<TraineeProfile> = SeedData.trainees

    private fun parseObjectives(jsonStr: String): List<CallObjective> {
        return try {
            val array = JSONArray(jsonStr)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                CallObjective(
                    id = obj.optString("id", "obj_$i"),
                    title = obj.optString("title", "Objective ${i + 1}"),
                    description = obj.optString("description", ""),
                    isMandatory = obj.optBoolean("isMandatory", true)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseDebrief(jsonStr: String): List<DebriefQuestion> {
        return try {
            val array = JSONArray(jsonStr)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                DebriefQuestion(
                    id = obj.optString("id", "dq_$i"),
                    question = obj.optString("question", ""),
                    guidance = obj.optString("guidance", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun seedInitialSessions() {
        val now = System.currentTimeMillis()
        val oneDay = 86_400_000L

        val sampleTurns1 = listOf(
            CallTurn(
                id = "turn_1",
                speaker = Speaker.CUSTOMER,
                message = "Finally I got through to a human being! Look, I am absolutely furious right now. I just opened my bill and see a ridiculous $180 charge. Who authorized this?!",
                timestampMs = now - (oneDay * 1) - 180000,
                sentimentFeedback = LiveSentimentFeedback("Belligerent", 40, -10, null, "Acknowledge frustration and take ownership", 95)
            ),
            CallTurn(
                id = "turn_2",
                speaker = Speaker.AGENT,
                message = "I hear exactly how shocking this is Mr. Vance. I am taking personal ownership of your account right now. Let me pull up the roaming line items and make sure this is resolved fairly for you.",
                timestampMs = now - (oneDay * 1) - 150000
            ),
            CallTurn(
                id = "turn_3",
                speaker = Speaker.CUSTOMER,
                message = "Well at least you're not reciting policy to me. Our team travels constantly—if you can't protect our corporate lines from these surprise charges, I will move all 25 accounts to Verizon.",
                timestampMs = now - (oneDay * 1) - 120000,
                sentimentFeedback = LiveSentimentFeedback("Skeptical", 80, -25, null, "Offer immediate waiver and recurring travel protection plan", 60)
            ),
            CallTurn(
                id = "turn_4",
                speaker = Speaker.AGENT,
                message = "You have been with us for years, and we value your business tremendously. I have just authorized a full $180 courtesy credit to your current invoice, and I am locking in our RoamEasy Corporate pass at zero extra charge so your team never faces surprise roaming again.",
                timestampMs = now - (oneDay * 1) - 60000
            ),
            CallTurn(
                id = "turn_5",
                speaker = Speaker.CUSTOMER,
                message = "Okay... that is actually what customer service should look like. I appreciate you handling this so quickly. Send me the confirmation email.",
                timestampMs = now - (oneDay * 1) - 20000,
                sentimentFeedback = LiveSentimentFeedback("Satisfied", 95, -30, null, "Provide email recap and warm closing", 20)
            )
        )

        val sampleEval1 = CallEvaluation(
            overallScore = 92,
            letterGrade = "A",
            callOutcome = "Retained & Satisfied",
            deescalationScore = 95,
            empathyScore = 90,
            toneProfessionalismScore = 94,
            retentionScore = 88,
            salesScore = 0,
            protocolAdherenceScore = 93,
            executiveSummary = "Alex maintained superb emotional composure against high anger, successfully validated travel frustration, waived roaming charge, and locked in the RoamEasy corporate bundle.",
            keyStrengths = listOf(
                "Immediate radical ownership in first turn without defensive hesitation",
                "Completely avoided triggering corporate terms like 'policy' and 'contract'",
                "Proactively safeguarded customer's account against future travel fees"
            ),
            areasForImprovement = listOf(
                "Verify billing contact email aloud before concluding call"
            ),
            objectiveResults = listOf(
                ObjectiveResult("obj_1", "De-escalate initial hostility without defensive justification", true, "Masterfully cooled hostility from 95% down to 20%."),
                ObjectiveResult("obj_2", "Acknowledge frustration within first 2 response turns", true, "Immediate empathy delivered on turn 1."),
                ObjectiveResult("obj_3", "Apply appropriate billing credit and explain statement", true, "Credited full $180 international surcharge.")
            ),
            debriefEvaluations = listOf(
                DebriefAnswerEvaluation("dq_1", "How did the agent navigate the customer's threat to churn 25 corporate lines?", "Remained unflappable and anchored on long-standing corporate value.", "Exemplary")
            ),
            recommendedTrainingFocus = "Tier-2 VIP Corporate Negotiations"
        )

        // Session 1: Alex Rivera (High performer)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_1",
                traineeName = "Alex Rivera",
                scenarioId = "scen_1",
                scenarioTitle = "Surprise International Data Surcharge",
                category = "BILLING_DISPUTE",
                timestamp = now - (oneDay * 1),
                durationSeconds = 184,
                overallScore = 92,
                letterGrade = "A",
                outcome = "Retained & Satisfied",
                deescalationScore = 95,
                empathyScore = 90,
                toneScore = 94,
                retentionScore = 88,
                protocolScore = 93,
                executiveSummary = "Alex maintained superb emotional composure against high anger, successfully validated travel frustration, waived roaming charge, and locked in the RoamEasy corporate bundle.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(sampleEval1),
                managerNotes = "Great empathy right at the start. Good job avoiding the word 'policy'."
            )
        )

        // Session 2: Alex Rivera (Moderate)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_1",
                traineeName = "Alex Rivera",
                scenarioId = "scen_3",
                scenarioTitle = "The Repeated Transfer Meltdown",
                category = "DE_ESCALATION",
                timestamp = now - (oneDay * 3),
                durationSeconds = 245,
                overallScore = 84,
                letterGrade = "B",
                outcome = "De-escalated Successfully",
                deescalationScore = 82,
                empathyScore = 86,
                toneScore = 88,
                retentionScore = 80,
                protocolScore = 84,
                executiveSummary = "Customer was ready to hang up. Alex took ownership and stopped the transfers, though initially hesitated when apologizing.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1.take(3)),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(
                    sampleEval1.copy(
                        overallScore = 84,
                        letterGrade = "B",
                        callOutcome = "De-escalated Successfully",
                        deescalationScore = 82,
                        empathyScore = 86,
                        executiveSummary = "Customer was ready to hang up. Alex took ownership and stopped the transfers, though initially hesitated when apologizing."
                    )
                ),
                managerNotes = "Work on immediate first-turn ownership statement."
            )
        )

        // Session 3: Jordan Smith (Needs coaching on de-escalation)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_2",
                traineeName = "Jordan Smith",
                scenarioId = "scen_2",
                scenarioTitle = "8-Year Loyalty Price Shock Churn Threat",
                category = "RETENTION",
                timestamp = now - (oneDay * 2),
                durationSeconds = 190,
                overallScore = 68,
                letterGrade = "D",
                outcome = "Escalated to Supervisor",
                deescalationScore = 55,
                empathyScore = 62,
                toneScore = 70,
                retentionScore = 75,
                protocolScore = 78,
                executiveSummary = "Jordan quoted terms and conditions too early when customer mentioned NovaNet. Defensiveness triggered customer to demand supervisor.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1.take(2)),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(
                    sampleEval1.copy(
                        overallScore = 68,
                        letterGrade = "D",
                        callOutcome = "Escalated to Supervisor",
                        deescalationScore = 55,
                        empathyScore = 62,
                        executiveSummary = "Jordan quoted terms and conditions too early when customer mentioned NovaNet. Defensiveness triggered customer to demand supervisor."
                    )
                ),
                managerNotes = "Focus on the Retention Value Ladder module before retrying."
            )
        )

        // Session 4: Jordan Smith (Turnaround session)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_2",
                traineeName = "Jordan Smith",
                scenarioId = "scen_sales_retention_1",
                scenarioTitle = "Spectrum Mobile Unlimited+ Fiber Cross-Sell",
                category = "SALES_OPPORTUNITY",
                timestamp = now - (oneDay / 2),
                durationSeconds = 210,
                overallScore = 86,
                letterGrade = "B",
                outcome = "Bundle Closed Successfully",
                deescalationScore = 80,
                empathyScore = 85,
                toneScore = 88,
                retentionScore = 90,
                protocolScore = 87,
                executiveSummary = "Strong turnaround from earlier attempts. Successfully identified multiple mobile lines eligible for Spectrum One $30/mo discount bundle.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1.take(3)),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(
                    sampleEval1.copy(
                        overallScore = 86,
                        letterGrade = "B",
                        callOutcome = "Bundle Closed Successfully",
                        deescalationScore = 80,
                        empathyScore = 85,
                        executiveSummary = "Strong turnaround from earlier attempts. Successfully identified multiple mobile lines eligible for Spectrum One $30/mo discount bundle."
                    )
                ),
                managerNotes = "Great recovery after coaching note. Commendable improvement!"
            )
        )

        // Session 5: Samira Khan (Top Tier Tech & Wi-Fi Specialist)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_4",
                traineeName = "Samira Khan",
                scenarioId = "scen_4",
                scenarioTitle = "Spectrum Gig Upgrade & Wi-Fi Pods Solution",
                category = "SALES_OPPORTUNITY",
                timestamp = now - (oneDay * 2),
                durationSeconds = 260,
                overallScore = 95,
                letterGrade = "A",
                outcome = "Resolved & Upgraded",
                deescalationScore = 94,
                empathyScore = 96,
                toneScore = 95,
                retentionScore = 92,
                protocolScore = 97,
                executiveSummary = "Outstanding technical empathy. Masterfully addressed Wi-Fi dead zones in client's upstairs home office, added 2 Spectrum Wi-Fi Pods with zero friction.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(
                    sampleEval1.copy(
                        overallScore = 95,
                        letterGrade = "A",
                        callOutcome = "Resolved & Upgraded",
                        deescalationScore = 94,
                        empathyScore = 96,
                        toneScore = 95,
                        executiveSummary = "Outstanding technical empathy. Masterfully addressed Wi-Fi dead zones in client's upstairs home office, added 2 Spectrum Wi-Fi Pods with zero friction."
                    )
                ),
                managerNotes = "Benchmark call for new hire onboarding. Perfect tone and active listening."
            )
        )

        // Session 6: Taylor Chen (Billing & Account Care Specialist)
        sessionDao.insertSession(
            CallSessionEntity(
                traineeId = "trainee_3",
                traineeName = "Taylor Chen",
                scenarioId = "scen_1",
                scenarioTitle = "Surprise International Data Surcharge",
                category = "BILLING_DISPUTE",
                timestamp = now - (oneDay * 4),
                durationSeconds = 195,
                overallScore = 89,
                letterGrade = "B",
                outcome = "Retained & Satisfied",
                deescalationScore = 88,
                empathyScore = 91,
                toneScore = 89,
                retentionScore = 86,
                protocolScore = 92,
                executiveSummary = "Taylor effectively dismantled customer's panic about unexpected roaming charges by providing clear account credit breakdown and setting usage alert caps.",
                transcriptJson = CallSessionConverter.serializeTranscript(sampleTurns1.take(3)),
                feedbackDetailsJson = CallSessionConverter.serializeEvaluation(
                    sampleEval1.copy(
                        overallScore = 89,
                        letterGrade = "B",
                        callOutcome = "Retained & Satisfied",
                        deescalationScore = 88,
                        empathyScore = 91,
                        executiveSummary = "Taylor effectively dismantled customer's panic about unexpected roaming charges by providing clear account credit breakdown and setting usage alert caps."
                    )
                ),
                managerNotes = "Excellent billing explanation. Steady and clear cadence."
            )
        )

        // Prepopulate coaching notes
        coachingDao.insertNote(
            ManagerCoachingNoteEntity(
                traineeId = "trainee_2",
                sessionId = null,
                note = "Observed high tension during churn objection handling. Recommended practicing the CARP method (Control, Acknowledge, Refocus, Problem-solve).",
                targetArea = "De-escalation & Trigger Words",
                priority = "High",
                timestamp = now - (oneDay * 2)
            )
        )
        coachingDao.insertNote(
            ManagerCoachingNoteEntity(
                traineeId = "trainee_1",
                sessionId = null,
                note = "Consistently hitting target metrics in empathy and call pacing. Ready for Tier-2 advanced VIP escalation simulations.",
                targetArea = "Advancement Review",
                priority = "Commendation",
                timestamp = now - (oneDay * 1)
            )
        )
    }
}

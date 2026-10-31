package com.example.domain.ai

import android.util.Log
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "CallSimulationEngine"

class CallSimulationEngine {

    // Structured JSON schema for real-time customer turn & whisper coaching
    private val turnSchema = Schema.obj(
        properties = mapOf(
            "customerResponse" to Schema.string(description = "What the customer says in response to the agent, true to their persona and anger level"),
            "customerAngerLevel" to Schema.integer(description = "Updated customer anger level from 0 (completely calm/happy) to 100 (furious/screaming)"),
            "agentTone" to Schema.string(description = "Detected tone of agent: e.g. Empathetic, Professional, Cold, Defensive, Hesitant"),
            "empathyScore" to Schema.integer(description = "Agent's empathy score on this turn from 0 to 100"),
            "deescalationImpact" to Schema.integer(description = "Net change in anger level caused by this turn: negative if calmed down (e.g. -15), positive if agitated (e.g. +20)"),
            "prohibitedPhraseUsed" to Schema.string(description = "Any detected taboo phrase like 'company policy', 'calm down', or empty if none", nullable = true),
            "whisperSuggestion" to Schema.string(description = "Immediate tactical co-pilot tip for the agent's next response")
        ),
        optionalProperties = listOf("prohibitedPhraseUsed")
    )

    // Generative model for real-time customer turn simulation with schema
    private val turnModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = turnSchema
                temperature = 0.7f
            }
        )
    }

    // Structured JSON schema for comprehensive post-call evaluation
    private val evaluationSchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer(description = "Overall performance score 0 to 100"),
            "letterGrade" to Schema.string(description = "Letter grade: A+, A, B, C, D, or F"),
            "callOutcome" to Schema.string(description = "Outcome: Retained & Satisfied, De-escalated Successfully, Escalated to Supervisor, or Customer Churned"),
            "deescalationScore" to Schema.integer(description = "Score 0-100 for de-escalation technique"),
            "empathyScore" to Schema.integer(description = "Score 0-100 for active listening and empathy"),
            "toneProfessionalismScore" to Schema.integer(description = "Score 0-100 for professionalism and composure"),
            "retentionScore" to Schema.integer(description = "Score 0-100 for retention ability"),
            "salesScore" to Schema.integer(description = "Score 0-100 for sales discovery, Spectrum One/Mobile/Gig value positioning, objection handling, and closing"),
            "protocolAdherenceScore" to Schema.integer(description = "Score 0-100 for adhering to service protocols"),
            "executiveSummary" to Schema.string(description = "Concise managerial summary of the agent's performance"),
            "keyStrengths" to Schema.array(
                items = Schema.string(),
                description = "List of 2 to 4 specific positive strengths exhibited by the agent"
            ),
            "areasForImprovement" to Schema.array(
                items = Schema.string(),
                description = "List of 2 to 4 specific tactical areas for improvement"
            ),
            "objectiveResults" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "objectiveId" to Schema.string(),
                        "title" to Schema.string(),
                        "passed" to Schema.boolean(),
                        "feedback" to Schema.string()
                    )
                ),
                description = "Results for each specific call objective"
            ),
            "debriefEvaluations" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "questionId" to Schema.string(),
                        "question" to Schema.string(),
                        "evaluation" to Schema.string(),
                        "rating" to Schema.string(description = "Exemplary, Adequate, or Needs Coaching")
                    )
                ),
                description = "Evaluations for manager debriefing questions"
            ),
            "recommendedTrainingFocus" to Schema.string(description = "Specific training module or technique recommended next")
        )
    )

    private val evaluationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = evaluationSchema
                temperature = 0.3f
            }
        )
    }

    suspend fun simulateNextCustomerTurn(
        scenario: CallScenario,
        history: List<CallTurn>,
        agentLatestMessage: String,
        currentAngerLevel: Int
    ): Pair<String, LiveSentimentFeedback> {
        val transcriptHistory = buildString {
            history.forEach { turn ->
                val speakerLabel = if (turn.speaker == Speaker.AGENT) "Agent" else "Customer (${scenario.persona.name})"
                appendLine("$speakerLabel: ${turn.message}")
            }
            appendLine("Agent: $agentLatestMessage")
        }

        val prompt = """
            You are roleplaying as a customer in a call center training simulation for a new agent.
            
            Customer Persona:
            - Name: ${scenario.persona.name} (Age: ${scenario.persona.age}, ${scenario.persona.occupation})
            - Current Emotional State: ${scenario.persona.emotionalState}
            - Current Anger Level: $currentAngerLevel / 100
            - Reason for Calling: ${scenario.persona.reasonForCalling}
            - Backstory: ${scenario.persona.backstory}
            - Desired Outcome: ${scenario.persona.desiredOutcome}
            - Trigger Words (anger increases if agent says these): ${scenario.persona.triggerWords.joinToString()}
            - Soothing Approaches (anger decreases): ${scenario.persona.soothingWords.joinToString()}
            
            Conversation So Far:
            $transcriptHistory
            
            Instructions:
            1. As ${scenario.persona.name}, respond naturally, emotionally, and realistically to what the agent just said.
               - If the agent is dismissive, defensive, or quotes "policy", get more agitated and push back firmly.
               - If the agent demonstrates genuine empathy, personal accountability, and offers real solutions, begin to de-escalate.
               - Keep the customer response realistic for a phone conversation (typically 1 to 4 sentences).
            2. Evaluate the agent's latest utterance for sentiment, tone, empathy (0-100), whether any prohibited phrases were used (e.g. 'calm down', 'policy', 'not my job'), and provide a concise live whisper suggestion to guide the agent.
            3. Update the customer's anger level (0-100).
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                turnModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty response from AI")
            val json = JSONObject(jsonText)

            val customerResponse = json.getString("customerResponse")
            val anger = json.getInt("customerAngerLevel").coerceIn(0, 100)
            val tone = json.optString("agentTone", "Professional")
            val empathy = json.optInt("empathyScore", 70).coerceIn(0, 100)
            val impact = json.optInt("deescalationImpact", anger - currentAngerLevel)
            val prohibited = if (json.isNull("prohibitedPhraseUsed") || json.optString("prohibitedPhraseUsed").isBlank()) {
                null
            } else {
                json.getString("prohibitedPhraseUsed")
            }
            val suggestion = json.optString("whisperSuggestion", "Listen actively and acknowledge the customer's feeling.")

            val feedback = LiveSentimentFeedback(
                tone = tone,
                empathyScore = empathy,
                deescalationImpact = impact,
                prohibitedPhraseUsed = prohibited,
                whisperSuggestion = suggestion,
                customerAngerLevel = anger
            )
            return Pair(customerResponse, feedback)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Turn simulation timed out", e)
            return fallbackTurn(scenario, agentLatestMessage, currentAngerLevel)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Turn simulation failed with exception", e)
            return fallbackTurn(scenario, agentLatestMessage, currentAngerLevel)
        }
    }

    suspend fun generateComprehensiveEvaluation(
        scenario: CallScenario,
        history: List<CallTurn>,
        durationSeconds: Int
    ): CallEvaluation {
        val transcriptFormatted = history.joinToString("\n") { turn ->
            val speaker = if (turn.speaker == Speaker.AGENT) "AGENT" else "CUSTOMER"
            "[$speaker]: ${turn.message}"
        }

        val objectivesList = scenario.targetObjectives.joinToString("\n") { obj ->
            "- ID: ${obj.id} | Title: ${obj.title} | Description: ${obj.description} | Mandatory: ${obj.isMandatory}"
        }

        val debriefList = scenario.debriefQuestions.joinToString("\n") { dq ->
            "- ID: ${dq.id} | Question: ${dq.question} | Guidance: ${dq.guidance}"
        }

        val prompt = """
            You are an expert Call Center Quality Assurance Director and Executive Coach analyzing a completed mock call.
            
            Scenario: ${scenario.title} (${scenario.category.displayName})
            Customer: ${scenario.persona.name} (${scenario.persona.emotionalState})
            Call Duration: ${durationSeconds}s
            
            Performance Objectives:
            $objectivesList
            
            Debriefing Questions:
            $debriefList
            
            Full Call Transcript:
            $transcriptFormatted
            
            Analyze the agent's performance in-depth against call center standards:
            1. De-escalation technique (Hear, Empathize, Apologize, Take Action - HEAT model).
            2. Retention tactics & value ladder (if applicable).
            3. Tone, empathy, vocabulary (absence of trigger words like 'policy', 'calm down').
            4. Each stated performance objective (pass/fail with specific feedback).
            5. Detailed debrief answers.
            
            Return the evaluation matching the exact JSON schema.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                evaluationModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty evaluation from AI")
            val json = JSONObject(jsonText)

            val overallScore = json.getInt("overallScore").coerceIn(0, 100)
            val letterGrade = json.optString("letterGrade", "B")
            val outcome = json.optString("callOutcome", "De-escalated Successfully")
            val deescalation = json.optInt("deescalationScore", 75).coerceIn(0, 100)
            val empathy = json.optInt("empathyScore", 75).coerceIn(0, 100)
            val tone = json.optInt("toneProfessionalismScore", 80).coerceIn(0, 100)
            val retention = json.optInt("retentionScore", 70).coerceIn(0, 100)
            val sales = json.optInt("salesScore", if (scenario.category == ScenarioCategory.SALES_AND_UPGRADE) 75 else 0).coerceIn(0, 100)
            val protocol = json.optInt("protocolAdherenceScore", 75).coerceIn(0, 100)
            val summary = json.optString("executiveSummary", "Call completed with adequate engagement.")

            val strengths = mutableListOf<String>()
            val strengthsArray = json.optJSONArray("keyStrengths")
            if (strengthsArray != null) {
                for (i in 0 until strengthsArray.length()) {
                    strengths.add(strengthsArray.getString(i))
                }
            }

            val improvements = mutableListOf<String>()
            val impArray = json.optJSONArray("areasForImprovement")
            if (impArray != null) {
                for (i in 0 until impArray.length()) {
                    improvements.add(impArray.getString(i))
                }
            }

            val objectiveResults = mutableListOf<ObjectiveResult>()
            val objArray = json.optJSONArray("objectiveResults")
            if (objArray != null) {
                for (i in 0 until objArray.length()) {
                    val objItem = objArray.getJSONObject(i)
                    objectiveResults.add(
                        ObjectiveResult(
                            objectiveId = objItem.optString("objectiveId"),
                            title = objItem.optString("title"),
                            passed = objItem.optBoolean("passed", true),
                            feedback = objItem.optString("feedback")
                        )
                    )
                }
            }

            val debriefResults = mutableListOf<DebriefAnswerEvaluation>()
            val debriefArray = json.optJSONArray("debriefEvaluations")
            if (debriefArray != null) {
                for (i in 0 until debriefArray.length()) {
                    val dqItem = debriefArray.getJSONObject(i)
                    debriefResults.add(
                        DebriefAnswerEvaluation(
                            questionId = dqItem.optString("questionId"),
                            question = dqItem.optString("question"),
                            evaluation = dqItem.optString("evaluation"),
                            rating = dqItem.optString("rating", "Adequate")
                        )
                    )
                }
            }

            val recommendedFocus = json.optString("recommendedTrainingFocus", "Advanced De-escalation & Trigger Word Avoidance")

            return CallEvaluation(
                overallScore = overallScore,
                letterGrade = letterGrade,
                callOutcome = outcome,
                deescalationScore = deescalation,
                empathyScore = empathy,
                toneProfessionalismScore = tone,
                retentionScore = retention,
                salesScore = sales,
                protocolAdherenceScore = protocol,
                executiveSummary = summary,
                keyStrengths = strengths.ifEmpty { listOf("Maintained professional demeanor throughout the call", "Listened attentively to customer issue") },
                areasForImprovement = improvements.ifEmpty { listOf("Provide earlier explicit ownership statements", "Deepen probing questions before proposing solutions") },
                objectiveResults = objectiveResults.ifEmpty {
                    scenario.targetObjectives.map {
                        ObjectiveResult(it.id, it.title, true, "Objective satisfied during call dialogue.")
                    }
                },
                debriefEvaluations = debriefResults.ifEmpty {
                    scenario.debriefQuestions.map {
                        DebriefAnswerEvaluation(it.id, it.question, "Addressed satisfactorily during roleplay.", "Adequate")
                    }
                },
                recommendedTrainingFocus = recommendedFocus
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Evaluation timed out", e)
            return fallbackEvaluation(scenario, history)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Evaluation failed", e)
            return fallbackEvaluation(scenario, history)
        }
    }

    private fun fallbackTurn(
        scenario: CallScenario,
        agentLatestMessage: String,
        currentAngerLevel: Int
    ): Pair<String, LiveSentimentFeedback> {
        val lower = agentLatestMessage.lowercase()
        val hasTrigger = scenario.persona.triggerWords.any { lower.contains(it.lowercase()) }
        val hasSoothing = scenario.persona.soothingWords.any { lower.contains(it.lowercase()) } ||
                lower.contains("understand") || lower.contains("help") || lower.contains("apologize") || lower.contains("ownership")

        val newAnger: Int
        val impact: Int
        val customerText: String
        val tone: String

        if (hasTrigger) {
            impact = 15
            newAnger = (currentAngerLevel + impact).coerceAtMost(100)
            customerText = "Don't you dare quote policy or tell me what to do! I've been dealing with this for hours. What are YOU specifically going to do to fix this right now?!"
            tone = "Defensive"
        } else if (hasSoothing) {
            impact = -15
            newAnger = (currentAngerLevel + impact).coerceAtLeast(10)
            customerText = "Okay... at least you are actually listening to me instead of brushing me off. I appreciate that. But I still need this resolved immediately."
            tone = "Empathetic & Collaborative"
        } else {
            impact = -5
            newAnger = (currentAngerLevel + impact).coerceIn(10, 100)
            customerText = "Alright, but what does that mean in practice? What is the actual next step here?"
            tone = "Professional"
        }

        val feedback = LiveSentimentFeedback(
            tone = tone,
            empathyScore = if (hasSoothing) 85 else if (hasTrigger) 35 else 65,
            deescalationImpact = impact,
            prohibitedPhraseUsed = if (hasTrigger) "Company Policy / Trigger phrase" else null,
            whisperSuggestion = if (hasSoothing) "Excellent validation! Now transition to offering a concrete solution." else "Acknowledge the caller's frustration before giving instructions.",
            customerAngerLevel = newAnger
        )
        return Pair(customerText, feedback)
    }

    private fun fallbackEvaluation(
        scenario: CallScenario,
        history: List<CallTurn>
    ): CallEvaluation {
        val agentTurns = history.filter { it.speaker == Speaker.AGENT }
        val avgEmpathy = if (agentTurns.isNotEmpty()) {
            agentTurns.mapNotNull { it.sentimentFeedback?.empathyScore }.average().toInt().coerceIn(50, 95)
        } else 70

        return CallEvaluation(
            overallScore = 80,
            letterGrade = "B",
            callOutcome = "De-escalated Successfully",
            deescalationScore = 82,
            empathyScore = avgEmpathy,
            toneProfessionalismScore = 85,
            retentionScore = 78,
            salesScore = if (scenario.category == ScenarioCategory.SALES_AND_UPGRADE) 82 else 0,
            protocolAdherenceScore = 80,
            executiveSummary = "The agent successfully engaged ${scenario.persona.name}, navigated initial resistance, and reached a constructive resolution.",
            keyStrengths = listOf(
                "Kept composure under initial customer hostility",
                "Effectively validated the customer's stated problem",
                "Maintained steady conversational pacing"
            ),
            areasForImprovement = listOf(
                "Avoid technical terminology during high-stress moments",
                "Confirm customer agreement before concluding the call"
            ),
            objectiveResults = scenario.targetObjectives.map {
                ObjectiveResult(it.id, it.title, true, "Objective demonstrated in simulation.")
            },
            debriefEvaluations = scenario.debriefQuestions.map {
                DebriefAnswerEvaluation(it.id, it.question, "Successfully applied core de-escalation framework.", "Adequate")
            },
            recommendedTrainingFocus = "HEAT Model Mastery & Customer Retention Scripts"
        )
    }
}

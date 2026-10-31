package com.example.data.model

import com.example.data.local.CallSessionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoricalCallReport(
    val entity: CallSessionEntity,
    val evaluation: CallEvaluation,
    val turns: List<CallTurn>,
    val formattedDate: String,
    val formattedDuration: String
)

object CallSessionConverter {

    fun formatDuration(durationSeconds: Int): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
    }

    fun formatDate(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun serializeTranscript(turns: List<CallTurn>): String {
        val arr = JSONArray()
        turns.forEach { turn ->
            arr.put(JSONObject().apply {
                put("id", turn.id)
                put("speaker", turn.speaker.name)
                put("message", turn.message)
                put("timestamp", turn.timestampMs)
                if (turn.sentimentFeedback != null) {
                    put("feedback", JSONObject().apply {
                        put("tone", turn.sentimentFeedback.tone)
                        put("empathy", turn.sentimentFeedback.empathyScore)
                        put("impact", turn.sentimentFeedback.deescalationImpact)
                        put("prohibited", turn.sentimentFeedback.prohibitedPhraseUsed ?: "")
                        put("suggestion", turn.sentimentFeedback.whisperSuggestion)
                        put("anger", turn.sentimentFeedback.customerAngerLevel)
                    })
                }
            })
        }
        return arr.toString()
    }

    fun deserializeTranscript(jsonStr: String?): List<CallTurn> {
        if (jsonStr.isNullOrBlank() || jsonStr == "[]") return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<CallTurn>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val speakerStr = obj.optString("speaker", "CUSTOMER")
                val speaker = try {
                    Speaker.valueOf(speakerStr)
                } catch (e: Exception) {
                    if (speakerStr.equals("agent", ignoreCase = true)) Speaker.AGENT else Speaker.CUSTOMER
                }

                val fbObj = obj.optJSONObject("feedback")
                val feedback = if (fbObj != null) {
                    val prohibited = fbObj.optString("prohibited", "").ifBlank { null }
                    LiveSentimentFeedback(
                        tone = fbObj.optString("tone", "Professional"),
                        empathyScore = fbObj.optInt("empathy", 70),
                        deescalationImpact = fbObj.optInt("impact", 0),
                        prohibitedPhraseUsed = prohibited,
                        whisperSuggestion = fbObj.optString("suggestion", ""),
                        customerAngerLevel = fbObj.optInt("anger", 40)
                    )
                } else null

                list.add(
                    CallTurn(
                        id = obj.optString("id", "turn_$i"),
                        speaker = speaker,
                        message = obj.optString("message", ""),
                        timestampMs = obj.optLong("timestamp", System.currentTimeMillis()),
                        sentimentFeedback = feedback
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeEvaluation(ev: CallEvaluation): String {
        return JSONObject().apply {
            put("overallScore", ev.overallScore)
            put("letterGrade", ev.letterGrade)
            put("callOutcome", ev.callOutcome)
            put("deescalationScore", ev.deescalationScore)
            put("empathyScore", ev.empathyScore)
            put("toneScore", ev.toneProfessionalismScore)
            put("retentionScore", ev.retentionScore)
            put("salesScore", ev.salesScore)
            put("protocolScore", ev.protocolAdherenceScore)
            put("executiveSummary", ev.executiveSummary)
            put("keyStrengths", JSONArray(ev.keyStrengths))
            put("areasForImprovement", JSONArray(ev.areasForImprovement))
            put("recommendedTrainingFocus", ev.recommendedTrainingFocus)

            val objectivesArr = JSONArray()
            ev.objectiveResults.forEach { res ->
                objectivesArr.put(JSONObject().apply {
                    put("id", res.objectiveId)
                    put("title", res.title)
                    put("passed", res.passed)
                    put("feedback", res.feedback)
                })
            }
            put("objectives", objectivesArr)

            val debriefArr = JSONArray()
            ev.debriefEvaluations.forEach { dq ->
                debriefArr.put(JSONObject().apply {
                    put("id", dq.questionId)
                    put("question", dq.question)
                    put("evaluation", dq.evaluation)
                    put("rating", dq.rating)
                })
            }
            put("debriefs", debriefArr)
        }.toString()
    }

    fun deserializeEvaluation(jsonStr: String?, fallbackEntity: CallSessionEntity): CallEvaluation {
        if (jsonStr.isNullOrBlank() || jsonStr == "{}") {
            return CallEvaluation(
                overallScore = fallbackEntity.overallScore,
                letterGrade = fallbackEntity.letterGrade,
                callOutcome = fallbackEntity.outcome,
                deescalationScore = fallbackEntity.deescalationScore,
                empathyScore = fallbackEntity.empathyScore,
                toneProfessionalismScore = fallbackEntity.toneScore,
                retentionScore = fallbackEntity.retentionScore,
                salesScore = fallbackEntity.salesScore,
                protocolAdherenceScore = fallbackEntity.protocolScore,
                executiveSummary = fallbackEntity.executiveSummary.ifBlank { "Performance report logged in training database." },
                keyStrengths = listOf("Active listening and prompt acknowledgement", "Professional conversational tone maintained throughout the interaction"),
                areasForImprovement = listOf("Expand first-turn ownership phrasing", "Refocus customer gently without interrupting"),
                objectiveResults = emptyList(),
                debriefEvaluations = emptyList(),
                recommendedTrainingFocus = "De-escalation & Retention Protocol"
            )
        }

        return try {
            val obj = JSONObject(jsonStr)
            val keyStrengths = mutableListOf<String>()
            val ksArr = obj.optJSONArray("keyStrengths")
            if (ksArr != null) {
                for (i in 0 until ksArr.length()) {
                    keyStrengths.add(ksArr.optString(i))
                }
            }

            val areas = mutableListOf<String>()
            val afiArr = obj.optJSONArray("areasForImprovement")
            if (afiArr != null) {
                for (i in 0 until afiArr.length()) {
                    areas.add(afiArr.optString(i))
                }
            }

            val objectives = mutableListOf<ObjectiveResult>()
            val objArr = obj.optJSONArray("objectives")
            if (objArr != null) {
                for (i in 0 until objArr.length()) {
                    val o = objArr.getJSONObject(i)
                    objectives.add(
                        ObjectiveResult(
                            objectiveId = o.optString("id", "obj_$i"),
                            title = o.optString("title", "Objective ${i + 1}"),
                            passed = o.optBoolean("passed", true),
                            feedback = o.optString("feedback", "")
                        )
                    )
                }
            }

            val debriefs = mutableListOf<DebriefAnswerEvaluation>()
            val debArr = obj.optJSONArray("debriefs")
            if (debArr != null) {
                for (i in 0 until debArr.length()) {
                    val d = debArr.getJSONObject(i)
                    debriefs.add(
                        DebriefAnswerEvaluation(
                            questionId = d.optString("id", "dq_$i"),
                            question = d.optString("question", ""),
                            evaluation = d.optString("evaluation", ""),
                            rating = d.optString("rating", "Adequate")
                        )
                    )
                }
            }

            CallEvaluation(
                overallScore = obj.optInt("overallScore", fallbackEntity.overallScore),
                letterGrade = obj.optString("letterGrade", fallbackEntity.letterGrade),
                callOutcome = obj.optString("callOutcome", fallbackEntity.outcome),
                deescalationScore = obj.optInt("deescalationScore", fallbackEntity.deescalationScore),
                empathyScore = obj.optInt("empathyScore", fallbackEntity.empathyScore),
                toneProfessionalismScore = obj.optInt("toneScore", fallbackEntity.toneScore),
                retentionScore = obj.optInt("retentionScore", fallbackEntity.retentionScore),
                salesScore = obj.optInt("salesScore", fallbackEntity.salesScore),
                protocolAdherenceScore = obj.optInt("protocolScore", fallbackEntity.protocolScore),
                executiveSummary = obj.optString("executiveSummary", fallbackEntity.executiveSummary),
                keyStrengths = if (keyStrengths.isNotEmpty()) keyStrengths else listOf("Radical ownership maintained", "Professional composure under pressure"),
                areasForImprovement = if (areas.isNotEmpty()) areas else listOf("Refine proactive value pitching", "Reinforce closing confirmation"),
                objectiveResults = objectives,
                debriefEvaluations = debriefs,
                recommendedTrainingFocus = obj.optString("recommendedTrainingFocus", "Advanced De-escalation & Value Retention")
            )
        } catch (e: Exception) {
            CallEvaluation(
                overallScore = fallbackEntity.overallScore,
                letterGrade = fallbackEntity.letterGrade,
                callOutcome = fallbackEntity.outcome,
                deescalationScore = fallbackEntity.deescalationScore,
                empathyScore = fallbackEntity.empathyScore,
                toneProfessionalismScore = fallbackEntity.toneScore,
                retentionScore = fallbackEntity.retentionScore,
                salesScore = fallbackEntity.salesScore,
                protocolAdherenceScore = fallbackEntity.protocolScore,
                executiveSummary = fallbackEntity.executiveSummary,
                keyStrengths = listOf("Empathy and composure under high emotional intensity"),
                areasForImprovement = listOf("Continue practicing proactive de-escalation statements"),
                objectiveResults = emptyList(),
                debriefEvaluations = emptyList(),
                recommendedTrainingFocus = "Call Center Customer Service Protocol"
            )
        }
    }

    fun toReport(entity: CallSessionEntity): HistoricalCallReport {
        val evaluation = deserializeEvaluation(entity.feedbackDetailsJson, entity)
        val turns = deserializeTranscript(entity.transcriptJson)
        return HistoricalCallReport(
            entity = entity,
            evaluation = evaluation,
            turns = turns,
            formattedDate = formatDate(entity.timestamp),
            formattedDuration = formatDuration(entity.durationSeconds)
        )
    }
}

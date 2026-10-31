package com.example.ui.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CallSessionEntity
import com.example.data.model.*
import com.example.data.repository.CallCoachRepository
import com.example.domain.ai.CallSimulationEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private const val TAG = "CallSessionViewModel"

enum class CallState {
    IDLE,
    RINGING,
    CONNECTED,
    ON_HOLD,
    EVALUATING,
    COMPLETED
}

data class ActiveCallUiState(
    val callState: CallState = CallState.IDLE,
    val scenario: CallScenario? = null,
    val trainee: TraineeProfile? = null,
    val turns: List<CallTurn> = emptyList(),
    val customerAngerLevel: Int = 50,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isOnHold: Boolean = false,
    val isTtsEnabled: Boolean = true,
    val isGeneratingCustomerResponse: Boolean = false,
    val latestFeedback: LiveSentimentFeedback? = null,
    val evaluation: CallEvaluation? = null,
    val savedSessionId: Long? = null,
    val suggestedResponses: List<String> = emptyList(),
    val errorMessage: String? = null
)

class CallSessionViewModel(
    private val repository: CallCoachRepository,
    private val simulationEngine: CallSimulationEngine = CallSimulationEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveCallUiState())
    val uiState: StateFlow<ActiveCallUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    fun initTts(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    textToSpeech?.setSpeechRate(0.95f)
                    textToSpeech?.setPitch(1.0f)
                    isTtsReady = true
                }
            }
        }
    }

    fun startCall(scenario: CallScenario, trainee: TraineeProfile) {
        stopTimer()
        _uiState.update {
            ActiveCallUiState(
                callState = CallState.RINGING,
                scenario = scenario,
                trainee = trainee,
                customerAngerLevel = scenario.persona.initialAngerLevel,
                suggestedResponses = generateInitialSuggestions(scenario)
            )
        }

        viewModelScope.launch {
            delay(1800) // Realistic ringing tone delay
            if (_uiState.value.callState == CallState.RINGING) {
                _uiState.update { it.copy(callState = CallState.CONNECTED) }
                startTimer()

                // Customer speaks initial opening statement
                val openingStatement = when (scenario.persona.emotionalState.lowercase()) {
                    "furious & belligerent", "furious" ->
                        "Finally I got through to a human being! Look, I am absolutely furious right now. I just opened my bill and see a ridiculous charge. Who authorized this?!"
                    "confused & highly anxious", "confused" ->
                        "Hello? Oh dear, I hope I reached the right place... my grandchildren are right here waiting and nothing is working on this screen. Can you help an old woman?"
                    "panicked & urgently demanding", "panicked" ->
                        "Listen to me very carefully: our enterprise fiber line just went completely dead, and I have venture capital investors logging in in 10 minutes! I need this fixed immediately!"
                    "sarcastic & thoroughly disgusted", "sarcastic" ->
                        "Let me guess, you're going to put me on hold for another 45 minutes and transfer me to nowhere? I've already spoken to three incompetent people today. Let me speak to your manager right now."
                    "distrustful & resigned to churn", "distrustful" ->
                        "Yes, I'm calling to cancel my account. I've been with you for 8 years and you hit me with a 35% price hike. NovaNet is offering half your price. Just cancel it."
                    else ->
                        "Hi, I'm calling regarding an issue on my account that needs urgent attention."
                }

                val firstTurn = CallTurn(
                    id = "turn_open",
                    speaker = Speaker.CUSTOMER,
                    message = openingStatement,
                    timestampMs = System.currentTimeMillis()
                )
                _uiState.update { it.copy(turns = listOf(firstTurn)) }
                speakCustomer(openingStatement)
            }
        }
    }

    fun sendAgentMessage(agentText: String) {
        val currentState = _uiState.value
        val scenario = currentState.scenario ?: return
        if (agentText.isBlank() || currentState.isGeneratingCustomerResponse) return

        val agentTurn = CallTurn(
            id = "agent_${System.currentTimeMillis()}",
            speaker = Speaker.AGENT,
            message = agentText.trim(),
            timestampMs = System.currentTimeMillis()
        )

        val updatedTurns = currentState.turns + agentTurn
        _uiState.update {
            it.copy(
                turns = updatedTurns,
                isGeneratingCustomerResponse = true,
                suggestedResponses = emptyList()
            )
        }

        viewModelScope.launch {
            try {
                val (customerReply, feedback) = simulationEngine.simulateNextCustomerTurn(
                    scenario = scenario,
                    history = updatedTurns,
                    agentLatestMessage = agentText,
                    currentAngerLevel = currentState.customerAngerLevel
                )

                val customerTurn = CallTurn(
                    id = "cust_${System.currentTimeMillis()}",
                    speaker = Speaker.CUSTOMER,
                    message = customerReply,
                    timestampMs = System.currentTimeMillis(),
                    sentimentFeedback = feedback
                )

                val newSuggestions = generateDynamicSuggestions(scenario, feedback.customerAngerLevel, feedback.whisperSuggestion)

                _uiState.update {
                    it.copy(
                        turns = it.turns + customerTurn,
                        customerAngerLevel = feedback.customerAngerLevel,
                        latestFeedback = feedback,
                        isGeneratingCustomerResponse = false,
                        suggestedResponses = newSuggestions
                    )
                }

                speakCustomer(customerReply)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in turn handling", e)
                _uiState.update {
                    it.copy(
                        isGeneratingCustomerResponse = false,
                        errorMessage = "Customer connection hiccup: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun toggleHold() {
        val currentHold = _uiState.value.isOnHold
        _uiState.update {
            it.copy(
                isOnHold = !currentHold,
                callState = if (!currentHold) CallState.ON_HOLD else CallState.CONNECTED
            )
        }
        if (!currentHold) {
            // Placing customer on hold
            val holdTurn = CallTurn(
                id = "hold_${System.currentTimeMillis()}",
                speaker = Speaker.AGENT,
                message = "[Agent placed call on hold to review account tools and consult tier-2 database]",
                timestampMs = System.currentTimeMillis()
            )
            _uiState.update { it.copy(turns = it.turns + holdTurn) }
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleTts() {
        val current = _uiState.value.isTtsEnabled
        _uiState.update { it.copy(isTtsEnabled = !current) }
        if (current) {
            textToSpeech?.stop()
        }
    }

    fun stopTts() {
        textToSpeech?.stop()
    }

    fun endCall() {
        stopTimer()
        textToSpeech?.stop()
        val state = _uiState.value
        val scenario = state.scenario ?: return
        val trainee = state.trainee ?: repository.getTrainees().first()

        _uiState.update { it.copy(callState = CallState.EVALUATING) }

        viewModelScope.launch {
            try {
                val evaluation = simulationEngine.generateComprehensiveEvaluation(
                    scenario = scenario,
                    history = state.turns,
                    durationSeconds = state.durationSeconds
                )

                // Save to Room DB
                val sessionEntity = CallSessionEntity(
                    traineeId = trainee.id,
                    traineeName = trainee.name,
                    scenarioId = scenario.id,
                    scenarioTitle = scenario.title,
                    category = scenario.category.name,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = state.durationSeconds,
                    overallScore = evaluation.overallScore,
                    letterGrade = evaluation.letterGrade,
                    outcome = evaluation.callOutcome,
                    deescalationScore = evaluation.deescalationScore,
                    empathyScore = evaluation.empathyScore,
                    toneScore = evaluation.toneProfessionalismScore,
                    retentionScore = evaluation.retentionScore,
                    salesScore = evaluation.salesScore,
                    protocolScore = evaluation.protocolAdherenceScore,
                    executiveSummary = evaluation.executiveSummary,
                    transcriptJson = CallSessionConverter.serializeTranscript(state.turns),
                    feedbackDetailsJson = CallSessionConverter.serializeEvaluation(evaluation),
                    managerNotes = ""
                )

                val newId = repository.saveSession(sessionEntity)

                _uiState.update {
                    it.copy(
                        callState = CallState.COMPLETED,
                        evaluation = evaluation,
                        savedSessionId = newId
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error evaluating call", e)
                _uiState.update {
                    it.copy(
                        callState = CallState.COMPLETED,
                        errorMessage = "Could not finalize evaluation: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearActiveCall() {
        stopTimer()
        textToSpeech?.stop()
        _uiState.value = ActiveCallUiState()
    }

    private fun speakCustomer(text: String) {
        if (_uiState.value.isTtsEnabled && isTtsReady) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cust_speech")
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun generateInitialSuggestions(scenario: CallScenario): List<String> {
        return when (scenario.category) {
            ScenarioCategory.BILLING_DISPUTE -> listOf(
                "I understand how alarming unexpected fees can be. Let me pull up your statement right now and take full ownership to resolve this.",
                "Thank you for bringing this to my attention. I will personally review this charge and make sure you are treated fairly.",
                "I completely get why you're upset. Let's look at the line items together so I can explain what occurred and apply the proper credits."
            )
            ScenarioCategory.RETENTION -> listOf(
                "First, I want to thank you for your long-standing loyalty. I can see you've been with us for years, and your business truly matters to us.",
                "I hear your frustration regarding the rate change. Before you make any final decisions to leave, let me see what exclusive loyalty programs we can lock in for you.",
                "You have every right to shop around for the best deal. Let me review your actual feature usage so we can tailor a plan that gives you the best price."
            )
            ScenarioCategory.DE_ESCALATION -> listOf(
                "I am so sorry for the runaround you've experienced today. I promise you that you will not be transferred again—I am handling this personally.",
                "You have been treated unfairly, and I take full accountability for getting this resolved for you right now.",
                "I hear how exhausting this has been. Please tell me what happened from your perspective, and I will take notes and take action."
            )
            ScenarioCategory.TECH_SUPPORT -> listOf(
                "Take your time, there is no rush at all. We are going to walk through this together one simple step at a time.",
                "I understand how critical this connection is for your company right now. Let me immediately check local nodes and activate your emergency failover.",
                "Don't worry about any confusing technical terms. I am right here with you and we'll have this working in no time."
            )
            ScenarioCategory.VIP_SERVICE -> listOf(
                "Good afternoon Mr. O'Connor, I recognize your Platinum status. Let me initiate our priority white-glove verification protocol immediately.",
                "We take your time and security with utmost priority. Let's do a direct visual biometric authentication so your team is unlocked within minutes.",
                "I am escalating this directly to our executive support bridge right now."
            )
            ScenarioCategory.SALES_AND_UPGRADE -> listOf(
                "I have verified your Spectrum Internet modem connection is clear and running at full speed. While I have your account pulled up, may I ask which wireless carrier you currently use for your smartphones?",
                "As a valued Spectrum Internet subscriber, you qualify for our Spectrum One offer: an Unlimited Mobile line for 12 months with Buy-One-Get-One-Free lines on the exact same leading 5G network.",
                "With over 15 devices active in your household, upgrading to Spectrum Internet Gig will give you 1,000 Mbps bandwidth and eliminate peak evening lag for less than $0.66 a day.",
                "With our Spectrum Mobile Contract Buyout program, we will pay off up to $500 per device on your existing carrier installment plan so your family can switch today with zero hassle."
            )
        }
    }

    private fun generateDynamicSuggestions(scenario: CallScenario, currentAnger: Int, hint: String): List<String> {
        val list = mutableListOf<String>()
        if (currentAnger > 70) {
            list.add("I hear exactly what you are saying, and you have every right to expect better from us. Here is what I am doing right now:")
            list.add("I apologize sincerely for the stress this has caused. Let me take immediate action to make this right.")
        } else if (currentAnger in 35..70) {
            list.add("To ensure you're completely satisfied, here is the resolution I can authorize today:")
            list.add("That is completely reasonable. Let's put this credit onto your account and verify the new balance together.")
        } else {
            list.add("I'm glad we could get this resolved smoothly for you. Is there anything else about your setup I can verify before we conclude?")
            list.add("Everything is now confirmed and updated in our system. You will receive an email confirmation with your credit details shortly.")
        }
        return list
    }

    private fun serializeTranscript(turns: List<CallTurn>): String {
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
                        put("prohibited", turn.sentimentFeedback.prohibitedPhraseUsed)
                        put("suggestion", turn.sentimentFeedback.whisperSuggestion)
                    })
                }
            })
        }
        return arr.toString()
    }

    private fun serializeEvaluation(ev: CallEvaluation): String {
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
        }.toString()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveCallUiState
import com.example.ui.viewmodel.CallSessionViewModel
import com.example.ui.viewmodel.CallState
import com.example.util.SpeechToTextManager
import com.example.util.VoiceInputState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSimScreen(
    viewModel: CallSessionViewModel,
    onCallFinished: () -> Unit,
    onCancelCall: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var agentInputText by remember { mutableStateOf("") }
    var showEndCallDialog by remember { mutableStateOf(false) }
    var showPersonaDetails by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Voice-to-Text State
    var isVoiceListening by remember { mutableStateOf(false) }
    var voiceRmsDb by remember { mutableFloatStateOf(0f) }
    var partialVoiceTranscript by remember { mutableStateOf("") }
    var voiceErrorNotice by remember { mutableStateOf<String?>(null) }

    val speechManager = remember { SpeechToTextManager(context) }

    DisposableEffect(speechManager) {
        speechManager.onStateChanged = { state ->
            when (state) {
                is VoiceInputState.Listening -> {
                    isVoiceListening = true
                    voiceRmsDb = state.rmsDb
                    if (state.partialText.isNotEmpty()) {
                        partialVoiceTranscript = state.partialText
                        agentInputText = state.partialText
                    }
                }
                is VoiceInputState.Success -> {
                    isVoiceListening = false
                    partialVoiceTranscript = state.recognizedText
                    agentInputText = state.recognizedText
                }
                is VoiceInputState.Error -> {
                    isVoiceListening = false
                    voiceErrorNotice = state.message
                }
                is VoiceInputState.Idle -> {
                    isVoiceListening = false
                }
                is VoiceInputState.Initializing -> {}
            }
        }
        speechManager.onPartialTranscript = { partial ->
            partialVoiceTranscript = partial
            agentInputText = partial
        }
        speechManager.onFinalTranscript = { finalResult ->
            partialVoiceTranscript = finalResult
            agentInputText = finalResult
            isVoiceListening = false
        }

        onDispose {
            speechManager.destroy()
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.stopTts()
            isVoiceListening = true
            voiceErrorNotice = null
            partialVoiceTranscript = ""
            speechManager.startListening()
        } else {
            showPermissionDeniedDialog = true
        }
    }

    fun handleVoiceToggle() {
        if (isVoiceListening) {
            speechManager.stopListening()
            isVoiceListening = false
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                viewModel.stopTts()
                isVoiceListening = true
                voiceErrorNotice = null
                partialVoiceTranscript = ""
                speechManager.startListening()
            } else {
                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Auto-cancel voice input when call completes or transitions
    LaunchedEffect(uiState.callState) {
        if (uiState.callState != CallState.CONNECTED && isVoiceListening) {
            speechManager.cancel()
            isVoiceListening = false
        }
    }

    // Auto-scroll to latest turn
    LaunchedEffect(uiState.turns.size) {
        if (uiState.turns.isNotEmpty()) {
            listState.animateScrollToItem(uiState.turns.size - 1)
        }
    }

    // When call evaluates and finishes, navigate to scorecard
    LaunchedEffect(uiState.callState) {
        if (uiState.callState == CallState.COMPLETED) {
            onCallFinished()
        }
    }

    val scenario = uiState.scenario

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = scenario?.title ?: "Mock Call Simulation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState.callState) {
                                            CallState.CONNECTED -> EmeraldSuccess
                                            CallState.ON_HOLD -> AmberWarning
                                            CallState.RINGING -> CyanGlow
                                            else -> Slate400
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (uiState.callState) {
                                    CallState.RINGING -> "Incoming Call..."
                                    CallState.CONNECTED -> formatTimer(uiState.durationSeconds)
                                    CallState.ON_HOLD -> "Call On Hold (${formatTimer(uiState.durationSeconds)})"
                                    CallState.EVALUATING -> "AI Evaluating Performance..."
                                    else -> "Disconnected"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Persona info sheet toggle
                    IconButton(
                        onClick = { showPersonaDetails = !showPersonaDetails },
                        modifier = Modifier.testTag("toggle_caller_brief_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactSupport,
                            contentDescription = "Caller Info",
                            tint = CyanGlow
                        )
                    }

                    // TTS Toggle
                    IconButton(
                        onClick = { viewModel.toggleTts() },
                        modifier = Modifier.testTag("toggle_tts_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Text to Speech Voice",
                            tint = if (uiState.isTtsEnabled) EmeraldSuccess else Slate400
                        )
                    }

                    // End Call button
                    IconButton(
                        onClick = { showEndCallDialog = true },
                        modifier = Modifier.testTag("end_call_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = CrimsonEscalated
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. Customer Emotion & Escalation Gauge
                CustomerEscalationMeter(
                    angerLevel = uiState.customerAngerLevel,
                    personaName = scenario?.persona?.name ?: "Customer",
                    emotionalState = scenario?.persona?.emotionalState ?: "Active",
                    isSpeaking = uiState.isGeneratingCustomerResponse
                )

                // 2. Expandable Caller Background Details
                AnimatedVisibility(visible = showPersonaDetails && scenario != null) {
                    CallerDetailsBanner(
                        scenario = scenario!!,
                        onDismiss = { showPersonaDetails = false }
                    )
                }

                // 3. Real-Time AI Whisper Coaching Card
                uiState.latestFeedback?.let { feedback ->
                    WhisperCoachingCard(feedback = feedback)
                }

                // 4. Conversation Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.turns, key = { it.id }) { turn ->
                        CallTurnBubble(turn = turn)
                    }

                    if (uiState.isGeneratingCustomerResponse) {
                        item {
                            TypingIndicatorBubble(customerName = scenario?.persona?.name ?: "Customer")
                        }
                    }
                }

                // 5. Suggested Dynamic Responses (One-Tap Co-Pilot)
                if (uiState.suggestedResponses.isNotEmpty() && !uiState.isGeneratingCustomerResponse) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.suggestedResponses) { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.sendAgentMessage(suggestion)
                                },
                                label = {
                                    Text(
                                        text = suggestion,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // 6. Action Control Bar (Hold, Mute, End)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.toggleHold() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isOnHold) AmberContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("hold_call_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isOnHold) "Resume" else "Hold", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.toggleMute() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isMuted) CrimsonContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("mute_call_button")
                        ) {
                            Icon(if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isMuted) "Muted" else "Mute", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { showEndCallDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonEscalated),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("end_and_evaluate_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("End Call & Score", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Voice Error Alert Notice
                AnimatedVisibility(visible = voiceErrorNotice != null) {
                    voiceErrorNotice?.let { error ->
                        Surface(
                            color = AmberContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AmberWarning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = error,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { voiceErrorNotice = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Voice Mode Active Listening Banner
                AnimatedVisibility(visible = isVoiceListening) {
                    VoiceInputActiveBanner(
                        partialText = partialVoiceTranscript,
                        rmsDb = voiceRmsDb,
                        scenarioCustomerName = scenario?.persona?.name ?: "Customer",
                        onSend = {
                            if (partialVoiceTranscript.isNotBlank()) {
                                viewModel.sendAgentMessage(partialVoiceTranscript)
                                partialVoiceTranscript = ""
                                agentInputText = ""
                            }
                            speechManager.stopListening()
                            isVoiceListening = false
                        },
                        onCancel = {
                            speechManager.cancel()
                            isVoiceListening = false
                        }
                    )
                }

                // 7. Input Bar (Voice Mic + Text + Send)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = agentInputText,
                        onValueChange = { agentInputText = it },
                        placeholder = {
                            Text(
                                text = if (isVoiceListening) "Listening... speak now" else "Speak or type response to customer...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("agent_response_input"),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3,
                        enabled = !uiState.isGeneratingCustomerResponse && uiState.callState != CallState.EVALUATING
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Voice-to-Text Microphone Button
                    val micBackground by animateColorAsState(
                        targetValue = if (isVoiceListening) CrimsonEscalated else MaterialTheme.colorScheme.surfaceVariant,
                        label = "mic_bg"
                    )

                    IconButton(
                        onClick = { handleVoiceToggle() },
                        enabled = !uiState.isGeneratingCustomerResponse && uiState.callState != CallState.EVALUATING,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(micBackground)
                            .testTag("voice_input_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = if (isVoiceListening) "Stop voice input" else "Speak response via microphone",
                            tint = if (isVoiceListening) Color.White else CyanGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (isVoiceListening) {
                                speechManager.stopListening()
                                isVoiceListening = false
                            }
                            if (agentInputText.isNotBlank()) {
                                viewModel.sendAgentMessage(agentInputText)
                                agentInputText = ""
                                partialVoiceTranscript = ""
                            }
                        },
                        enabled = agentInputText.isNotBlank() && !uiState.isGeneratingCustomerResponse,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (agentInputText.isNotBlank() && !uiState.isGeneratingCustomerResponse)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                            .testTag("send_agent_response_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Evaluating Loading Overlay
            if (uiState.callState == CallState.EVALUATING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = CyanGlow)
                            Text(
                                "Generating AI Scorecard...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Analyzing empathy, tone, de-escalation keywords, and company service protocols against full transcript.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // End Call Confirmation Dialog
    if (showEndCallDialog) {
        AlertDialog(
            onDismissRequest = { showEndCallDialog = false },
            title = { Text("End Mock Call?") },
            text = { Text("Are you ready to conclude this call? The AI will immediately analyze your transcript and generate your comprehensive performance evaluation.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndCallDialog = false
                        viewModel.endCall()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonEscalated),
                    modifier = Modifier.testTag("confirm_end_call_button")
                ) {
                    Text("End & Evaluate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndCallDialog = false }) {
                    Text("Continue Call")
                }
            }
        )
    }

    // Microphone Permission Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyanGlow
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Microphone Permission", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "CallCoach AI uses your device microphone so you can practice speaking responses aloud to customers in real time. Please allow audio recording permission to enable voice simulation, or continue by typing.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDeniedDialog = false
                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enable Microphone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Type Instead")
                }
            }
        )
    }
}

@Composable
fun CustomerEscalationMeter(
    angerLevel: Int,
    personaName: String,
    emotionalState: String,
    isSpeaking: Boolean
) {
    val barColor by animateColorAsState(
        targetValue = when {
            angerLevel > 70 -> CrimsonEscalated
            angerLevel in 40..70 -> AmberWarning
            else -> EmeraldSuccess
        },
        label = "anger_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = personaName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $emotionalState",
                    fontSize = 12.sp,
                    color = barColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Anger: $angerLevel%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { angerLevel / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun WhisperCoachingCard(feedback: LiveSentimentFeedback) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "AI Whisper Coach",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanGlow
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tone: ${feedback.tone}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Empathy: ${feedback.empathyScore}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (feedback.empathyScore >= 70) EmeraldSuccess else AmberWarning
                    )
                }
            }

            // Prohibited phrase warning if fired
            feedback.prohibitedPhraseUsed?.let { taboo ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonEscalated, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Trigger Alert: $taboo (caused escalation)",
                        fontSize = 11.sp,
                        color = CrimsonEscalated,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = feedback.whisperSuggestion,
                fontSize = 12.sp,
                color = Slate100,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CallTurnBubble(turn: CallTurn) {
    val isAgent = turn.speaker == Speaker.AGENT

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAgent) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isAgent) "You (Agent)" else "Customer",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAgent) IndigoLight else CyanGlow
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatTimestamp(turn.timestampMs),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isAgent) 14.dp else 2.dp,
                        bottomEnd = if (isAgent) 2.dp else 14.dp
                    )
                )
                .background(
                    if (isAgent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = turn.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAgent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TypingIndicatorBubble(customerName: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = CyanGlow
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$customerName is speaking...",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun CallerDetailsBanner(
    scenario: CallScenario,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Caller Dossier & Target Objectives", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanGlow)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(16.dp))
                }
            }

            Text("Reason: ${scenario.persona.reasonForCalling}", fontSize = 12.sp, color = Slate100)
            Text("Desired Outcome: ${scenario.persona.desiredOutcome}", fontSize = 12.sp, color = EmeraldSuccess)
            Text("Trigger Words to Avoid: ${scenario.persona.triggerWords.joinToString()}", fontSize = 11.sp, color = CrimsonEscalated)
            Text("Objectives: ${scenario.targetObjectives.joinToString { it.title }}", fontSize = 11.sp, color = Slate400)
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

private fun formatTimestamp(timeMs: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timeMs))
}

@Composable
fun AudioWaveformIndicator(
    isListening: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_val"
    )

    val normalizedRms = ((rmsDb + 2f) / 12f).coerceIn(0.15f, 1f)

    Row(
        modifier = modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 5
        for (i in 0 until barCount) {
            val factor = when (i) {
                0, 4 -> 0.45f
                1, 3 -> 0.75f
                else -> 1.0f
            }
            val dynamicHeight = if (isListening) {
                (24 * normalizedRms * factor * pulse).coerceIn(4f, 24f).dp
            } else {
                4.dp
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isListening) CrimsonEscalated else CyanGlow)
            )
        }
    }
}

@Composable
fun VoiceInputActiveBanner(
    partialText: String,
    rmsDb: Float,
    scenarioCustomerName: String,
    onSend: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CrimsonEscalated)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Voice Input Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonEscalated
                    )
                }

                AudioWaveformIndicator(
                    isListening = true,
                    rmsDb = rmsDb
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (partialText.isNotBlank()) "\"$partialText\"" else "Listening... Speak your response naturally to $scenarioCustomerName",
                fontSize = 13.sp,
                color = if (partialText.isNotBlank()) Slate100 else Slate400,
                fontStyle = if (partialText.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSend,
                    enabled = partialText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Spoken Response", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


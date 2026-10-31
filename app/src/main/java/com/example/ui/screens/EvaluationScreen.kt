package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CallSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationScreen(
    callSessionViewModel: CallSessionViewModel,
    onRetryScenario: () -> Unit,
    onBackToScenarios: () -> Unit,
    onViewManagerAnalytics: () -> Unit,
    onViewHistory: () -> Unit
) {
    val uiState by callSessionViewModel.uiState.collectAsStateWithLifecycle()
    val evaluation = uiState.evaluation
    var showFullTranscript by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Call Scorecard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(uiState.scenario?.title ?: "Simulation QA Report", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToScenarios, modifier = Modifier.testTag("evaluation_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onViewHistory, modifier = Modifier.testTag("evaluation_to_history_button")) {
                        Icon(Icons.Default.History, contentDescription = "Call History", tint = CyanGlow)
                    }
                    IconButton(onClick = onViewManagerAnalytics, modifier = Modifier.testTag("evaluation_to_analytics_button")) {
                        Icon(Icons.Default.Insights, contentDescription = "Manager Analytics", tint = IndigoLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetryScenario,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("retry_scenario_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry")
                    }

                    OutlinedButton(
                        onClick = onViewHistory,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("evaluation_bottom_history_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History")
                    }

                    Button(
                        onClick = onBackToScenarios,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("back_to_scenarios_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (evaluation == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                // 1. Overall Score Hero Card
                item {
                    ScoreHeroCard(evaluation = evaluation)
                }

                // 2. Executive Manager Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Executive QA Evaluation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyanGlow)
                            }
                            Text(
                                text = evaluation.executiveSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 3. Dimensional Scoring Breakdown
                item {
                    DimensionScoresCard(evaluation = evaluation)
                }

                // 4. Strengths & Improvements Grid
                item {
                    StrengthsAndImprovementsCard(evaluation = evaluation)
                }

                // 5. Target Objectives Pass/Fail
                if (evaluation.objectiveResults.isNotEmpty()) {
                    item {
                        ObjectivesResultsCard(results = evaluation.objectiveResults)
                    }
                }

                // 6. Targeted Debriefing Questions Answers
                if (evaluation.debriefEvaluations.isNotEmpty()) {
                    item {
                        DebriefEvaluationsCard(debriefs = evaluation.debriefEvaluations)
                    }
                }

                // 7. Recommended Training Focus
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Recommended Training Focus", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                                Text(evaluation.recommendedTrainingFocus, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate100)
                            }
                        }
                    }
                }

                // 8. Full Annotated Transcript (Toggleable)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFullTranscript = !showFullTranscript },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Full Call Transcript (${uiState.turns.size} turns)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Icon(
                                    if (showFullTranscript) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = showFullTranscript) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.turns.forEach { turn ->
                                        TranscriptTurnItem(turn = turn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreHeroCard(evaluation: CallEvaluation) {
    val gradeColor = when (evaluation.letterGrade) {
        "A+", "A" -> EmeraldSuccess
        "B" -> CyanGlow
        "C" -> AmberWarning
        else -> CrimsonEscalated
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Overall Performance", fontSize = 13.sp, color = Slate400)
                    Text(
                        text = evaluation.callOutcome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = gradeColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(gradeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = evaluation.letterGrade,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = gradeColor
                        )
                    }
                }
            }

            Divider(color = Slate800)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "QA Score", value = "${evaluation.overallScore}/100", color = gradeColor)
                StatColumn(label = "De-escalation", value = "${evaluation.deescalationScore}%", color = EmeraldSuccess)
                StatColumn(label = "Empathy", value = "${evaluation.empathyScore}%", color = CyanGlow)
                StatColumn(label = "Retention", value = "${evaluation.retentionScore}%", color = AmberWarning)
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Slate400)
    }
}

@Composable
fun DimensionScoresCard(evaluation: CallEvaluation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Competency Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            DimensionBar("De-escalation Technique", evaluation.deescalationScore, EmeraldSuccess)
            DimensionBar("Empathy & Active Listening", evaluation.empathyScore, CyanGlow)
            DimensionBar("Tone & Professionalism", evaluation.toneProfessionalismScore, IndigoLight)
            DimensionBar("Retention Strategy Execution", evaluation.retentionScore, AmberWarning)
            if (evaluation.salesScore > 0) {
                DimensionBar("Spectrum Sales & Value Pitching", evaluation.salesScore, IndigoLight)
            }
            DimensionBar("Service Protocol Adherence", evaluation.protocolAdherenceScore, CyanAccent)
        }
    }
}

@Composable
fun DimensionBar(label: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("$score%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun StrengthsAndImprovementsCard(evaluation: CallEvaluation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Strengths
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Key Strengths", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                }
                evaluation.keyStrengths.forEach { str ->
                    Text("• $str", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline)

            // Improvements
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Areas for Improvement", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberWarning)
                }
                evaluation.areasForImprovement.forEach { imp ->
                    Text("• $imp", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ObjectivesResultsCard(results: List<ObjectiveResult>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Stated Performance Objectives", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            results.forEach { res ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = if (res.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (res.passed) EmeraldSuccess else CrimsonEscalated,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(res.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(res.feedback, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun DebriefEvaluationsCard(debriefs: List<DebriefAnswerEvaluation>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Debriefing Questions Review", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            debriefs.forEach { dq ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dq.question, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Surface(
                            color = when (dq.rating.lowercase()) {
                                "exemplary" -> EmeraldContainer
                                "adequate" -> AmberContainer
                                else -> CrimsonContainer
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = dq.rating,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dq.evaluation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TranscriptTurnItem(turn: CallTurn) {
    val isAgent = turn.speaker == Speaker.AGENT
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isAgent) Slate800 else Slate900, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isAgent) "Agent" else "Customer",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isAgent) IndigoLight else CyanGlow
            )
            turn.sentimentFeedback?.let { fb ->
                Text(
                    text = "Empathy: ${fb.empathyScore}% | Tone: ${fb.tone}",
                    fontSize = 10.sp,
                    color = if (fb.empathyScore >= 70) EmeraldSuccess else AmberWarning
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(turn.message, fontSize = 12.sp, color = Slate100)
    }
}

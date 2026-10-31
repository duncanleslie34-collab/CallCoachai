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
import com.example.data.model.HistoricalCallReport
import com.example.data.model.Speaker
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalSessionDetailScreen(
    report: HistoricalCallReport?,
    onNavigateBack: () -> Unit,
    onRetryScenario: (scenarioId: String) -> Unit,
    onSaveManagerNote: (sessionId: Long, note: String) -> Unit,
    onDeleteSession: (sessionId: Long) -> Unit
) {
    if (report == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Session not found", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onNavigateBack) {
                    Text("Back to History")
                }
            }
        }
        return
    }

    val entity = report.entity
    val evaluation = report.evaluation
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember(entity.managerNotes) { mutableStateOf(entity.managerNotes) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTranscriptSection by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historical Call Scorecard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "${report.formattedDate} • ${report.formattedDuration}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("history_detail_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_session_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Session", tint = CrimsonEscalated)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditNoteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_coaching_note_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (entity.managerNotes.isBlank()) "Add Note" else "Edit Note")
                    }

                    Button(
                        onClick = { onRetryScenario(entity.scenarioId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("history_practice_again_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Practice Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = entity.category.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(report.formattedDuration, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Text(
                            text = entity.scenarioTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trainee: ${entity.traineeName}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Score Hero Card
            item {
                ScoreHeroCard(evaluation = evaluation)
            }

            // Manager Personalized Coaching Notes Section
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (entity.managerNotes.isNotBlank()) Slate900 else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manager Coaching Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberWarning)
                            }
                            IconButton(
                                onClick = { showEditNoteDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Note", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (entity.managerNotes.isNotBlank()) {
                            Text(
                                text = entity.managerNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate100
                            )
                        } else {
                            Text(
                                text = "No coaching notes provided yet. Tap 'Edit Note' to add personalized feedback for this session.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Executive Summary
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

            // Dimensional Scoring Breakdown
            item {
                DimensionScoresCard(evaluation = evaluation)
            }

            // Strengths & Improvements
            item {
                StrengthsAndImprovementsCard(evaluation = evaluation)
            }

            // Target Objectives Pass/Fail (if present)
            if (evaluation.objectiveResults.isNotEmpty()) {
                item {
                    ObjectivesResultsCard(results = evaluation.objectiveResults)
                }
            }

            // Debrief Evaluations (if present)
            if (evaluation.debriefEvaluations.isNotEmpty()) {
                item {
                    DebriefEvaluationsCard(debriefs = evaluation.debriefEvaluations)
                }
            }

            // Recommended Training Focus
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
                            Text("Recommended Next Training Focus", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Text(evaluation.recommendedTrainingFocus, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        }
                    }
                }
            }

            // Full Call Transcript
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
                                .clickable { showTranscriptSection = !showTranscriptSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Full Call Transcript (${report.turns.size} turns)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Icon(
                                if (showTranscriptSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(visible = showTranscriptSection) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (report.turns.isEmpty()) {
                                    Text(
                                        text = "No recorded turns in this session.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    report.turns.forEach { turn ->
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

    // Edit Coaching Note Dialog
    if (showEditNoteDialog) {
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Manager Coaching Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Add personalized guidance, praise, or action items for this mock call session.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = { Text("Enter feedback...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_note_input"),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveManagerNote(entity.id, noteInput.trim())
                        showEditNoteDialog = false
                    },
                    modifier = Modifier.testTag("save_dialog_note_button")
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Call Session?") },
            text = {
                Text("Are you sure you want to delete this recorded call session from history? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteSession(entity.id)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonEscalated),
                    modifier = Modifier.testTag("confirm_delete_session_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

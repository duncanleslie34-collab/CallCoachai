package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.CallSessionEntity
import com.example.data.local.ManagerCoachingNoteEntity
import com.example.data.model.TraineeProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.ManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerAnalyticsScreen(
    managerViewModel: ManagerViewModel,
    onNavigateBack: () -> Unit,
    onOpenSessionDetail: ((Long) -> Unit)? = null
) {
    val uiState by managerViewModel.uiState.collectAsStateWithLifecycle()
    val sessions by managerViewModel.sessions.collectAsStateWithLifecycle()
    val analytics by managerViewModel.analytics.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Performance Metrics", "Call History", "Coaching Notes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Manager Analytics Suite", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = if (uiState.isAllTraineesView) "Team-wide Performance Overview" else "${uiState.selectedTrainee?.name} (${uiState.selectedTrainee?.role})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("manager_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Trainee Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.isAllTraineesView,
                        onClick = { managerViewModel.setAllTraineesView(true) },
                        label = { Text("All Trainees") },
                        leadingIcon = {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                items(uiState.trainees, key = { it.id }) { trainee ->
                    FilterChip(
                        selected = !uiState.isAllTraineesView && uiState.selectedTrainee?.id == trainee.id,
                        onClick = { managerViewModel.selectTrainee(trainee) },
                        label = { Text(trainee.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(trainee.avatarColorHex))
                            )
                        }
                    )
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                    )
                }
            }

            // Tab Content
            when (activeTab) {
                0 -> PerformanceMetricsTab(analytics = analytics, sessions = sessions)
                1 -> CallHistoryTab(sessions = sessions, managerViewModel = managerViewModel, onOpenSessionDetail = onOpenSessionDetail)
                2 -> CoachingNotesTab(managerViewModel = managerViewModel)
            }
        }
    }
}

@Composable
fun PerformanceMetricsTab(
    analytics: com.example.ui.viewmodel.TraineeAnalytics,
    sessions: List<CallSessionEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // At-Risk Alert Banner if applicable
        analytics.atRiskWarning?.let { alert ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonEscalated, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Manager Coaching Alert", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(alert, color = Slate100, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4 KPI Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Avg QA Score",
                    value = "${analytics.averageScore}%",
                    icon = Icons.Default.Verified,
                    color = if (analytics.averageScore >= 80) EmeraldSuccess else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "De-escalation",
                    value = "${analytics.deescalationSuccessRate}%",
                    icon = Icons.Default.HealthAndSafety,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Retention Rate",
                    value = "${analytics.retentionSuccessRate}%",
                    icon = Icons.Default.Repeat,
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Sales Conversion",
                    value = "${analytics.salesConversionRate}%",
                    icon = Icons.Default.TrendingUp,
                    color = IndigoLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Calls Completed",
                    value = "${analytics.totalCalls}",
                    icon = Icons.Default.Call,
                    color = CyanGlow,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Top QA Score",
                    value = "${analytics.highestScore}%",
                    icon = Icons.Default.EmojiEvents,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Competency Averages
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Team Mastery Benchmarks", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    BenchmarkRow("De-escalation Pacing", analytics.averageDeescalation, EmeraldSuccess)
                    BenchmarkRow("Empathy & Active Listening", analytics.averageEmpathy, CyanGlow)
                    BenchmarkRow("Retention Value Ladder", analytics.averageRetention, AmberWarning)
                    BenchmarkRow("Spectrum Sales & Value Pitching", analytics.averageSales, IndigoLight)
                    BenchmarkRow("Policy & Protocol Adherence", analytics.averageProtocol, CyanAccent)
                }
            }
        }

        // Recent Activity summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Executive Coaching Recommendation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyanGlow)
                    Text(
                        if (analytics.totalCalls == 0)
                            "No mock calls completed yet. Assign initial billing dispute and de-escalation scenarios to generate baselines."
                        else if (analytics.averageDeescalation < 75)
                            "Prioritize high-rage simulation modules with Marcus Vance and Elena Rostova. Emphasize first-turn radical ownership."
                        else
                            "Trainees are meeting benchmark standards. Recommend transitioning to advanced corporate VIP negotiations and retention objections.",
                        fontSize = 13.sp,
                        color = Slate100
                    )
                }
            }
        }
    }
}

@Composable
fun BenchmarkRow(label: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp)
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
fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun CallHistoryTab(
    sessions: List<CallSessionEntity>,
    managerViewModel: ManagerViewModel,
    onOpenSessionDetail: ((Long) -> Unit)? = null
) {
    var selectedSession by remember { mutableStateOf<CallSessionEntity?>(null) }
    var noteInput by remember { mutableStateOf("") }

    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PhoneMissed, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No Call Sessions Recorded", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Complete a mock call to view detailed QA logs here.", fontSize = 12.sp, color = Slate400)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions, key = { it.id }) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSession = session },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(session.traineeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                color = when (session.letterGrade) {
                                    "A+", "A" -> EmeraldContainer
                                    "B" -> Color(0xFF1E3A8A)
                                    "C" -> AmberContainer
                                    else -> CrimsonContainer
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${session.overallScore}% (${session.letterGrade})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(session.scenarioTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(session.outcome, fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.Medium)
                            Text(formatDate(session.timestamp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (session.managerNotes.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Manager Note: ${session.managerNotes}",
                                    fontSize = 11.sp,
                                    color = CyanGlow,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Inspect Session Modal
    selectedSession?.let { session ->
        AlertDialog(
            onDismissRequest = { selectedSession = null },
            title = { Text(session.scenarioTitle) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Trainee: ${session.traineeName}", fontWeight = FontWeight.Bold)
                    Text("QA Score: ${session.overallScore}% | Outcome: ${session.outcome}")
                    Text(session.executiveSummary, style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = noteInput.ifBlank { session.managerNotes },
                        onValueChange = { noteInput = it },
                        label = { Text("Manager Personalized Coaching Note") },
                        placeholder = { Text("Add coaching feedback...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onOpenSessionDetail != null) {
                        OutlinedButton(onClick = {
                            val id = session.id
                            selectedSession = null
                            onOpenSessionDetail(id)
                        }) {
                            Text("Full Report")
                        }
                    }
                    Button(onClick = {
                        if (noteInput.isNotBlank()) {
                            managerViewModel.updateSessionNotes(session.id, noteInput)
                        }
                        selectedSession = null
                    }) {
                        Text("Save Note")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSession = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CoachingNotesTab(managerViewModel: ManagerViewModel) {
    val uiState by managerViewModel.uiState.collectAsStateWithLifecycle()
    val notesFlow = remember(uiState.selectedTrainee?.id) {
        managerViewModel.getNotesForSelectedTrainee()
    }
    val coachingNotes by notesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val targetAreas = listOf("De-escalation & Trigger Words", "Retention Value Ladder", "Technical Pacing", "Tone & Empathy")
    val priorities = listOf("High", "Action Item", "Commendation")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Add Note Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Personalized Coaching Note", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyanGlow)

                    OutlinedTextField(
                        value = uiState.newNoteText,
                        onValueChange = { managerViewModel.updateNewNoteText(it) },
                        placeholder = { Text("Write targeted feedback for ${uiState.selectedTrainee?.name ?: "trainee"}...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coaching_note_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    // Priority Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { prio ->
                            FilterChip(
                                selected = uiState.newNotePriority == prio,
                                onClick = { managerViewModel.updateNewNotePriority(prio) },
                                label = { Text(prio, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { managerViewModel.addCoachingNote() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_coaching_note_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Coaching Note", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Notes Header
        item {
            Text(
                text = "Active Coaching Notes (${coachingNotes.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(coachingNotes, key = { it.id }) { note ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (note.priority) {
                                "High" -> CrimsonContainer
                                "Commendation" -> EmeraldContainer
                                else -> AmberContainer
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = note.priority,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { managerViewModel.deleteCoachingNote(note.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(note.note, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(note.targetArea, fontSize = 11.sp, color = CyanGlow, fontWeight = FontWeight.Medium)
                        Text(formatDate(note.timestamp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun formatDate(timeMs: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timeMs))
}

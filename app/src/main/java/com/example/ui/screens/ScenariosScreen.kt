package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScenarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenariosScreen(
    scenarioViewModel: ScenarioViewModel,
    onStartCall: (CallScenario) -> Unit,
    onOpenManagerDashboard: () -> Unit,
    onCreateCustomModule: () -> Unit,
    onOpenTechniquesGuide: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val scenarios by scenarioViewModel.scenarios.collectAsStateWithLifecycle()
    val filterState by scenarioViewModel.filterState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Spectrum CallCoach AI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("De-escalation, Retention & Sales Training Lab", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenLeaderboard,
                        modifier = Modifier.testTag("nav_leaderboard_button")
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Team Leaderboard", tint = Color(0xFFF59E0B))
                    }
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Call History", tint = CyanGlow)
                    }
                    IconButton(
                        onClick = onOpenTechniquesGuide,
                        modifier = Modifier.testTag("nav_techniques_guide_button")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Training Guide", tint = CyanGlow)
                    }
                    IconButton(
                        onClick = onOpenManagerDashboard,
                        modifier = Modifier.testTag("nav_manager_analytics_button")
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = "Manager Analytics", tint = IndigoLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateCustomModule,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Module", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_custom_module_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Leaderboard & Standings Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenLeaderboard() }
                    .testTag("scenarios_leaderboard_banner"),
                color = Slate900,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Spectrum QA Leaderboard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "View Top Agent Scores, Trophies & Standings",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View Standings",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { scenarioViewModel.setSearchQuery(it) },
                placeholder = { Text("Search personas, scenarios, or keywords...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filterState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { scenarioViewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("scenario_search_input")
            )

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedCategory == null,
                        onClick = { scenarioViewModel.setCategoryFilter(null) },
                        label = { Text("All Scenarios") }
                    )
                }
                items(ScenarioCategory.values()) { cat ->
                    FilterChip(
                        selected = filterState.selectedCategory == cat,
                        onClick = {
                            scenarioViewModel.setCategoryFilter(
                                if (filterState.selectedCategory == cat) null else cat
                            )
                        },
                        label = { Text(cat.displayName) }
                    )
                }
            }

            // Quick Access History Strip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenHistory)
                    .testTag("scenarios_history_banner"),
                color = Slate900
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Past Call History & Scorecards", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate100)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Logs", fontSize = 11.sp, color = CyanGlow, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Scenarios Count Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${scenarios.size} Training Scenarios Available",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "10 Personas + Custom",
                    fontSize = 12.sp,
                    color = CyanGlow,
                    fontWeight = FontWeight.Medium
                )
            }

            // Scenario List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(scenarios, key = { it.id }) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onStartCall = { onStartCall(scenario) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: CallScenario,
    onStartCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scenario_card_${scenario.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge + Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (scenario.category) {
                        ScenarioCategory.DE_ESCALATION -> CrimsonContainer
                        ScenarioCategory.RETENTION -> AmberContainer
                        ScenarioCategory.SALES_AND_UPGRADE -> Color(0xFF3730A3)
                        ScenarioCategory.BILLING_DISPUTE -> Color(0xFF1E3A8A)
                        ScenarioCategory.TECH_SUPPORT -> Color(0xFF14532D)
                        ScenarioCategory.VIP_SERVICE -> Color(0xFF581C87)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = scenario.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (scenario.isCustom) {
                        Surface(
                            color = CyanAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Manager Custom", fontSize = 10.sp, color = CyanGlow, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = scenario.difficulty.displayName,
                        fontSize = 12.sp,
                        color = when (scenario.difficulty) {
                            DifficultyLevel.BEGINNER -> EmeraldSuccess
                            DifficultyLevel.INTERMEDIATE -> AmberWarning
                            DifficultyLevel.ADVANCED -> CrimsonEscalated
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Persona Snippet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when (scenario.persona.category) {
                                ScenarioCategory.DE_ESCALATION -> CrimsonEscalated
                                ScenarioCategory.RETENTION -> AmberWarning
                                else -> IndigoLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scenario.persona.avatarInitials,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = scenario.persona.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${scenario.persona.occupation})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Mood: ${scenario.persona.emotionalState}",
                        fontSize = 12.sp,
                        color = if (scenario.persona.initialAngerLevel > 70) CrimsonEscalated else AmberWarning,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Initial Hostility Indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${scenario.persona.initialAngerLevel}% Anger",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (scenario.persona.initialAngerLevel > 70) CrimsonEscalated else AmberWarning
                    )
                    LinearProgressIndicator(
                        progress = { scenario.persona.initialAngerLevel / 100f },
                        modifier = Modifier
                            .width(55.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (scenario.persona.initialAngerLevel > 70) CrimsonEscalated else AmberWarning,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scenario Brief
            Text(
                text = scenario.scenarioBrief,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Objectives counter & CTA Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${scenario.targetObjectives.size} Goals",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStartCall,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("start_mock_call_button_${scenario.id}")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Mock Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

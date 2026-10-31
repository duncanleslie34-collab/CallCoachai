package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LeaderboardUiState
import com.example.ui.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onNavigateBack: () -> Unit,
    onStartCall: () -> Unit,
    onViewAgentHistory: (traineeId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showTierInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Spectrum QA Leaderboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Call Center Excellence & Agent Rankings",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Scenarios"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTierInfoDialog = true },
                        modifier = Modifier.testTag("leaderboard_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tier Information",
                            tint = CyanGlow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Team Benchmark & Healthy Competition Hero Banner
            item {
                LeaderboardHeroCard(
                    state = state,
                    onStartCall = onStartCall
                )
            }

            // Section 2: Timeframe Filter Tabs
            item {
                TimeframeSelector(
                    selectedTimeframe = state.selectedTimeframe,
                    onSelectTimeframe = { viewModel.setTimeframe(it) }
                )
            }

            // Section 3: Metric Filter Chips
            item {
                MetricFilterChips(
                    selectedMetric = state.selectedMetric,
                    onSelectMetric = { viewModel.setMetric(it) }
                )
            }

            // Section 4: Top 3 Podium
            if (state.entries.isNotEmpty()) {
                item {
                    PodiumSection(
                        entries = state.entries,
                        selectedMetric = state.selectedMetric,
                        onAgentClick = { viewModel.selectAgentModal(it) }
                    )
                }
            }

            // Section 5: Header for Full Rankings
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agent Standings (${state.entries.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sorted by ${state.selectedMetric.shortLabel}",
                        fontSize = 12.sp,
                        color = CyanGlow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Section 6: Ranked List of All Agents
            items(state.entries, key = { it.traineeId }) { entry ->
                AgentLeaderboardCard(
                    entry = entry,
                    selectedMetric = state.selectedMetric,
                    onClick = { viewModel.selectAgentModal(entry) }
                )
            }

            // Section 7: Motivational Footer Note
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Spectrum Certified QA Ranking",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Rankings auto-update after each simulation. Top 3 monthly agents qualify for Advanced Tier-2 VIP and Leadership coaching.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Agent Detail dialog
    state.selectedAgentForModal?.let { agent ->
        AgentDetailDialog(
            entry = agent,
            onDismiss = { viewModel.selectAgentModal(null) },
            onViewHistory = {
                viewModel.selectAgentModal(null)
                onViewAgentHistory(agent.traineeId)
            },
            onChallenge = {
                viewModel.selectAgentModal(null)
                onStartCall()
            }
        )
    }

    // Modal: Tier Information dialog
    if (showTierInfoDialog) {
        TierInfoDialog(onDismiss = { showTierInfoDialog = false })
    }
}

@Composable
private fun LeaderboardHeroCard(
    state: LeaderboardUiState,
    onStartCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("leaderboard_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            IndigoPrimary.copy(alpha = 0.25f),
                            Slate900
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Team Performance Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Live QA benchmark & leaderboards",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Button(
                    onClick = onStartCall,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("leaderboard_start_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice Call",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat counters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800.copy(alpha = 0.7f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Team QA Avg", value = "${state.teamAverageScore}%", color = CyanGlow)
                StatDivider()
                StatColumn(label = "Top Score", value = "${state.topScore}%", color = EmeraldSuccess)
                StatDivider()
                StatColumn(label = "Simulations", value = "${state.totalTeamSimulations}", color = IndigoLight)
            }

            // Spotlight highlights
            if (state.weeklyMvp != null || state.mostImproved != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.weeklyMvp?.let { mvp ->
                        SpotlightChip(
                            icon = Icons.Default.Stars,
                            iconTint = Color(0xFFF59E0B),
                            title = "Weekly MVP",
                            name = "${mvp.name} (${mvp.overallAvg}%)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.mostImproved?.let { climber ->
                        SpotlightChip(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = EmeraldSuccess,
                            title = "Top Climber",
                            name = "${climber.name} (+${climber.trend}%)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = Slate400)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(Slate700)
    )
}

@Composable
private fun SpotlightChip(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Column {
            Text(text = title, fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TimeframeSelector(
    selectedTimeframe: LeaderboardTimeframe,
    onSelectTimeframe: (LeaderboardTimeframe) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LeaderboardTimeframe.values().forEach { tf ->
            val isSelected = tf == selectedTimeframe
            val containerColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor)
                    .clickable { onSelectTimeframe(tf) }
                    .padding(vertical = 10.dp)
                    .testTag("timeframe_${tf.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tf.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun MetricFilterChips(
    selectedMetric: LeaderboardMetric,
    onSelectMetric: (LeaderboardMetric) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(LeaderboardMetric.values()) { metric ->
            FilterChip(
                selected = metric == selectedMetric,
                onClick = { onSelectMetric(metric) },
                label = { Text(metric.label, fontSize = 12.sp) },
                leadingIcon = {
                    val icon = when (metric) {
                        LeaderboardMetric.OVERALL_QA -> Icons.Default.Leaderboard
                        LeaderboardMetric.EMPATHY -> Icons.Default.Favorite
                        LeaderboardMetric.DE_ESCALATION -> Icons.Default.Shield
                        LeaderboardMetric.SALES_RETENTION -> Icons.Default.LocalOffer
                        LeaderboardMetric.ACTIVITY -> Icons.Default.PhoneInTalk
                    }
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.testTag("metric_${metric.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun PodiumSection(
    entries: List<AgentLeaderboardEntry>,
    selectedMetric: LeaderboardMetric,
    onAgentClick: (AgentLeaderboardEntry) -> Unit
) {
    val first = entries.getOrNull(0)
    val second = entries.getOrNull(1)
    val third = entries.getOrNull(2)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("podium_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOP PERFORMERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = CyanGlow
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (second != null) {
                    PodiumSlot(
                        entry = second,
                        place = 2,
                        pedestalHeight = 110.dp,
                        trophyColor = Color(0xFFCBD5E1),
                        pedestalColor = Color(0xFF334155),
                        selectedMetric = selectedMetric,
                        onClick = { onAgentClick(second) }
                    )
                }

                // 1st Place (Gold - Center)
                if (first != null) {
                    PodiumSlot(
                        entry = first,
                        place = 1,
                        pedestalHeight = 145.dp,
                        trophyColor = Color(0xFFF59E0B),
                        pedestalColor = Color(0xFF78350F),
                        selectedMetric = selectedMetric,
                        onClick = { onAgentClick(first) }
                    )
                }

                // 3rd Place (Bronze)
                if (third != null) {
                    PodiumSlot(
                        entry = third,
                        place = 3,
                        pedestalHeight = 90.dp,
                        trophyColor = Color(0xFFD97706),
                        pedestalColor = Color(0xFF451A03),
                        selectedMetric = selectedMetric,
                        onClick = { onAgentClick(third) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: AgentLeaderboardEntry,
    place: Int,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    trophyColor: Color,
    pedestalColor: Color,
    selectedMetric: LeaderboardMetric,
    onClick: () -> Unit
) {
    val metricValue = when (selectedMetric) {
        LeaderboardMetric.OVERALL_QA -> "${entry.overallAvg}%"
        LeaderboardMetric.EMPATHY -> "${entry.empathyAvg}%"
        LeaderboardMetric.DE_ESCALATION -> "${entry.deescalationAvg}%"
        LeaderboardMetric.SALES_RETENTION -> "${entry.salesAvg}%"
        LeaderboardMetric.ACTIVITY -> "${entry.totalCalls} calls"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        // Crown or Trophy on 1st place
        if (place == 1) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = trophyColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = trophyColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(if (place == 1) 54.dp else 46.dp)
                .clip(CircleShape)
                .background(Color(entry.avatarColorHex))
                .border(
                    width = if (place == 1) 2.5.dp else 1.5.dp,
                    color = trophyColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.avatarInitials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (place == 1) 16.sp else 14.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.name.split(" ").firstOrNull() ?: entry.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = metricValue,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (place == 1) 15.sp else 13.sp,
            color = trophyColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal block
        Box(
            modifier = Modifier
                .width(86.dp)
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(pedestalColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$place",
                    fontWeight = FontWeight.Black,
                    fontSize = if (place == 1) 24.sp else 20.sp,
                    color = trophyColor.copy(alpha = 0.9f)
                )
                Text(
                    text = entry.tier.title,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AgentLeaderboardCard(
    entry: AgentLeaderboardEntry,
    selectedMetric: LeaderboardMetric,
    onClick: () -> Unit
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFF59E0B) // Gold
        2 -> Color(0xFFCBD5E1) // Silver
        3 -> Color(0xFFD97706) // Bronze
        else -> Slate400
    }

    val primaryDisplay = when (selectedMetric) {
        LeaderboardMetric.OVERALL_QA -> "${entry.overallAvg}% QA"
        LeaderboardMetric.EMPATHY -> "${entry.empathyAvg}% Empathy"
        LeaderboardMetric.DE_ESCALATION -> "${entry.deescalationAvg}% De-esc"
        LeaderboardMetric.SALES_RETENTION -> "${entry.salesAvg}% Sales"
        LeaderboardMetric.ACTIVITY -> "${entry.totalCalls} Calls"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
            .testTag("agent_card_${entry.traineeId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.15f))
                        .border(1.dp, rankColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = rankColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(entry.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Department
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Tier pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(entry.tier.colorHex).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = entry.tier.title.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(entry.tier.colorHex)
                            )
                        }
                    }
                    Text(
                        text = "${entry.role} • ${entry.department}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Primary score pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = primaryDisplay,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (entry.bestScore > 0) {
                        Text(
                            text = "Peak: ${entry.bestScore}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics and badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniMetricChip(label = "Emp", score = entry.empathyAvg)
                    MiniMetricChip(label = "De-esc", score = entry.deescalationAvg)
                    MiniMetricChip(label = "Sales", score = entry.salesAvg)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entry.streak >= 2) {
                        Text(
                            text = "🔥 ${entry.streak}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                    Text(
                        text = "${entry.winRate}% Win",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = EmeraldSuccess
                    )
                    Text(
                        text = "${entry.totalCalls} calls",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entry.badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(entry.badges) { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMetricChip(label: String, score: Int) {
    val color = when {
        score >= 90 -> EmeraldSuccess
        score >= 80 -> CyanGlow
        score >= 70 -> AmberWarning
        score > 0 -> CrimsonEscalated
        else -> Slate400
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: $score%",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AgentDetailDialog(
    entry: AgentLeaderboardEntry,
    onDismiss: () -> Unit,
    onViewHistory: () -> Unit,
    onChallenge: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(entry.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(text = entry.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${entry.role} • Rank #${entry.rank}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tier and summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Current Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(entry.tier.colorHex).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${entry.tier.title} Tier (${entry.overallAvg}% QA)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(entry.tier.colorHex)
                        )
                    }
                }

                HorizontalDivider()

                // Performance Matrix Grid
                Text(text = "Performance Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerformanceRow("Empathy & Active Listening", entry.empathyAvg)
                    PerformanceRow("De-escalation & Trigger Avoidance", entry.deescalationAvg)
                    PerformanceRow("Sales & Retention Conversion", entry.salesAvg)
                    PerformanceRow("Spectrum Service Protocol", entry.protocolAvg)
                }

                HorizontalDivider()

                // Stats overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Simulations", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${entry.totalCalls} calls", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Save/Win Rate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${entry.winRate}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldSuccess)
                    }
                    Column {
                        Text("Active Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("🔥 ${entry.streak}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF59E0B))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onViewHistory,
                modifier = Modifier.testTag("dialog_view_history_button")
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Call History")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onChallenge,
                modifier = Modifier.testTag("dialog_challenge_call_button")
            ) {
                Text("Challenge Score")
            }
        }
    )
}

@Composable
private fun PerformanceRow(label: String, score: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp)
            Text(text = "$score%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                score >= 90 -> EmeraldSuccess
                score >= 80 -> CyanGlow
                score >= 70 -> AmberWarning
                else -> CrimsonEscalated
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun TierInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = CyanGlow)
                Text("Spectrum Agent Tiers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Agent tiers are calculated dynamically from mock simulation QA scores across all de-escalation, retention, and sales evaluations.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AgentTier.values().forEach { tier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(tier.colorHex).copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tier.title,
                            fontWeight = FontWeight.Bold,
                            color = Color(tier.colorHex),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${tier.minScore}%+ Average QA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("tier_info_close_button")
            ) {
                Text("Got It")
            }
        }
    )
}

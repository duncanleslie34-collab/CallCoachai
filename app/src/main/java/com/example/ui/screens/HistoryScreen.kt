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
import com.example.data.model.HistoricalCallReport
import com.example.data.model.ScenarioCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.GradeFilter
import com.example.ui.viewmodel.HistorySortOption
import com.example.ui.viewmodel.HistorySummaryStats
import com.example.ui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onOpenSessionDetail: (sessionId: Long) -> Unit,
    onStartNewCall: () -> Unit
) {
    val reports by viewModel.filteredReports.collectAsStateWithLifecycle()
    val stats by viewModel.summaryStats.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Call Session History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "${reports.size} recorded mock calls • AI scorecards & coaching",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("history_screen_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("history_sort_button")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort Options", tint = CyanGlow)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        HistorySortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.displayName,
                                        fontWeight = if (filterState.sortOption == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (filterState.sortOption == option) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
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
            // Summary KPI Banner
            HistoryStatsHero(stats = stats)

            // Search Bar
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search scenarios, trainees, outcomes, or notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filterState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("history_search_input")
            )

            // Grade / Performance Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GradeFilter.values()) { grade ->
                    FilterChip(
                        selected = filterState.gradeFilter == grade,
                        onClick = { viewModel.setGradeFilter(grade) },
                        label = { Text(grade.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            if (filterState.gradeFilter == grade) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }

            // Category Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedCategory == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All Categories", fontSize = 12.sp) }
                    )
                }

                items(ScenarioCategory.values()) { cat ->
                    FilterChip(
                        selected = filterState.selectedCategory == cat,
                        onClick = {
                            viewModel.setCategoryFilter(
                                if (filterState.selectedCategory == cat) null else cat
                            )
                        },
                        label = { Text(cat.displayName, fontSize = 12.sp) }
                    )
                }
            }

            // Session List or Empty State
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            text = if (filterState.searchQuery.isNotBlank() || filterState.selectedCategory != null || filterState.gradeFilter != GradeFilter.ALL)
                                "No matching sessions found"
                            else
                                "No Mock Calls Recorded Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (filterState.searchQuery.isNotBlank() || filterState.selectedCategory != null)
                                "Try adjusting your search filters to find past call sessions."
                            else
                                "Complete a mock simulation call with any customer persona to record your first performance report.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (filterState.searchQuery.isNotBlank() || filterState.selectedCategory != null || filterState.gradeFilter != GradeFilter.ALL) {
                            OutlinedButton(onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setCategoryFilter(null)
                                viewModel.setGradeFilter(GradeFilter.ALL)
                            }) {
                                Text("Reset Filters")
                            }
                        } else {
                            Button(
                                onClick = onStartNewCall,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start First Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(reports, key = { it.entity.id }) { report ->
                        HistoricalSessionCard(
                            report = report,
                            onClick = { onOpenSessionDetail(report.entity.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryStatsHero(stats: HistorySummaryStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroStatItem(
                label = "Total Calls",
                value = "${stats.totalCalls}",
                color = CyanGlow
            )
            Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Slate800)
            HeroStatItem(
                label = "Avg QA Score",
                value = "${stats.averageScore}%",
                color = if (stats.averageScore >= 80) EmeraldSuccess else AmberWarning
            )
            Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Slate800)
            HeroStatItem(
                label = "Pass Rate",
                value = "${stats.passRatePercentage}%",
                color = EmeraldSuccess
            )
            Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Slate800)
            HeroStatItem(
                label = "Practice Time",
                value = "${stats.totalDurationMinutes}m",
                color = IndigoLight
            )
        }
    }
}

@Composable
fun HeroStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = Slate400)
    }
}

@Composable
fun HistoricalSessionCard(
    report: HistoricalCallReport,
    onClick: () -> Unit
) {
    val entity = report.entity
    val gradeColor = when (entity.letterGrade) {
        "A+", "A" -> EmeraldSuccess
        "B" -> CyanGlow
        "C" -> AmberWarning
        else -> CrimsonEscalated
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("historical_session_card_${entity.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Category Badge + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (entity.category) {
                        "DE_ESCALATION" -> CrimsonContainer
                        "RETENTION" -> AmberContainer
                        "SALES_AND_UPGRADE" -> Color(0xFF3730A3)
                        "BILLING_DISPUTE" -> Color(0xFF1E3A8A)
                        "TECH_SUPPORT" -> Color(0xFF14532D)
                        else -> Color(0xFF581C87)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = entity.category.replace("_", " "),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scenario Title & Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = entity.scenarioTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trainee: ${entity.traineeName} • ${report.formattedDuration}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = gradeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${entity.overallScore}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradeColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${entity.letterGrade})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradeColor
                        )
                    }
                }
            }

            // Outcome & Dimension Mini-Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.outcome,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (entity.overallScore >= 80) EmeraldSuccess else AmberWarning
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScorePill(label = "Emp", score = entity.empathyScore, color = CyanGlow)
                    ScorePill(label = "DeEsc", score = entity.deescalationScore, color = EmeraldSuccess)
                    if (entity.salesScore > 0) {
                        ScorePill(label = "Sales", score = entity.salesScore, color = IndigoLight)
                    }
                }
            }

            // Manager Note Preview
            if (entity.managerNotes.isNotBlank()) {
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entity.managerNotes,
                            fontSize = 11.sp,
                            color = Slate100,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Footer Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Scorecard & Transcript",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScorePill(label: String, score: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "$label: $score%",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

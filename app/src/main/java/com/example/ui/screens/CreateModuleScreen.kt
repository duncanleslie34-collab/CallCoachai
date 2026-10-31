package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.DifficultyLevel
import com.example.data.model.ScenarioCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.ScenarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModuleScreen(
    viewModel: ScenarioViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.createFormState.collectAsStateWithLifecycle()

    var newObjTitle by remember { mutableStateOf("") }
    var newObjDesc by remember { mutableStateOf("") }
    var newObjMandatory by remember { mutableStateOf(true) }

    var newDqQuestion by remember { mutableStateOf("") }
    var newDqGuidance by remember { mutableStateOf("") }

    var showAddObjectiveDialog by remember { mutableStateOf(false) }
    var showAddDebriefDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Training Module", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Custom scenario, agent objectives & debrief questions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("create_module_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    formState.validationError?.let { err ->
                        Text(
                            text = err,
                            color = CrimsonEscalated,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveCustomModule {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_custom_module_button"),
                        enabled = !formState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (formState.isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Module to Training Suite", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
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
            // Telecom Sales Templates Fast Track
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast Track: Telecom Sales Templates", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyanGlow)
                        }
                        Text(
                            "One-tap pre-fill with proven telecommunications sales, cross-sell & up-sell scripts:",
                            fontSize = 12.sp,
                            color = Slate400
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(com.example.data.datasource.SalesModuleTemplates.templates) { tmpl ->
                                val isSelected = formState.selectedTemplate?.templateId == tmpl.templateId
                                Surface(
                                    color = if (isSelected) IndigoLight.copy(alpha = 0.25f) else Slate800,
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanGlow) else null,
                                    modifier = Modifier
                                        .clickable { viewModel.applySalesTemplate(tmpl) }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(tmpl.templateName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSelected) CyanGlow else Slate100)
                                        Text(tmpl.telecomOffer, fontSize = 10.sp, color = Slate400)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Script Coach Tips Banner (if template is selected)
            formState.selectedTemplate?.let { tmpl ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Telecom Sales Scripts & Objectives Loaded", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSuccess)
                            }
                            tmpl.scriptTips.forEach { tip ->
                                Text("• $tip", fontSize = 11.sp, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Section 1: Scenario Meta
            item {
                SectionHeader("1. Scenario Definition", "Title, department track & difficulty level")
            }

            item {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Module Title *") },
                    placeholder = { Text("e.g. VIP Subscription Fee Outrage") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("module_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                Text("Category Track", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ScenarioCategory.DE_ESCALATION,
                        ScenarioCategory.RETENTION,
                        ScenarioCategory.SALES_AND_UPGRADE
                    ).forEach { cat ->
                        FilterChip(
                            selected = formState.category == cat,
                            onClick = { viewModel.updateCategory(cat) },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ScenarioCategory.BILLING_DISPUTE,
                        ScenarioCategory.TECH_SUPPORT,
                        ScenarioCategory.VIP_SERVICE
                    ).forEach { cat ->
                        FilterChip(
                            selected = formState.category == cat,
                            onClick = { viewModel.updateCategory(cat) },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }

            item {
                Text("Difficulty Rating", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLevel.values().forEach { diff ->
                        FilterChip(
                            selected = formState.difficulty == diff,
                            onClick = { viewModel.updateDifficulty(diff) },
                            label = { Text(diff.displayName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = formState.scenarioBrief,
                    onValueChange = { viewModel.updateScenarioBrief(it) },
                    label = { Text("Scenario Brief for Trainee *") },
                    placeholder = { Text("Context given to the agent before the call begins...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("module_brief_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
            }

            // Section 2: Customer Persona
            item {
                SectionHeader("2. Customer Persona Configuration", "Psychology, emotional state & desired outcome")
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.personaName,
                        onValueChange = { viewModel.updatePersonaName(it) },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Rachel Torres") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("persona_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = formState.personaOccupation,
                        onValueChange = { viewModel.updatePersonaOccupation(it) },
                        label = { Text("Occupation") },
                        placeholder = { Text("e.g. Attorney") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = formState.emotionalState,
                    onValueChange = { viewModel.updateEmotionalState(it) },
                    label = { Text("Emotional State / Demeanor") },
                    placeholder = { Text("e.g. Belligerent & Sarcastic, Distrustful") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("persona_mood_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Initial Customer Anger: ${formState.initialAngerLevel}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (formState.initialAngerLevel > 75) "High Rage" else if (formState.initialAngerLevel > 45) "Agitated" else "Mild",
                            fontSize = 12.sp,
                            color = if (formState.initialAngerLevel > 75) CrimsonEscalated else AmberWarning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = formState.initialAngerLevel.toFloat(),
                        onValueChange = { viewModel.updateInitialAnger(it.toInt()) },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = if (formState.initialAngerLevel > 75) CrimsonEscalated else CyanGlow,
                            activeTrackColor = if (formState.initialAngerLevel > 75) CrimsonEscalated else CyanGlow
                        )
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = formState.reasonForCalling,
                    onValueChange = { viewModel.updateReasonForCalling(it) },
                    label = { Text("Reason for Calling *") },
                    placeholder = { Text("Exact issue customer is contacting support about...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            item {
                OutlinedTextField(
                    value = formState.desiredOutcome,
                    onValueChange = { viewModel.updateDesiredOutcome(it) },
                    label = { Text("Customer's Desired Outcome *") },
                    placeholder = { Text("What resolution will satisfy them? (e.g. 50% credit, replacement)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = formState.backstory,
                    onValueChange = { viewModel.updateBackstory(it) },
                    label = { Text("Persona Backstory (Optional)") },
                    placeholder = { Text("Hidden context about customer's day or history...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            // Section 3: Performance Objectives
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("3. Agent Performance Objectives", "Specific scoring criteria evaluated by AI")
                    TextButton(
                        onClick = { showAddObjectiveDialog = true },
                        modifier = Modifier.testTag("add_objective_dialog_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Objective")
                    }
                }
            }

            itemsIndexed(formState.objectives) { index, obj ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate950)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(obj.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(obj.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.removeObjective(obj.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CrimsonEscalated)
                        }
                    }
                }
            }

            // Section 4: Debriefing Questions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("4. Targeted Debriefing Questions", "Post-call questions evaluated in QA report")
                    TextButton(
                        onClick = { showAddDebriefDialog = true },
                        modifier = Modifier.testTag("add_debrief_dialog_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Question")
                    }
                }
            }

            itemsIndexed(formState.debriefQuestions) { index, dq ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(IndigoLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Q${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dq.question, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Guidance: ${dq.guidance}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.removeDebriefQuestion(dq.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = CrimsonEscalated)
                        }
                    }
                }
            }
        }
    }

    // Add Objective Modal
    if (showAddObjectiveDialog) {
        AlertDialog(
            onDismissRequest = { showAddObjectiveDialog = false },
            title = { Text("Add Performance Objective") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newObjTitle,
                        onValueChange = { newObjTitle = it },
                        label = { Text("Objective Title") },
                        placeholder = { Text("e.g. De-escalate within 4 turns") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newObjDesc,
                        onValueChange = { newObjDesc = it },
                        label = { Text("Description / Scoring Criteria") },
                        placeholder = { Text("e.g. Agent must validate feeling before offering credit.") }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newObjMandatory, onCheckedChange = { newObjMandatory = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mandatory for passing grade", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newObjTitle.isNotBlank()) {
                        viewModel.addObjective(newObjTitle, newObjDesc, newObjMandatory)
                        newObjTitle = ""
                        newObjDesc = ""
                        showAddObjectiveDialog = false
                    }
                }) {
                    Text("Add Objective")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddObjectiveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Debrief Question Modal
    if (showAddDebriefDialog) {
        AlertDialog(
            onDismissRequest = { showAddDebriefDialog = false },
            title = { Text("Add Debriefing Question") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newDqQuestion,
                        onValueChange = { newDqQuestion = it },
                        label = { Text("Question") },
                        placeholder = { Text("e.g. How did you identify the customer's root churn reason?") }
                    )
                    OutlinedTextField(
                        value = newDqGuidance,
                        onValueChange = { newDqGuidance = it },
                        label = { Text("Evaluation Guidance for AI") },
                        placeholder = { Text("e.g. Look for open probing vs defensive pricing defense.") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newDqQuestion.isNotBlank()) {
                        viewModel.addDebriefQuestion(newDqQuestion, newDqGuidance)
                        newDqQuestion = ""
                        newDqGuidance = ""
                        showAddDebriefDialog = false
                    }
                }) {
                    Text("Add Question")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDebriefDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyanGlow)
        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

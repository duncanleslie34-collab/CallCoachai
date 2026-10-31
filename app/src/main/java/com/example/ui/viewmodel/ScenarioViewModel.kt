package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CallCoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScenarioFilterState(
    val selectedCategory: ScenarioCategory? = null,
    val selectedDifficulty: DifficultyLevel? = null,
    val searchQuery: String = ""
)

data class CreateModuleFormState(
    val title: String = "",
    val category: ScenarioCategory = ScenarioCategory.DE_ESCALATION,
    val difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
    val personaName: String = "",
    val personaAge: Int = 35,
    val personaOccupation: String = "",
    val emotionalState: String = "Frustrated",
    val initialAngerLevel: Int = 75,
    val backstory: String = "",
    val reasonForCalling: String = "",
    val desiredOutcome: String = "",
    val scenarioBrief: String = "",
    val objectives: List<CallObjective> = listOf(
        CallObjective("obj_1", "Active Empathy Validation", "Acknowledge the caller's feeling within the first 2 responses.", true),
        CallObjective("obj_2", "Prohibited Word Avoidance", "Avoid saying 'company policy' or telling the caller to 'calm down'.", true)
    ),
    val debriefQuestions: List<DebriefQuestion> = listOf(
        DebriefQuestion("dq_1", "What was the caller's core emotional trigger?", "Identifies root cause vs surface anger."),
        DebriefQuestion("dq_2", "How did you structure the solution to prevent churn?", "Evaluates retention value framing.")
    ),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val selectedTemplate: com.example.data.datasource.SalesModuleTemplate? = null,
    val validationError: String? = null
)

class ScenarioViewModel(
    private val repository: CallCoachRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(ScenarioFilterState())
    val filterState: StateFlow<ScenarioFilterState> = _filterState.asStateFlow()

    private val _createFormState = MutableStateFlow(CreateModuleFormState())
    val createFormState: StateFlow<CreateModuleFormState> = _createFormState.asStateFlow()

    val scenarios: StateFlow<List<CallScenario>> = combine(
        repository.getAllScenarios(),
        _filterState
    ) { allScenarios, filters ->
        allScenarios.filter { scen ->
            val matchCategory = filters.selectedCategory == null || scen.category == filters.selectedCategory
            val matchDifficulty = filters.selectedDifficulty == null || scen.difficulty == filters.selectedDifficulty
            val matchSearch = filters.searchQuery.isBlank() ||
                    scen.title.contains(filters.searchQuery, ignoreCase = true) ||
                    scen.persona.name.contains(filters.searchQuery, ignoreCase = true) ||
                    scen.scenarioBrief.contains(filters.searchQuery, ignoreCase = true)
            matchCategory && matchDifficulty && matchSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategoryFilter(category: ScenarioCategory?) {
        _filterState.update { it.copy(selectedCategory = category) }
    }

    fun setDifficultyFilter(difficulty: DifficultyLevel?) {
        _filterState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    // Custom Module Form Handlers
    fun updateTitle(v: String) = _createFormState.update { it.copy(title = v, validationError = null) }
    fun updateCategory(v: ScenarioCategory) = _createFormState.update { it.copy(category = v) }
    fun updateDifficulty(v: DifficultyLevel) = _createFormState.update { it.copy(difficulty = v) }
    fun updatePersonaName(v: String) = _createFormState.update { it.copy(personaName = v, validationError = null) }
    fun updatePersonaOccupation(v: String) = _createFormState.update { it.copy(personaOccupation = v) }
    fun updateEmotionalState(v: String) = _createFormState.update { it.copy(emotionalState = v) }
    fun updateInitialAnger(v: Int) = _createFormState.update { it.copy(initialAngerLevel = v) }
    fun updateBackstory(v: String) = _createFormState.update { it.copy(backstory = v) }
    fun updateReasonForCalling(v: String) = _createFormState.update { it.copy(reasonForCalling = v) }
    fun updateDesiredOutcome(v: String) = _createFormState.update { it.copy(desiredOutcome = v) }
    fun updateScenarioBrief(v: String) = _createFormState.update { it.copy(scenarioBrief = v) }

    fun addObjective(title: String, desc: String, isMandatory: Boolean = true) {
        if (title.isBlank()) return
        val newObj = CallObjective(
            id = "custom_obj_${System.currentTimeMillis()}",
            title = title.trim(),
            description = desc.trim(),
            isMandatory = isMandatory
        )
        _createFormState.update { it.copy(objectives = it.objectives + newObj) }
    }

    fun removeObjective(id: String) {
        _createFormState.update { it.copy(objectives = it.objectives.filter { obj -> obj.id != id }) }
    }

    fun addDebriefQuestion(question: String, guidance: String) {
        if (question.isBlank()) return
        val newDq = DebriefQuestion(
            id = "custom_dq_${System.currentTimeMillis()}",
            question = question.trim(),
            guidance = guidance.trim()
        )
        _createFormState.update { it.copy(debriefQuestions = it.debriefQuestions + newDq) }
    }

    fun removeDebriefQuestion(id: String) {
        _createFormState.update { it.copy(debriefQuestions = it.debriefQuestions.filter { dq -> dq.id != id }) }
    }

    fun saveCustomModule(onSuccess: () -> Unit) {
        val form = _createFormState.value
        if (form.title.isBlank()) {
            _createFormState.update { it.copy(validationError = "Please enter a module title.") }
            return
        }
        if (form.personaName.isBlank()) {
            _createFormState.update { it.copy(validationError = "Please enter a customer persona name.") }
            return
        }
        if (form.reasonForCalling.isBlank()) {
            _createFormState.update { it.copy(validationError = "Please describe the reason for calling.") }
            return
        }

        _createFormState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val scenarioId = "custom_${System.currentTimeMillis()}"
            val scenario = CallScenario(
                id = scenarioId,
                title = form.title.trim(),
                category = form.category,
                difficulty = form.difficulty,
                persona = CustomerPersona(
                    id = "persona_$scenarioId",
                    name = form.personaName.trim(),
                    age = form.personaAge,
                    occupation = form.personaOccupation.ifBlank { "Customer" },
                    emotionalState = form.emotionalState.trim(),
                    initialAngerLevel = form.initialAngerLevel,
                    backstory = form.backstory.trim(),
                    reasonForCalling = form.reasonForCalling.trim(),
                    desiredOutcome = form.desiredOutcome.trim(),
                    triggerWords = listOf("policy", "calm down", "cannot", "rules"),
                    soothingWords = listOf("I understand", "take care of this", "ownership", "solution"),
                    category = form.category,
                    avatarInitials = form.personaName.take(2).uppercase()
                ),
                scenarioBrief = form.scenarioBrief.ifBlank { form.reasonForCalling },
                targetObjectives = form.objectives,
                debriefQuestions = form.debriefQuestions,
                isCustom = true,
                createdByManager = "Manager (Training Lead)"
            )

            repository.saveCustomScenario(scenario)
            _createFormState.update { CreateModuleFormState(saveSuccess = true) }
            onSuccess()
        }
    }

    fun applySalesTemplate(template: com.example.data.datasource.SalesModuleTemplate) {
        _createFormState.update {
            it.copy(
                title = template.templateName,
                category = template.suggestedCategory,
                difficulty = template.suggestedDifficulty,
                personaName = template.personaName,
                personaAge = template.personaAge,
                personaOccupation = template.personaOccupation,
                emotionalState = template.emotionalState,
                initialAngerLevel = template.initialAnger,
                reasonForCalling = template.reasonForCalling,
                desiredOutcome = template.desiredOutcome,
                scenarioBrief = template.scenarioBrief,
                objectives = template.objectives,
                debriefQuestions = template.debriefQuestions,
                selectedTemplate = template,
                validationError = null
            )
        }
    }

    fun resetCreateForm() {
        _createFormState.value = CreateModuleFormState()
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.CallScenario
import com.example.data.repository.CallCoachRepository
import com.example.ui.screens.*
import com.example.ui.theme.CallCoachTheme
import com.example.ui.viewmodel.CallSessionViewModel
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.ManagerViewModel
import com.example.ui.viewmodel.ScenarioViewModel
import com.example.util.configureAppCheck

enum class AppScreen {
    SCENARIOS,
    CALL_SIM,
    EVALUATION,
    MANAGER_ANALYTICS,
    CREATE_MODULE,
    TECHNIQUES_GUIDE,
    HISTORY,
    SESSION_DETAIL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val repository = CallCoachRepository(
            sessionDao = database.callSessionDao(),
            customScenarioDao = database.customScenarioDao(),
            coachingDao = database.managerCoachingDao()
        )

        setContent {
            CallCoachTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val callViewModel = remember { CallSessionViewModel(repository) }
                    val scenarioViewModel = remember { ScenarioViewModel(repository) }
                    val managerViewModel = remember { ManagerViewModel(repository) }
                    val historyViewModel = remember { HistoryViewModel(repository) }

                    // Initialize TextToSpeech engine
                    LaunchedEffect(Unit) {
                        callViewModel.initTts(this@MainActivity)
                    }

                    var currentScreen by remember { mutableStateOf(AppScreen.SCENARIOS) }
                    var activeScenario by remember { mutableStateOf<CallScenario?>(null) }

                    // Handle Back Navigation cleanly
                    when (currentScreen) {
                        AppScreen.SCENARIOS -> {
                            // Root screen - default system back
                        }
                        AppScreen.CALL_SIM -> {
                            BackHandler {
                                callViewModel.clearActiveCall()
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.EVALUATION -> {
                            BackHandler {
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.MANAGER_ANALYTICS -> {
                            BackHandler {
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.CREATE_MODULE -> {
                            BackHandler {
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.TECHNIQUES_GUIDE -> {
                            BackHandler {
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.HISTORY -> {
                            BackHandler {
                                currentScreen = AppScreen.SCENARIOS
                            }
                        }
                        AppScreen.SESSION_DETAIL -> {
                            BackHandler {
                                historyViewModel.clearSelectedSession()
                                currentScreen = AppScreen.HISTORY
                            }
                        }
                    }

                    when (currentScreen) {
                        AppScreen.SCENARIOS -> {
                            ScenariosScreen(
                                scenarioViewModel = scenarioViewModel,
                                onStartCall = { scenario ->
                                    activeScenario = scenario
                                    val trainee = managerViewModel.uiState.value.selectedTrainee
                                        ?: repository.getTrainees().first()
                                    callViewModel.startCall(scenario, trainee)
                                    currentScreen = AppScreen.CALL_SIM
                                },
                                onOpenManagerDashboard = {
                                    currentScreen = AppScreen.MANAGER_ANALYTICS
                                },
                                onCreateCustomModule = {
                                    scenarioViewModel.resetCreateForm()
                                    currentScreen = AppScreen.CREATE_MODULE
                                },
                                onOpenTechniquesGuide = {
                                    currentScreen = AppScreen.TECHNIQUES_GUIDE
                                },
                                onOpenHistory = {
                                    currentScreen = AppScreen.HISTORY
                                }
                            )
                        }

                        AppScreen.CALL_SIM -> {
                            CallSimScreen(
                                viewModel = callViewModel,
                                onCallFinished = {
                                    currentScreen = AppScreen.EVALUATION
                                },
                                onCancelCall = {
                                    callViewModel.clearActiveCall()
                                    currentScreen = AppScreen.SCENARIOS
                                }
                            )
                        }

                        AppScreen.EVALUATION -> {
                            EvaluationScreen(
                                callSessionViewModel = callViewModel,
                                onRetryScenario = {
                                    val scenario = activeScenario
                                    if (scenario != null) {
                                        val trainee = managerViewModel.uiState.value.selectedTrainee
                                            ?: repository.getTrainees().first()
                                        callViewModel.startCall(scenario, trainee)
                                        currentScreen = AppScreen.CALL_SIM
                                    } else {
                                        currentScreen = AppScreen.SCENARIOS
                                    }
                                },
                                onBackToScenarios = {
                                    callViewModel.clearActiveCall()
                                    currentScreen = AppScreen.SCENARIOS
                                },
                                onViewManagerAnalytics = {
                                    currentScreen = AppScreen.MANAGER_ANALYTICS
                                },
                                onViewHistory = {
                                    currentScreen = AppScreen.HISTORY
                                }
                            )
                        }

                        AppScreen.MANAGER_ANALYTICS -> {
                            ManagerAnalyticsScreen(
                                managerViewModel = managerViewModel,
                                onNavigateBack = {
                                    currentScreen = AppScreen.SCENARIOS
                                },
                                onOpenSessionDetail = { sessionId ->
                                    historyViewModel.selectSession(sessionId)
                                    currentScreen = AppScreen.SESSION_DETAIL
                                }
                            )
                        }

                        AppScreen.CREATE_MODULE -> {
                            CreateModuleScreen(
                                viewModel = scenarioViewModel,
                                onNavigateBack = {
                                    currentScreen = AppScreen.SCENARIOS
                                }
                            )
                        }

                        AppScreen.TECHNIQUES_GUIDE -> {
                            TechniquesGuideScreen(
                                onNavigateBack = {
                                    currentScreen = AppScreen.SCENARIOS
                                }
                            )
                        }

                        AppScreen.HISTORY -> {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                onNavigateBack = {
                                    currentScreen = AppScreen.SCENARIOS
                                },
                                onOpenSessionDetail = { sessionId ->
                                    historyViewModel.selectSession(sessionId)
                                    currentScreen = AppScreen.SESSION_DETAIL
                                },
                                onStartNewCall = {
                                    currentScreen = AppScreen.SCENARIOS
                                }
                            )
                        }

                        AppScreen.SESSION_DETAIL -> {
                            val selectedReport by historyViewModel.selectedReport.collectAsStateWithLifecycle()
                            HistoricalSessionDetailScreen(
                                report = selectedReport,
                                onNavigateBack = {
                                    historyViewModel.clearSelectedSession()
                                    currentScreen = AppScreen.HISTORY
                                },
                                onRetryScenario = { scenarioId ->
                                    val scenariosList = scenarioViewModel.scenarios.value
                                    val scenario = scenariosList.find { it.id == scenarioId }
                                        ?: com.example.data.datasource.SeedData.defaultScenarios.first()
                                    val trainee = managerViewModel.uiState.value.selectedTrainee
                                        ?: repository.getTrainees().first()
                                    activeScenario = scenario
                                    callViewModel.startCall(scenario, trainee)
                                    currentScreen = AppScreen.CALL_SIM
                                },
                                onSaveManagerNote = { sessionId, note ->
                                    historyViewModel.updateManagerNote(sessionId, note)
                                },
                                onDeleteSession = { sessionId ->
                                    historyViewModel.deleteSession(sessionId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.SeedData
import com.example.data.model.ScenarioCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CallCoach AI", appName)
    }

    @Test
    fun `verify customer personas including Spectrum sales exist`() {
        val personas = SeedData.personas
        assertEquals(14, personas.size)

        // Verify key personas
        val marcus = personas.find { it.name == "Marcus Vance" }
        assertTrue("Marcus Vance should exist", marcus != null)
        assertEquals(ScenarioCategory.BILLING_DISPUTE, marcus?.category)

        val brenda = personas.find { it.name == "Brenda Miller" }
        assertTrue("Brenda Miller should exist", brenda != null)

        val david = personas.find { it.name == "David Sterling" }
        assertTrue("David Sterling should exist", david != null)
        assertEquals(ScenarioCategory.RETENTION, david?.category)

        val elena = personas.find { it.name == "Elena Rostova" }
        assertTrue("Elena Rostova should exist", elena != null)
        assertEquals(ScenarioCategory.DE_ESCALATION, elena?.category)

        val tyler = personas.find { it.name == "Tyler Brooks" }
        assertTrue("Tyler Brooks Spectrum Sales persona should exist", tyler != null)
        assertEquals(ScenarioCategory.SALES_AND_UPGRADE, tyler?.category)

        val morgan = personas.find { it.name == "Morgan Reynolds" }
        assertTrue("Morgan Reynolds Mover persona should exist", morgan != null)
        assertEquals(ScenarioCategory.SALES_AND_UPGRADE, morgan?.category)
    }

    @Test
    fun `verify all scenarios have objectives and debrief questions`() {
        val scenarios = SeedData.defaultScenarios
        assertTrue(scenarios.isNotEmpty())
        scenarios.forEach { scenario ->
            assertTrue("Scenario ${scenario.title} should have objectives", scenario.targetObjectives.isNotEmpty())
            assertTrue("Scenario ${scenario.title} should have debrief questions", scenario.debriefQuestions.isNotEmpty())
        }
    }

    @Test
    fun `verify call session transcript and evaluation serialization fidelity`() {
        val turns = listOf(
            com.example.data.model.CallTurn(
                id = "t1",
                speaker = com.example.data.model.Speaker.CUSTOMER,
                message = "Why is my bill higher?",
                timestampMs = 1000L,
                sentimentFeedback = com.example.data.model.LiveSentimentFeedback("Frustrated", 50, -5, null, "Acknowledge frustration", 75)
            ),
            com.example.data.model.CallTurn(
                id = "t2",
                speaker = com.example.data.model.Speaker.AGENT,
                message = "I will take care of this for you immediately.",
                timestampMs = 2000L
            )
        )

        val jsonTranscript = com.example.data.model.CallSessionConverter.serializeTranscript(turns)
        assertTrue(jsonTranscript.contains("Why is my bill higher?"))

        val restoredTurns = com.example.data.model.CallSessionConverter.deserializeTranscript(jsonTranscript)
        assertEquals(2, restoredTurns.size)
        assertEquals("Why is my bill higher?", restoredTurns[0].message)
        assertEquals(com.example.data.model.Speaker.CUSTOMER, restoredTurns[0].speaker)
        assertEquals(75, restoredTurns[0].sentimentFeedback?.customerAngerLevel)
        assertEquals(com.example.data.model.Speaker.AGENT, restoredTurns[1].speaker)
    }

    @Test
    fun `verify in-memory room database session persistence and manager notes`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.local.AppDatabase::class.java).build()
        val dao = db.callSessionDao()

        val sampleSession = com.example.data.local.CallSessionEntity(
            traineeId = "trainee_1",
            traineeName = "Alex Rivera",
            scenarioId = "scen_1",
            scenarioTitle = "Billing Dispute",
            category = "BILLING_DISPUTE",
            durationSeconds = 120,
            overallScore = 88,
            letterGrade = "B",
            outcome = "De-escalated Successfully",
            deescalationScore = 90,
            empathyScore = 85,
            toneScore = 87,
            retentionScore = 80,
            salesScore = 75,
            protocolScore = 92,
            executiveSummary = "Strong active listening demonstrated.",
            transcriptJson = "[]",
            feedbackDetailsJson = "{}",
            managerNotes = "Good first attempt."
        )

        val id = dao.insertSession(sampleSession)
        assertTrue(id > 0)

        // Query session
        val sessionFlow = dao.getSessionById(id)
        val loaded = sessionFlow.first()
        assertTrue(loaded != null)
        assertEquals("Alex Rivera", loaded?.traineeName)
        assertEquals(88, loaded?.overallScore)
        assertEquals("Good first attempt.", loaded?.managerNotes)

        // Update manager notes
        dao.updateManagerNotes(id, "Updated coaching note: exemplary tone.")
        val updated = dao.getSessionById(id).first()
        assertEquals("Updated coaching note: exemplary tone.", updated?.managerNotes)

        // Delete session
        dao.deleteSession(id)
        val afterDelete = dao.getSessionById(id).first()
        assertEquals(null, afterDelete)

        db.close()
    }
}

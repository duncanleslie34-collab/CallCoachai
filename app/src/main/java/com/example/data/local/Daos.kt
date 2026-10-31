package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallSessionDao {
    @Query("SELECT * FROM call_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<CallSessionEntity>>

    @Query("SELECT * FROM call_sessions WHERE traineeId = :traineeId ORDER BY timestamp DESC")
    fun getSessionsForTrainee(traineeId: String): Flow<List<CallSessionEntity>>

    @Query("SELECT * FROM call_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: Long): Flow<CallSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CallSessionEntity): Long

    @Query("UPDATE call_sessions SET managerNotes = :notes WHERE id = :id")
    suspend fun updateManagerNotes(id: Long, notes: String)

    @Query("DELETE FROM call_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}

@Dao
interface CustomScenarioDao {
    @Query("SELECT * FROM custom_scenarios ORDER BY createdTimestamp DESC")
    fun getAllCustomScenarios(): Flow<List<CustomScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: CustomScenarioEntity)

    @Query("DELETE FROM custom_scenarios WHERE id = :id")
    suspend fun deleteScenario(id: String)
}

@Dao
interface ManagerCoachingDao {
    @Query("SELECT * FROM manager_coaching_notes WHERE traineeId = :traineeId ORDER BY timestamp DESC")
    fun getNotesForTrainee(traineeId: String): Flow<List<ManagerCoachingNoteEntity>>

    @Query("SELECT * FROM manager_coaching_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<ManagerCoachingNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ManagerCoachingNoteEntity): Long

    @Query("DELETE FROM manager_coaching_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}

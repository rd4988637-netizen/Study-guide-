package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudySession
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // Sessions
    @Query("SELECT * FROM study_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE completedAt >= :startTimeMs AND completedAt <= :endTimeMs ORDER BY completedAt DESC")
    fun getSessionsBetween(startTimeMs: Long, endTimeMs: Long): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE sessionType = 'FOCUS' AND wasCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedFocusSessions(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAllSessions()

    // Subjects
    @Query("SELECT * FROM study_subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<StudySubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: StudySubject): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialSubjects(subjects: List<StudySubject>)

    @Delete
    suspend fun deleteSubject(subject: StudySubject)

    // Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettings)
}

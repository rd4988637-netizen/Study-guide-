package com.example.data.repository

import com.example.data.dao.StudyDao
import com.example.data.model.StudySession
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val dao: StudyDao) {

    val allSessions: Flow<List<StudySession>> = dao.getAllSessions()
    val allSubjects: Flow<List<StudySubject>> = dao.getAllSubjects()
    val userSettings: Flow<UserSettings?> = dao.getUserSettings()

    fun getSessionsBetween(startMs: Long, endMs: Long): Flow<List<StudySession>> =
        dao.getSessionsBetween(startMs, endMs)

    fun getCompletedFocusSessions(): Flow<List<StudySession>> =
        dao.getCompletedFocusSessions()

    suspend fun logSession(session: StudySession): Long =
        dao.insertSession(session)

    suspend fun deleteSession(session: StudySession) =
        dao.deleteSession(session)

    suspend fun deleteSessionById(id: Long) =
        dao.deleteSessionById(id)

    suspend fun clearAllSessions() =
        dao.clearAllSessions()

    suspend fun addSubject(subject: StudySubject): Long =
        dao.insertSubject(subject)

    suspend fun deleteSubject(subject: StudySubject) =
        dao.deleteSubject(subject)

    suspend fun saveSettings(settings: UserSettings) =
        dao.saveUserSettings(settings)
}

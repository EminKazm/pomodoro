package com.syntax.domain.repository

import androidx.lifecycle.LiveData
import com.syntax.domain.entities.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun insertSession(session: Session)
    fun getAllSessions(): Flow<List<Session>>
}
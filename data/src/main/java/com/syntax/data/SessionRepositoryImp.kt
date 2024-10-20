package com.syntax.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.syntax.data.dao.SessionDao
import com.syntax.domain.entities.Session
import com.syntax.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
    override suspend fun insertSession(session: Session) {
        try {
            sessionDao.insertSession(session)
        } catch (e: Exception) {
        }
    }

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions()
    }
}

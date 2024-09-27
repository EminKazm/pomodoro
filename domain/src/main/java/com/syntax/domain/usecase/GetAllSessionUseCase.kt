package com.syntax.domain.usecase

import com.syntax.domain.entities.Session
import com.syntax.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSessionUseCase @Inject constructor(private val repository: SessionRepository) {

    operator fun invoke(): Flow<List<Session>> {
        return repository.getAllSessions()
    }
}
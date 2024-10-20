package com.syntax.domain.usecase
import android.util.Log
import com.syntax.domain.entities.Session
import com.syntax.domain.repository.SessionRepository
import javax.inject.Inject


class InsertSessionUseCase @Inject constructor(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke(session: Session) {
        sessionRepository.insertSession(session)
    }
}

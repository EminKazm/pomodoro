package com.syntax.domain.usecase
import android.util.Log
import com.syntax.domain.entities.Session
import com.syntax.domain.repository.SessionRepository
import javax.inject.Inject


class InsertSessionUseCase @Inject constructor(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke(session: Session) {
        Log.d("InsertSessionUseCase", "Inserting session via UseCase: $session")
        sessionRepository.insertSession(session)
    }
}

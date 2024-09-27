package com.syntax.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.syntax.domain.entities.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Query("SELECT * FROM session_table WHERE id = :id")
    suspend fun getSessionById(id: Long): Session?

    @Query("SELECT * FROM session_table ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<Session>>
}
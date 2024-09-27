package com.syntax.domain.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_table")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val timestamp: Long,
    val workduration: Long,
    val breakduration: Long,
)
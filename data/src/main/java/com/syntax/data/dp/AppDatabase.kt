package com.syntax.data.dp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.syntax.data.dao.SessionDao
import com.syntax.domain.entities.Session

@Database(entities = [Session::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
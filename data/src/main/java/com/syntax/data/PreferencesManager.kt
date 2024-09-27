package com.syntax.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)


    var pomodoroDuration: Long
        get() = preferences.getLong(POMODORO_DURATION, 25)
        set(value) = preferences.edit().putLong(POMODORO_DURATION, value).apply()
    var shortBreakDuration: Long
        get() = preferences.getLong(SHORT_BREAK_DURATION, 5)
        set(value) = preferences.edit().putLong(SHORT_BREAK_DURATION, value).apply()
    var longBreakDuration: Long
        get() = preferences.getLong(LONG_BREAK_DURATION, 15)
        set(value) = preferences.edit().putLong(LONG_BREAK_DURATION, value).apply()

    companion object{
        const val POMODORO_DURATION = "pomodoro_duration"
        const val SHORT_BREAK_DURATION = "short_break_duration"
        const val LONG_BREAK_DURATION = "long_break_duration"

    }
}
package com.syntax.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntax.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor( val preferencesManager: PreferencesManager):ViewModel()
{
    private val _pomodoroDuration = MutableStateFlow(preferencesManager.pomodoroDuration)
    val pomodoroDuration: StateFlow<Long> = _pomodoroDuration

    private val _shortBreakDuration = MutableStateFlow(preferencesManager.shortBreakDuration)
    val shortBreakDuration: StateFlow<Long> = _shortBreakDuration

    private val _longBreakDuration = MutableStateFlow(preferencesManager.longBreakDuration)
    val longBreakDuration: StateFlow<Long> = _longBreakDuration

    // Functions to update durations
    fun setPomodoroDuration(duration: Long) {
        _pomodoroDuration.value = duration
    }

    fun setShortBreakDuration(duration: Long) {
        _shortBreakDuration.value = duration
    }

    fun setLongBreakDuration(duration: Long) {
        _longBreakDuration.value = duration
    }

    // Function to save settings
    fun saveSettings() {
        viewModelScope.launch {
            preferencesManager.pomodoroDuration = _pomodoroDuration.value
            preferencesManager.shortBreakDuration = _shortBreakDuration.value
            preferencesManager.longBreakDuration = _longBreakDuration.value
        }
    }
}
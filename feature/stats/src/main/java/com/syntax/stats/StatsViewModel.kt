package com.syntax.stats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntax.domain.entities.Session
import com.syntax.domain.usecase.GetAllSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(private val getStatsUseCase: GetAllSessionUseCase) : ViewModel(){
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    init {
        viewModelScope.launch {
\            getStatsUseCase().collect { sessionList ->
                Log.d("StatsViewModel", "Collected sessions: $sessionList")
                _sessions.emit(sessionList) // Emit the new list of sessions
            }
        }
    }

}
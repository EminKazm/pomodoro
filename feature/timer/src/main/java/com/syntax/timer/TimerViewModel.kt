package com.syntax.timer

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syntax.core.CHANNEL_ID
import com.syntax.core.NOTIFICATION_ID
import com.syntax.data.PreferencesManager
import com.syntax.domain.entities.Session
import com.syntax.domain.usecase.InsertSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class TimerState {
    Stopped,
    Running,
    Paused
}

enum class TimerType {
    Pomodoro,
    ShortBreak,
    LongBreak
}

data class TimerData(
    val remainingTimeMillis: Long,
    val totalDurationMillis: Long,
    val timerState: TimerState,
    val timerType: TimerType
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val insertSessionUseCase: InsertSessionUseCase,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val appContext: Context

) : ViewModel() {

    private val initialTimerType = TimerType.Pomodoro
    private val initialDurationMillis = getDurationMillis(initialTimerType)

    private val _timerData = MutableStateFlow(
        TimerData(
            remainingTimeMillis = initialDurationMillis,
            totalDurationMillis = initialDurationMillis,
            timerState = TimerState.Stopped,
            timerType = initialTimerType
        )
    )
    val timerData: StateFlow<TimerData> = _timerData.asStateFlow()

    private var timerJob: Job? = null

    private fun showNotification() {
        val packageName = appContext.packageName
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(appContext, CHANNEL_ID)
        notificationBuilder.setContentTitle("Timer Finished")
            .setContentText("Your ${timerData.value.timerType} is up!")
            .setSmallIcon(R.drawable.ic_timer)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        with(NotificationManagerCompat.from(appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }
    private fun scheduleAlarm(triggerAtMillis: Long, timerType: TimerType) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, TimerExpiredReceiver::class.java).apply {
            action = "com.syntax.timer.ACTION_TIMER_EXPIRED"
            putExtra("TIMER_TYPE", timerType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun getDurationMillis(timerType: TimerType): Long {
        val durationMinutes = when (timerType) {
            TimerType.Pomodoro -> preferencesManager.pomodoroDuration
            TimerType.ShortBreak -> preferencesManager.shortBreakDuration
            TimerType.LongBreak -> preferencesManager.longBreakDuration
        }
        return durationMinutes * 60 * 1000
    }

//    fun startTimer() {
//        if (_timerData.value.timerState == TimerState.Running) return
//
//        _timerData.update { it.copy(timerState = TimerState.Running) }
//
//        val startTime = System.currentTimeMillis()
//        val initialRemainingTime = _timerData.value.remainingTimeMillis
//
//        timerJob = viewModelScope.launch {
//            while (_timerData.value.remainingTimeMillis > 0 && isActive) {
//                delay(1000L)
//                val elapsedTime = System.currentTimeMillis() - startTime
//                val newRemainingTime = initialRemainingTime - elapsedTime
//                _timerData.update { it.copy(remainingTimeMillis = newRemainingTime.coerceAtLeast(0L)) }
//            }
//
//            if (_timerData.value.remainingTimeMillis <= 0) {
//                onTimerFinished()
//            }
//        }
//    }
fun startTimer() {
    if (_timerData.value.timerState == TimerState.Running) return

    _timerData.update { it.copy(timerState = TimerState.Running) }

    val startTime = System.currentTimeMillis()
    val initialRemainingTime = _timerData.value.remainingTimeMillis

    // Calculate when the timer should finish
    val triggerAtMillis = startTime + initialRemainingTime

    // Schedule the alarm
    scheduleAlarm(triggerAtMillis, _timerData.value.timerType)

    // Start the timer countdown (if needed for UI updates)
    timerJob = viewModelScope.launch {
        while (_timerData.value.remainingTimeMillis > 0 && isActive) {
            delay(1000L)
            val elapsedTime = System.currentTimeMillis() - startTime
            val newRemainingTime = initialRemainingTime - elapsedTime
            _timerData.update { it.copy(remainingTimeMillis = newRemainingTime.coerceAtLeast(0L)) }
        }

        if (_timerData.value.remainingTimeMillis <= 0) {
            onTimerFinished()
        }
    }
}
    private fun cancelAlarm() {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, TimerExpiredReceiver::class.java).apply {
            action = "com.syntax.timer.ACTION_TIMER_EXPIRED"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun pauseTimer() {
        if (_timerData.value.timerState != TimerState.Running) return

        timerJob?.cancel()
        cancelAlarm()

        _timerData.update { it.copy(timerState = TimerState.Paused) }
        saveSession() // Save the session when paused
    }

    fun resetTimer() {
        if (_timerData.value.timerState == TimerState.Running) {
            timerJob?.cancel()
            saveSession() // Save the session when reset during running state
        }
        val totalDurationMillis = getDurationMillis(_timerData.value.timerType)
        _timerData.update {
            it.copy(
                remainingTimeMillis = totalDurationMillis,
                totalDurationMillis = totalDurationMillis,
                timerState = TimerState.Stopped
            )
        }
    }

    fun setTimerType(timerType: TimerType) {
        if (_timerData.value.timerState == TimerState.Running) {
            timerJob?.cancel()
            cancelAlarm()

            saveSession() // Save the session before changing the timer type
        }
        val durationMillis = getDurationMillis(timerType)
        _timerData.update {
            it.copy(
                timerType = timerType,
                totalDurationMillis = durationMillis,
                remainingTimeMillis = durationMillis,
                timerState = TimerState.Stopped
            )
        }
    }

    fun refreshDurations() {
        val durationMillis = getDurationMillis(_timerData.value.timerType)
        _timerData.update {
            it.copy(
                totalDurationMillis = durationMillis,
                remainingTimeMillis = durationMillis
            )
        }
    }

    private fun onTimerFinished() {
        _timerData.update { it.copy(timerState = TimerState.Stopped) }
        saveSession()
//        showNotification()

    }

    private fun saveSession() {
        val totalDurationMillis = _timerData.value.totalDurationMillis
        val remainingTimeMillis = _timerData.value.remainingTimeMillis
        val elapsedTimeMillis = totalDurationMillis - remainingTimeMillis
        val elapsedMinutes = elapsedTimeMillis / 1000 / 60

        if (elapsedMinutes > 0) {
            val session = Session(
                timestamp = System.currentTimeMillis(),
                workduration = if (_timerData.value.timerType == TimerType.Pomodoro)
                    elapsedMinutes else 0,
                breakduration = if (_timerData.value.timerType != TimerType.Pomodoro)
                    elapsedMinutes else 0
            )
            viewModelScope.launch {
                try {
                    insertSessionUseCase(session)
                    Log.d("TimerViewModel", "Session saved: $session")
                } catch (e: Exception) {
                    Log.e("TimerViewModel", "Error saving session", e)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

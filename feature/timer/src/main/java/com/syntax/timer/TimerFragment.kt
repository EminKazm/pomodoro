package com.syntax.timer

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.syntax.core.utils.AnimationUtils
import com.syntax.core.utils.ColorUtils
import com.syntax.timer.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimerViewModel by viewModels()
    private val startColor = Color.parseColor("#00FF00") // Green
    private val endColor = Color.parseColor("#FF0000") // Red
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupTabLayout()
        binding.imgTomato.setImageResource(R.drawable.tomato_unripe)


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timerData.collectLatest { timerData ->
                val remainingTimeMillis = timerData.remainingTimeMillis
                binding.textViewTimer.text = formatTime(remainingTimeMillis)

                val fraction = calculateFraction(timerData)
                showMotivationalMessage(fraction)

                val interpolatedColor = ColorUtils.interpolateColor(startColor, endColor, fraction)
                binding.imgTomato.setColorFilter(interpolatedColor)
                if (timerData.timerType == TimerType.Pomodoro) {
                    binding.textViewMotivation.visibility = View.VISIBLE
                    showMotivationalMessage(fraction)
                } else {
                    binding.textViewMotivation.visibility = View.GONE
                }
                when (timerData.timerState) {
                    TimerState.Running -> {
                        binding.buttonStartPause.text = "Pause"
                        AnimationUtils.applyPulsingAnimation(binding.imgTomato)
                    }
                    TimerState.Stopped -> {
                        binding.buttonStartPause.text = "Start"
                        AnimationUtils.stopAnimation(binding.imgTomato)
                        binding.imgTomato.setColorFilter(startColor)
                    }
                    TimerState.Paused -> {
                        binding.buttonStartPause.text = "Resume"
                        AnimationUtils.stopAnimation(binding.imgTomato)
                    }
                }

                updateSelectedTab(timerData.timerType)
            }
        }

        binding.buttonStartPause.setOnClickListener {
            when (viewModel.timerData.value.timerState) {
                TimerState.Stopped, TimerState.Paused -> viewModel.startTimer()
                TimerState.Running -> viewModel.pauseTimer()
            }
        }

        binding.buttonReset.setOnClickListener {
            viewModel.resetTimer()
        }
    }

    private fun setupTabLayout() {
        val tabLayout = binding.tabLayoutTimer

        tabLayout.addTab(tabLayout.newTab().setText("Pomodoro"))
        tabLayout.addTab(tabLayout.newTab().setText("Short Break"))
        tabLayout.addTab(tabLayout.newTab().setText("Long Break"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.setTimerType(TimerType.Pomodoro)
                    1 -> viewModel.setTimerType(TimerType.ShortBreak)
                    2 -> viewModel.setTimerType(TimerType.LongBreak)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }

    private fun updateSelectedTab(timerType: TimerType) {
        val tabLayout = binding.tabLayoutTimer
        val tabIndex = when (timerType) {
            TimerType.Pomodoro -> 0
            TimerType.ShortBreak -> 1
            TimerType.LongBreak -> 2
        }
        val selectedTab = tabLayout.getTabAt(tabIndex)
        if (selectedTab != null && !selectedTab.isSelected) {
            selectedTab.select()
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = millis / (60 * 1000)
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun calculateFraction(timerData: TimerData): Float {
        val elapsedTime = timerData.totalDurationMillis - timerData.remainingTimeMillis
        return if (timerData.totalDurationMillis > 0) {
            (elapsedTime.toFloat() / timerData.totalDurationMillis).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    private fun showMotivationalMessage(fraction: Float) {
        val message = when {
            fraction < 0.25 -> "Great start! Keep focusing."
            fraction in 0.25..0.5 -> "Halfway there! Stay strong."
            fraction in 0.5..0.75 -> "Almost done! Don't give up."
            else -> "Final stretch! Finish strong."
        }
        binding.textViewMotivation.text = message
    }

    override fun onPause() {
        super.onPause()
        AnimationUtils.stopAnimation(binding.imgTomato)
    }
    override fun onResume() {
        super.onResume()
//        viewModel.refreshDurations()
        when (viewModel.timerData.value.timerState) {
            TimerState.Running -> {
                AnimationUtils.applyPulsingAnimation(binding.imgTomato)
            }
            else -> {
                AnimationUtils.stopAnimation(binding.imgTomato)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

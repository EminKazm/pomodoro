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

        // Set up the TabLayout
        setupTabLayout()
        binding.imgTomato.setImageResource(R.drawable.tomato_unripe)

        // Observe timer data
        // Observe timer data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timerData.collectLatest { timerData ->
                // Update the timer text
                val remainingTimeMillis = timerData.remainingTimeMillis
                binding.textViewTimer.text = formatTime(remainingTimeMillis)

                // Calculate the fraction of time elapsed
                val fraction = calculateFraction(timerData)
                showMotivationalMessage(fraction)

                // Interpolate color based on fraction
                val interpolatedColor = ColorUtils.interpolateColor(startColor, endColor, fraction)
                binding.imgTomato.setColorFilter(interpolatedColor)
// Conditionally show/hide and update motivational messages
                if (timerData.timerType == TimerType.Pomodoro) {
                    // Show motivational message
                    binding.textViewMotivation.visibility = View.VISIBLE
                    showMotivationalMessage(fraction)
                } else {
                    // Hide motivational message
                    binding.textViewMotivation.visibility = View.GONE
                }
                // Optionally, apply pulsing animation when running
                when (timerData.timerState) {
                    TimerState.Running -> {
                        binding.buttonStartPause.text = "Pause"
                        // Apply pulsing animation
                        AnimationUtils.applyPulsingAnimation(binding.imgTomato)
                    }
                    TimerState.Stopped -> {
                        binding.buttonStartPause.text = "Start"
                        // Stop animation and reset color
                        AnimationUtils.stopAnimation(binding.imgTomato)
                        binding.imgTomato.setColorFilter(startColor)
                    }
                    TimerState.Paused -> {
                        binding.buttonStartPause.text = "Resume"
                        // Stop animation
                        AnimationUtils.stopAnimation(binding.imgTomato)
                    }
                }

                // Update the selected tab if necessary
                updateSelectedTab(timerData.timerType)
            }
        }

        // Set up button listeners
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

        // Add tabs to the TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Pomodoro"))
        tabLayout.addTab(tabLayout.newTab().setText("Short Break"))
        tabLayout.addTab(tabLayout.newTab().setText("Long Break"))

        // Set tab selection listener
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
                // Optionally handle reselection
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
        // Stop any ongoing animations when the fragment is paused
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

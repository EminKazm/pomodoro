package com.syntax.timer

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.syntax.timer.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimerViewModel by viewModels()
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imgTomato.setImageResource(R.drawable.tomato_ripening_animation)
        animatedVectorDrawable = binding.imgTomato.drawable as? AnimatedVectorDrawable
        // Set up the TabLayout
        setupTabLayout()

        // Observe timer data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timerData.collectLatest { timerData ->
                // Update the timer text
                val remainingTimeMillis = timerData.remainingTimeMillis
                binding.textViewTimer.text = formatTime(remainingTimeMillis)

                // Update the button text
                when (timerData.timerState) {
                    TimerState.Stopped -> {
                        // Update button text to "Start"
                        binding.buttonStartPause.text = "Start"
                        // Stop animation
                        animatedVectorDrawable?.stop()
                    }
                    TimerState.Running -> {
                        // Update button text to "Pause"
                        binding.buttonStartPause.text = "Pause"
                        // Start animation
                        animatedVectorDrawable?.start()
                    }
                    TimerState.Paused -> {
                        // Update button text to "Resume"
                        binding.buttonStartPause.text = "Resume"
                        // Stop animation
                        animatedVectorDrawable?.stop()
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
    override fun onResume() {
        super.onResume()
        viewModel.refreshDurations()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

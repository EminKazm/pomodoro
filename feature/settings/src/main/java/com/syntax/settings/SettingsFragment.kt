package com.syntax.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.syntax.settings.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    // Minimum values for SeekBars if needed
    private val minPomodoro = 1
    private val minShortBreak = 1
    private val minLongBreak = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSeekBars()
        binding.buttonSaveSettings.setOnClickListener {
            viewModel.saveSettings()
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun setupSeekBars() {
        // Observe ViewModel data and set initial values
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pomodoroDuration.collectLatest { duration ->
                binding.seekBarPomodoro.progress = (duration - minPomodoro).toInt()
                binding.textViewPomodoroLabel.text = "Pomodoro Duration: $duration minutes"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shortBreakDuration.collectLatest { duration ->
                binding.seekBarShortBreak.progress = (duration - minShortBreak).toInt()
                binding.textViewShortBreakLabel.text = "Short Break Duration: $duration minutes"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.longBreakDuration.collectLatest { duration ->
                binding.seekBarLongBreak.progress = (duration - minLongBreak).toInt()
                binding.textViewLongBreakLabel.text = "Long Break Duration: $duration minutes"
            }
        }
        setupSeekBarListeners()
    }

    private fun setupSeekBarListeners() {
        binding.seekBarPomodoro.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val actualProgress = progress + minPomodoro
                binding.textViewPomodoroLabel.text = "Pomodoro Duration: $actualProgress minutes"
                viewModel.setPomodoroDuration(actualProgress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }
        })
        binding.seekBarShortBreak.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val actualProgress = progress + minShortBreak
                binding.textViewShortBreakLabel.text = "Short Break Duration: $actualProgress minutes"
                viewModel.setShortBreakDuration(actualProgress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.seekBarLongBreak.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val actualProgress = progress + minLongBreak
                binding.textViewLongBreakLabel.text = "Long Break Duration: $actualProgress minutes"
                viewModel.setLongBreakDuration(actualProgress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

}
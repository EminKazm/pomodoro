package com.syntax.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.syntax.domain.entities.Session
import com.syntax.stats.databinding.FragmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.recyclerViewSessions.layoutManager = LinearLayoutManager(requireContext())
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.sessions.collect { sessions ->
//                    // Update the RecyclerView adapter
//                    binding.recyclerViewSessions.adapter = SessionAdapter(sessions)
//                    // Update total sessions text
//                    binding.textViewStats.text = "Total Sessions: ${sessions.size}"
//                }
//            }
//        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessions.collect { sessions ->
                    if (sessions.isNotEmpty()) {
                        setupLineChart(sessions)
                        setupBarChart(sessions)
                        setupPieChart(sessions)
                    } else {
                        // Handle empty data scenario
                        displayNoDataMessage()
                    }
                }

            }
        }
    }
    private fun setupLineChart(sessions: List<Session>) {
        val entries = sessions.mapIndexed { index, session ->
            val totalDuration = session.workduration + session.breakduration
            Entry(index.toFloat(), totalDuration.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "Total Session Duration (min)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val lineData = LineData(lineDataSet)

        binding.lineChart.data = lineData
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.description.isEnabled = false
        binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.lineChart.xAxis.granularity = 1f
        binding.lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < sessions.size) {
                    val date = Date(sessions[index].timestamp)
                    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                    sdf.format(date)
                } else {
                    ""
                }
            }
        }
        binding.lineChart.invalidate()
    }
    private fun setupBarChart(sessions: List<Session>) {
        val entries = sessions.mapIndexed { index, session ->
            BarEntry(index.toFloat(), session.workduration.toFloat())
        }

        val barDataSet = BarDataSet(entries, "Work Duration (min)").apply {
            color = Color.GREEN
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val barData = BarData(barDataSet)

        binding.barChart.data = barData
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.barChart.xAxis.granularity = 1f
        binding.barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < sessions.size) {
                    val date = Date(sessions[index].timestamp)
                    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                    sdf.format(date)
                } else {
                    ""
                }
            }
        }
        binding.barChart.invalidate()
    }
    private fun setupPieChart(sessions: List<Session>) {
        val totalWork = sessions.sumOf { it.workduration.toDouble() }
        val totalBreak = sessions.sumOf { it.breakduration.toDouble() }

        val entries = listOf(
            PieEntry(totalWork.toFloat(), "Work"),
            PieEntry(totalBreak.toFloat(), "Break")
        )

        val pieDataSet = PieDataSet(entries, "Work vs Break").apply {
            colors = listOf(Color.BLUE, Color.RED)
            valueTextColor = Color.WHITE
            valueTextSize = 16f
        }

        val pieData = PieData(pieDataSet)

        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.isDrawHoleEnabled = false
        binding.pieChart.invalidate()
    }

    private fun displayNoDataMessage() {
        // Display a message or placeholder
        binding.lineChart.clear()
        binding.barChart.clear()
        binding.pieChart.clear()
        Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

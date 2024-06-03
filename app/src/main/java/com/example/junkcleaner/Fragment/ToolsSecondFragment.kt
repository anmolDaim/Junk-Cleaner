package com.example.junkcleaner.Fragment

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import com.example.junkcleaner.databinding.FragmentToolsSecondBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Random


class ToolsSecondFragment : Fragment() {


    private var _binding: FragmentToolsSecondBinding? = null
    private val binding get() = _binding!!


    private val downloadEntries = ArrayList<Entry>()
    private val uploadEntries = ArrayList<Entry>()
    private var index = 0


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsSecondBinding.inflate(inflater, container, false)


        setupChart()
        loadGraphData()

        binding.startTestButton.setOnClickListener {
            measureAndDisplaySpeed()

        }

        return binding.root
    }
    private fun setupChart() {
        binding.lineChart.description.isEnabled = false
        binding.lineChart.setTouchEnabled(true)
        binding.lineChart.isDragEnabled = true
        binding.lineChart.setScaleEnabled(true)
        binding.lineChart.setPinchZoom(true)

        // Set X Axis text color to white
        val xAxis: XAxis = binding.lineChart.xAxis
        xAxis.textColor = Color.WHITE

        // Set Y Axis text color to white
        val yAxisLeft: YAxis = binding.lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE

        val yAxisRight: YAxis = binding.lineChart.axisRight
        yAxisRight.textColor = Color.WHITE

        // Customize legend
        val l = binding.lineChart.legend
        l.form = Legend.LegendForm.LINE
        l.textColor = Color.WHITE
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun measureAndDisplaySpeed() {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)
        val downSpeed = (nc?.linkDownstreamBandwidthKbps)?.div(1000)
        val upSpeed = (nc?.linkUpstreamBandwidthKbps)?.div(1000)

        if (downSpeed != null) {
            binding.speedometer.setSpeed(downSpeed.toInt(), 1000L)
        }
        binding.downloadSpeedTextView.text = "Download Speed: $downSpeed Mbps"
        binding.uploadSpeedTextView.text = "Upload Speed: $upSpeed Mbps"

        getSpeedAndUpdateChart(downSpeed, upSpeed)
//        //  Toast.makeText(requireContext(), "Up Speed: $upSpeed Mbps \nDown Speed: $downSpeed Mbps", Toast.LENGTH_LONG).show()
//
//        // Update graphs
//        if (downSpeed != null) {
//            downloadSeries.appendData(DataPoint(lastXValue, downSpeed.toDouble()), true, 100)
//        }
//        if (upSpeed != null) {
//            uploadSeries.appendData(DataPoint(lastXValue, upSpeed.toDouble()), true, 100)
//        }
//
//        // Increment X value
//        lastXValue += 1
    }

    private fun getSpeedAndUpdateChart(downSpeed: Int?, upSpeed: Int?) {        // Simulate download and upload speeds
        if (downSpeed!= null && upSpeed!= null) {
            downloadEntries.add(Entry(index.toFloat(), downSpeed.toFloat()))
            uploadEntries.add(Entry(index.toFloat(), upSpeed.toFloat()))
            index++

            val downloadDataSet = LineDataSet(downloadEntries, "Download Speed").apply {
                color = resources.getColor(android.R.color.holo_blue_dark)
                setCircleColor(resources.getColor(android.R.color.holo_blue_dark))
            }

            val uploadDataSet = LineDataSet(uploadEntries, "Upload Speed").apply {
                color = resources.getColor(android.R.color.holo_green_dark)
                setCircleColor(resources.getColor(android.R.color.holo_green_dark))
            }

            val lineData = LineData(downloadDataSet, uploadDataSet)
            binding.lineChart.data = lineData
            binding.lineChart.invalidate() // Refresh the chart

            saveGraphData()
        }
    }

    private fun saveGraphData() {
        val sharedPreferences = requireContext().getSharedPreferences("graph_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save download data
        val downloadData = downloadEntries.map { "${it.x},${it.y}" }.joinToString("|")
        editor.putString("download_data", downloadData)

        // Save upload data
        val uploadData = uploadEntries.map { "${it.x},${it.y}" }.joinToString("|")
        editor.putString("upload_data", uploadData)

        editor.putInt("index", index)
        editor.apply()
    }
    private fun loadGraphData() {
        val sharedPreferences = requireContext().getSharedPreferences("graph_data", Context.MODE_PRIVATE)

        // Load download data
        val downloadData = sharedPreferences.getString("download_data", null)
        if (downloadData != null) {
            val entries = downloadData.split("|").map {
                val values = it.split(",")
                Entry(values[0].toFloat(), values[1].toFloat())
            }
            downloadEntries.clear()
            downloadEntries.addAll(entries)
        }

        // Load upload data
        val uploadData = sharedPreferences.getString("upload_data", null)
        if (uploadData != null) {
            val entries = uploadData.split("|").map {
                val values = it.split(",")
                Entry(values[0].toFloat(), values[1].toFloat())
            }
            uploadEntries.clear()
            uploadEntries.addAll(entries)
        }

        index = sharedPreferences.getInt("index", 0)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
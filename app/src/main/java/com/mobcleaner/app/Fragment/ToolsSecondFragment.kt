package com.mobcleaner.app.Fragment


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobcleaner.app.databinding.FragmentToolsSecondBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ToolsSecondFragment : Fragment() {

    private var _binding: FragmentToolsSecondBinding? = null
    private val binding get() = _binding!!

    private val downloadEntries = ArrayList<Entry>()
    private val uploadEntries = ArrayList<Entry>()
    private var index = 0

    // Store the last measured speeds
    private var lastDownSpeed: Float? = null
    private var lastUpSpeed: Float? = null

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

    private fun measureAndDisplaySpeed() {
        val url = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"  // Replace with your actual URL
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val downSpeed = connection.inputStream.readBytes().size.toFloat() / 1024  // Simulated download speed in KB
                val upSpeed = downSpeed / 2  // Simulated upload speed as half of download speed for example

                withContext(Dispatchers.Main) {
                    displaySpeed(downSpeed, upSpeed)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch speeds", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun displaySpeed(downSpeed: Float, upSpeed: Float) {
        val formattedDownSpeed = String.format("%.2f", downSpeed)
        val formattedUpSpeed = String.format("%.2f", upSpeed)

        binding.downloadSpeedTextView.text = "Download Speed: $formattedDownSpeed Mbps"
        binding.uploadSpeedTextView.text = "Upload Speed: $formattedUpSpeed Mbps"

        // Update speedometer
        binding.speedometer.setSpeed(downSpeed.toInt(), 1000L)

        // Update line chart
        getSpeedAndUpdateChart(downSpeed, upSpeed)

        binding.speedometer.maxSpeed = 100
        binding.speedometer.textColor = Color.WHITE
    }


    private fun getSpeedAndUpdateChart(downSpeed: Float?, upSpeed: Float?) {
        if (downSpeed != null && upSpeed != null) {
            downloadEntries.add(Entry(index.toFloat(), downSpeed))
            uploadEntries.add(Entry(index.toFloat(), upSpeed))
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
            binding.lineChart.invalidate()

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

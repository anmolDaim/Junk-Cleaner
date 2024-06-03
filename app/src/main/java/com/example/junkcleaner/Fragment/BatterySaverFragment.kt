package com.example.junkcleaner.Fragment

import android.app.ApplicationErrorReport.BatteryInfo
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import androidx.core.content.ContextCompat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.junkcleaner.Activity.AppDiaryActivity
import com.example.junkcleaner.Activity.AppManagerActivity
import com.example.junkcleaner.Activity.BatteryInfoActivity
import com.example.junkcleaner.Activity.BatteryUsageActivity
import com.example.junkcleaner.Activity.BigFileActivity
import com.example.junkcleaner.Activity.GameAssistantActivity
import com.example.junkcleaner.Activity.NetworkTrafficActivity
import com.example.junkcleaner.Adapter.BatteryCategoryAdapter
import com.example.junkcleaner.Adapter.TabPagerAdapter
import com.example.junkcleaner.Adapter.batteyManagementAdapter
import com.example.junkcleaner.BluetoothStatusListener
import com.example.junkcleaner.DataClass.batteryCategoryDataClass
import com.example.junkcleaner.DataClass.batteryManagementDataClass
import com.example.junkcleaner.MobileDataStatusListener
import com.example.junkcleaner.R
import com.example.junkcleaner.Receiver.BluetoothReceiver
import com.example.junkcleaner.Receiver.MobileDataReceiver
import com.example.junkcleaner.Receiver.RotationReceiver
import com.example.junkcleaner.Receiver.SilentReceiver
import com.example.junkcleaner.Receiver.WifiReceiver
import com.example.junkcleaner.RotationStatusListener
import com.example.junkcleaner.SilentStatusListener
import com.example.junkcleaner.databinding.FragmentBatterySaverBinding
import com.example.junkcleaner.databinding.FragmentTodayBinding
import com.example.junkcleaner.wifiStatusListener
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar


class BatterySaverFragment : Fragment(),MobileDataStatusListener,
    wifiStatusListener,
    BluetoothStatusListener,
    RotationStatusListener,
    SilentStatusListener {

//    private lateinit var viewPager: ViewPager
//    private lateinit var viewPagerIndicator: LinearLayout
//    private lateinit var batteryCategoryRecyclerView:RecyclerView
//    private lateinit var batteryManagementRecyclerView:RecyclerView
//    private lateinit var progressBar:ProgressBar
private val batteryTemperatureData = mutableListOf<Pair<Long, Float>>()
private val batteryUsageData = mutableListOf<Pair<Long, Int>>()
    private lateinit var mobileDataReceiver: MobileDataReceiver
    private lateinit var wifiReceiver:WifiReceiver
    private lateinit var bluetoothReceiver:BluetoothReceiver
    private lateinit var rotationReceiver:RotationReceiver
    private lateinit var binding:FragmentBatterySaverBinding
    private lateinit var silentReceiver:SilentReceiver


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBatterySaverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mobileDataReceiver = MobileDataReceiver(this)
        wifiReceiver = WifiReceiver(this)
        bluetoothReceiver = BluetoothReceiver(this)
        rotationReceiver = RotationReceiver(this)
        silentReceiver =SilentReceiver(this)


        // Setup ViewPager with TabLayout
        val tabPagerAdapter = TabPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = tabPagerAdapter

        // Add page change listener to update the indicator
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Do nothing
            }

            override fun onPageSelected(position: Int) {
                updateIndicator(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Do nothing
            }
        })

        binding.likeBtn.setOnClickListener(){
            val url = "https://play.google.com/store/apps/details?id=com.mobnews.app" // Replace this URL with your actual rate us URL

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }


        binding.settingBtn.setOnClickListener(){
            val profileFragment = ProfileFragment() // Replace ProfileFragment with your actual profile fragment class
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, profileFragment) // Replace R.id.fragment_container with the ID of the container where fragments are hosted
            transaction.addToBackStack(null) // This allows users to navigate back to the previous fragment by pressing the back button
            transaction.commit()
        }

        binding.shareBtn.setOnClickListener(){
            val urlToShare = "https://play.google.com/store/apps/details?id=com.mobnews.app"
            // Define the text you want to share
            val shareText = "Check out this amazing app! $urlToShare"

            // Create an ACTION_SEND intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            // Create a chooser intent to let the user select the app to share the content
            val chooserIntent = Intent.createChooser(shareIntent, "Share with...")

            // Try to launch the chooser
            try {
                startActivity(chooserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No application available to share content", Toast.LENGTH_SHORT).show()
            }
        }


        //widget creation
        binding.mobiledata.setOnClickListener(){
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            startActivity(intent)
        }
        binding.wifi.setOnClickListener {
            // Code to move to the Wifi settings screen
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        binding.aeroplane.setOnClickListener {
            // Code to enable/disable Airplane mode
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            startActivity(intent)
        }

        binding.location.setOnClickListener {
            // Code to enable/disable Location services
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        binding.silent.setOnClickListener {
            // Code to enable/disable Silent mode
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
            startActivity(intent)
        }
        binding.rotation.setOnClickListener {
            // Code to enable/disable Auto-rotate screen
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            startActivity(intent)
        }

        binding.bluetooth.setOnClickListener {
            // Code to move to the Bluetooth settings screen
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }

        binding.settings.setOnClickListener {
            // Code to move to the App settings screen
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
//battery usage graph
        setupLineChart()
        generateDummyBatteryUsageData()
        plotBatteryUsageData()
        setUptempLineChart()
        generateDummyBatteryTemperatureData()
        plotBatteryTemperatureData()


        // Populate the indicator initially
        updateIndicator(0)

        val array=ArrayList<batteryCategoryDataClass>()
        array.add(batteryCategoryDataClass(R.drawable.junk_file_icon,"Big File Cleaner","Check your Files Storage"))
        array.add(batteryCategoryDataClass(R.drawable.process_manager_icon,"Game Assistant","Gamings"))
        array.add(batteryCategoryDataClass(R.drawable.cold_shower_icon,"App Manager","Manage your app"))
        array.add(batteryCategoryDataClass(R.drawable.power_mode_icon,"Recent App Usage","Recently used app"))

        val layout=GridLayoutManager(requireContext(),2)
        binding.batteryCategoryRecyclerView.layoutManager=layout
val adapter=BatteryCategoryAdapter(array){ categoryName ->
    // Handle item click here and navigate to the appropriate fragment
    when (categoryName) {
        "Big File Cleaner" -> bigFileCleaner(categoryName)
        "Game Assistant" -> gamingAssistent(categoryName)
        "App Manager" -> appManager(categoryName)
        "Recent App Usage" -> recentAppUsage(categoryName)

    }
}
        binding.batteryCategoryRecyclerView.adapter=adapter

        val secArr=ArrayList<batteryManagementDataClass>()
        secArr.add(batteryManagementDataClass(R.drawable.battery_manager_icon,"Battery Info"))
        secArr.add(batteryManagementDataClass(R.drawable.schedule_mode_icon,"Network Traffic"))
        secArr.add(batteryManagementDataClass(R.drawable.battery_usage_icon,"Battery Usage"))
        val gridlayout=GridLayoutManager(requireContext(),3)
        binding.batteryManagementRecyclerView.layoutManager=gridlayout
        val gridadapter=batteyManagementAdapter(secArr){
            categoryName->
            when(categoryName){
                "Battery Info" ->batteryInfo()
                "Network Traffic" -> NetworkTraffic()
                "Battery Usage" -> batteryUsage()
            }
        }
        binding.batteryManagementRecyclerView.adapter=gridadapter


    }

    private fun batteryUsage() {
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(),BatteryUsageActivity::class.java)
        startActivity(intent)
    }

    private fun NetworkTraffic(){
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(),NetworkTrafficActivity::class.java)
        startActivity(intent)
    }

    private fun batteryInfo() {
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(),BatteryInfoActivity::class.java)
        startActivity(intent)
    }

    private fun recentAppUsage(categoryName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(), AppDiaryActivity::class.java)
        startActivity(intent)
    }

    private fun appManager(categoryName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(), AppManagerActivity::class.java)
        startActivity(intent)
    }

    private fun gamingAssistent(categoryName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val intent=Intent(requireContext(), GameAssistantActivity::class.java)
        startActivity(intent)
    }

    private fun bigFileCleaner(categoryName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val intent= Intent(requireContext(), BigFileActivity::class.java)
        startActivity(intent)

    }

    private fun updateIndicator(position: Int) {
        binding.viewPagerIndicator.removeAllViews() // Clear existing views

        val numPages = binding.viewPager.adapter?.count ?: 0
        val dotSize = resources.getDimensionPixelSize(R.dimen.dot_size)
        val dotMargin = resources.getDimensionPixelSize(R.dimen.dot_margin)

        val layoutParams = LinearLayout.LayoutParams(dotSize, dotSize)
        layoutParams.setMargins(dotMargin, 0, dotMargin, 0)

        for (i in 0 until numPages) {
            val dot = ImageView(requireContext())
            dot.setImageResource(if (i == position) R.drawable.selected_dot else R.drawable.unselected_dot)
            dot.layoutParams = layoutParams
            binding.viewPagerIndicator.addView(dot)
        }
    }
    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(mobileDataReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        requireContext().registerReceiver(wifiReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        requireContext().registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        //context?.registerReceiver(rotationReceiver, IntentFilter(Settings.ACTION_DISPLAY_SETTINGS))
        requireContext().registerReceiver(silentReceiver, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION))

        // Hide the progress bar when the fragment resumes
        binding.progressBar.visibility = View.GONE
    }
    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(mobileDataReceiver)
        requireContext().unregisterReceiver(wifiReceiver)
        requireContext().unregisterReceiver(bluetoothReceiver)
        requireContext().unregisterReceiver(rotationReceiver)
        requireContext().unregisterReceiver(silentReceiver)
    }
    private fun setupLineChart() {
        binding.lineChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun generateDummyBatteryUsageData() {
        // Simulate battery usage data for demonstration
        val calendar = Calendar.getInstance()
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val batteryLevel = (30..100).random()
            batteryUsageData.add(Pair(timestamp, batteryLevel))
        }
    }

    private fun plotBatteryUsageData() {
        val entries = ArrayList<Entry>()
        batteryUsageData.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Battery Level")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate() // Refresh chart
    }
    private fun setUptempLineChart() {
        binding.batteryTemperature.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }
    private fun generateDummyBatteryTemperatureData() {
        // Simulate battery temperature data for demonstration
        val calendar = Calendar.getInstance()
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val temperature = (25..40).random().toFloat()
            batteryTemperatureData.add(Pair(timestamp, temperature))
        }
    }

    private fun plotBatteryTemperatureData() {
        val entries = ArrayList<Entry>()
        batteryTemperatureData.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second))
        }

        val dataSet = LineDataSet(entries, "Battery Temperature")
        dataSet.color = Color.RED
        dataSet.setCircleColor(Color.RED)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.batteryTemperature.data = lineData
        binding.batteryTemperature.invalidate() // Refresh chart
    }

    override fun onMobileDataStatusChanged(isEnabled: Boolean) {
        if (isEnabled == true) {
            // Change background color of mobiledata view to enabled color
            binding.mobiledata.setBackgroundResource(R.drawable.optimize_btn)
            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_icon_white)
        } else {
            // Change background color of mobiledata view to grey
            binding.mobiledata.setBackgroundResource(R.drawable.remove_btn)
            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_grey)
        }
    }

    override fun onWifiStatusChanged(isEnabled: Boolean) {
        if (isEnabled) {
            binding.wifi.setBackgroundResource(R.drawable.optimize_btn)
            binding.wifiImage.setImageResource(R.drawable.mobiledata_icon_white)
        } else {
            // Change background color of mobiledata view to grey
            binding.wifi.setBackgroundResource(R.drawable.remove_btn)
            binding.wifiImage.setImageResource(R.drawable.mobiledata_grey)
        }
    }

    override fun onbluetoothStatusChanged(isEnabled: Boolean) {
        if (isEnabled) {
            binding.bluetooth.setBackgroundResource(R.drawable.optimize_btn)
            binding.bluetoothImage.setImageResource(R.drawable.mobiledata_icon_white)
        } else {
            // Change background color of mobiledata view to grey
            binding.bluetooth.setBackgroundResource(R.drawable.remove_btn)
            binding.bluetoothImage.setImageResource(R.drawable.mobiledata_grey)
        }
    }

    override fun onRotationStatusChanged(isEnabled: Boolean) {
        if (isEnabled) {
            // Change background color of rotation view to enabled color
            binding.rotation.setBackgroundResource(R.drawable.optimize_btn)
            binding.rotationImage.setImageResource(R.drawable.rotate_white)
        } else {
            // Change background color of rotation view to grey
            binding.rotation.setBackgroundResource(R.drawable.remove_btn)
            binding.rotationImage.setImageResource(R.drawable.rotate_grey)
        }
    }

    override fun onSilentStatusChanged(isEnabled: Boolean) {
        if (isEnabled) {
            binding.mobiledata.setBackgroundResource(R.drawable.optimize_btn)
            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_icon_white)
        } else {
            // Change background color of mobiledata view to grey
            binding.mobiledata.setBackgroundResource(R.drawable.remove_btn)
            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_grey)
        }
    }

}
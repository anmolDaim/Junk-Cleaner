package com.mobcleaner.app.Fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.mobcleaner.app.Activity.AppDiaryActivity
import com.mobcleaner.app.Activity.AppManagerActivity
import com.mobcleaner.app.Activity.BatteryInfoActivity
import com.mobcleaner.app.Activity.BatteryUsageActivity
import com.mobcleaner.app.Activity.DeviceStatusActivity
import com.mobcleaner.app.Activity.FileBigActivity
import com.mobcleaner.app.Activity.GameAssistantActivity
import com.mobcleaner.app.Activity.MainActivity
import com.mobcleaner.app.Activity.NetworkTrafficActivity
import com.mobcleaner.app.Adapter.BatteryCategoryAdapter
import com.mobcleaner.app.Adapter.TabPagerAdapter
import com.mobcleaner.app.Adapter.batteyManagementAdapter
import com.mobcleaner.app.DataClass.batteryCategoryDataClass
import com.mobcleaner.app.DataClass.batteryManagementDataClass
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.FragmentBatterySaverBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import java.util.Calendar


class BatterySaverFragment : Fragment() {

private val batteryTemperatureData = mutableListOf<Pair<Long, Float>>()
private val batteryUsageData = mutableListOf<Pair<Long, Int>>()
    private lateinit var binding:FragmentBatterySaverBinding
    private var nativeAd: NativeAd? = null
    private lateinit var adFrameLarge: FrameLayout


    // Flags to track registration state
    private var isMobileDataReceiverRegistered = false
    private var isWifiReceiverReceived = false
    private var isBluetoothReceiverRegistered = false
    private var isRotationReceiverRegistered = false
    private var isSilentReceiverRegistered = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBatterySaverBinding.inflate(inflater, container, false)
        loadNativeAd(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mobileDataReceiver = MobileDataReceiver(this)
//       // requireContext().registerReceiver(mobileDataReceiver,  IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
//        wifiReceiver = WifiReceiver(this)
//        bluetoothReceiver = BluetoothReceiver(this)
//        rotationReceiver = RotationReceiver(this)
//        silentReceiver =SilentReceiver(this)

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


        binding.settingBtn.setOnClickListener(){
            val drawerLayout = (activity as MainActivity).drawerLayout
            drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.shareBtn.setOnClickListener(){
            val urlToShare = "https://play.google.com/store/apps/details?id=com.mobcleaner.app"
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

        binding.deviceStatusBtn.setOnClickListener(){
            val intent=Intent(requireContext(),DeviceStatusActivity::class.java)
            startActivity(intent)
        }


        //widget creation
       // setupWidgetListeners()
//battery usage graph
        generateBatteryUsageData()
        //plotBatteryUsageData()
        setUptempLineChart()
        generateBatteryTemperatureData()
        plotBatteryTemperatureData()
        loadDeviceInfo()
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
    private fun loadDeviceInfo() {
        val deviceModel = Build.MODEL
        val osVersion = Build.VERSION.RELEASE
        binding.phoneModel.text = deviceModel
        binding.osVersion.text = osVersion
    }
//    private fun setupWidgetListeners() {
//        binding.mobiledata.setOnClickListener {
//            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
//            startActivity(intent)
//        }
//        binding.wifi.setOnClickListener {
//            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
//            startActivity(intent)
//        }
//        binding.aeroplane.setOnClickListener {
//            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
//            startActivity(intent)
//        }
//        binding.location.setOnClickListener {
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
//        }
//        binding.silent.setOnClickListener {
//            val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
//            startActivity(intent)
//        }
//        binding.rotation.setOnClickListener {
//            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
//            startActivity(intent)
//        }
//        binding.bluetooth.setOnClickListener {
//            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
//            startActivity(intent)
//        }
//        binding.settings.setOnClickListener {
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", requireContext().packageName, null)
//            intent.data = uri
//            startActivity(intent)
//        }
//    }

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
        val intent= Intent(requireContext(), FileBigActivity::class.java)
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
//        if (!isMobileDataReceiverRegistered) {
//            val mobileDataFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
//            requireContext().registerReceiver(mobileDataReceiver, mobileDataFilter)
//            isMobileDataReceiverRegistered = true
//        }
//        if (!isBluetoothReceiverRegistered) {
//            val bluetoothFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
//            requireContext().registerReceiver(bluetoothReceiver, bluetoothFilter)
//            isBluetoothReceiverRegistered = true
//        }
//        if (!isRotationReceiverRegistered) {
//            val rotationFilter = IntentFilter("android.intent.action.CONFIGURATION_CHANGED")
//            requireContext().registerReceiver(rotationReceiver, rotationFilter)
//            isRotationReceiverRegistered = true
//        }
//        if (!isSilentReceiverRegistered) {
//            val silentFilter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
//            requireContext().registerReceiver(silentReceiver, silentFilter)
//            isSilentReceiverRegistered = true
//        }
        // Hide the progress bar when the fragment resumes
        binding.progressBar.visibility = View.GONE
    }
//    override fun onPause() {
//        super.onPause()
//        if (isMobileDataReceiverRegistered) {
//            requireContext().unregisterReceiver(mobileDataReceiver)
//            isMobileDataReceiverRegistered = false
//        }
//        if (isBluetoothReceiverRegistered) {
//            requireContext().unregisterReceiver(bluetoothReceiver)
//            isBluetoothReceiverRegistered = false
//        }
//        if (isRotationReceiverRegistered) {
//            requireContext().unregisterReceiver(rotationReceiver)
//            isRotationReceiverRegistered = false
//        }
//        if (isSilentReceiverRegistered) {
//            requireContext().unregisterReceiver(silentReceiver)
//            isSilentReceiverRegistered = false
//        }
//    }
    private fun generateBatteryUsageData() {
        val batteryUsageData = mutableListOf<Entry>()

        // Get battery usage data for each day
        val calendar = Calendar.getInstance()
        for (i in 0 until 7) { // 7 days of data
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + 86400000 // 86400000 = 1 day in milliseconds

            // Get battery usage for this day
            val batteryUsage = getBatteryUsageForDay(dayStart, dayEnd)

            // Add data point to chart
            batteryUsageData.add(Entry(i.toFloat(), batteryUsage.toFloat()))

            Log.d("BatteryUsage", "Added data point: $i, $batteryUsage")
        }

        Log.d("BatteryUsage", "Data points: $batteryUsageData")

        // Set up line chart
        setupLineChart()

        // Add data to line chart
        val lineDataSet = LineDataSet(batteryUsageData, "Battery Usage")
        lineDataSet.color = Color.BLUE
        lineDataSet.setCircleColor(Color.WHITE)
        lineDataSet.lineWidth = 2f

        val lineData = LineData(lineDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()

        Log.d("BatteryUsage", "Chart data set: $lineData")
    }

    private fun getBatteryUsageForDay(dayStart: Long, dayEnd: Long): Int {
        val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val currentTime = System.currentTimeMillis()

        // Calculate the time elapsed since the start of the day
        val timeElapsed = currentTime - dayStart

        // Calculate the battery usage for the day based on the current level and time elapsed
        val batteryUsage = (currentLevel * timeElapsed) / (dayEnd - dayStart)

        Log.d("BatteryUsage", "Battery usage for day: $batteryUsage")

        return batteryUsage.toInt()
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = true // Set to true to see if the chart is rendering
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }
//    private fun plotBatteryUsageData() {
//        val entries = ArrayList<Entry>()
//        batteryUsageData.forEachIndexed { index, pair ->
//            entries.add(Entry(index.toFloat(), pair.second.toFloat()))
//        }
//
//        val dataSet = LineDataSet(entries, "Battery Level")
//        dataSet.color = Color.BLUE
//        dataSet.setCircleColor(Color.BLUE)
//        dataSet.lineWidth = 2f
//
//        val lineData = LineData(dataSet)
//        binding.lineChart.data = lineData
//        binding.lineChart.invalidate() // Refresh chart
//    }
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
    private fun generateBatteryTemperatureData() {
        // Get battery temperature data
        val calendar = Calendar.getInstance()
        val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val temperature = getBatteryTemperature(requireContext())
            batteryTemperatureData.add(Pair(timestamp, temperature))
        }
    }
    private fun getBatteryTemperature(context: Context): Float {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (temperature != -1) {
            // Convert temperature from tenths of a degree Celsius to degrees Celsius
            temperature / 10f
        } else {
            0f // Default to 0°C if temperature not available
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

//    override fun onMobileDataStatusChanged(isEnabled: Boolean) {
//        if (isEnabled == true) {
//            // Change background color of mobiledata view to enabled color
//            binding.mobiledata.setBackgroundResource(R.drawable.optimize_btn)
//            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_icon_white)
//        } else {
//            // Change background color of mobiledata view to grey
//            binding.mobiledata.setBackgroundResource(R.drawable.remove_btn)
//            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_grey)
//        }
//    }
//
//    override fun onWifiStatusChanged(isEnabled: Boolean) {
//        if (isEnabled) {
//            binding.wifi.setBackgroundResource(R.drawable.optimize_btn)
//            binding.wifiImage.setImageResource(R.drawable.mobiledata_icon_white)
//        } else {
//            // Change background color of mobiledata view to grey
//            binding.wifi.setBackgroundResource(R.drawable.remove_btn)
//            binding.wifiImage.setImageResource(R.drawable.mobiledata_grey)
//        }
//    }
//
//    override fun onbluetoothStatusChanged(isEnabled: Boolean) {
//        if (isEnabled) {
//            binding.bluetooth.setBackgroundResource(R.drawable.optimize_btn)
//            binding.bluetoothImage.setImageResource(R.drawable.mobiledata_icon_white)
//        } else {
//            // Change background color of mobiledata view to grey
//            binding.bluetooth.setBackgroundResource(R.drawable.remove_btn)
//            binding.bluetoothImage.setImageResource(R.drawable.mobiledata_grey)
//        }
//    }
//
//    override fun onRotationStatusChanged(isEnabled: Boolean) {
//        if (isEnabled) {
//            // Change background color of rotation view to enabled color
//            binding.rotation.setBackgroundResource(R.drawable.optimize_btn)
//            binding.rotationImage.setImageResource(R.drawable.rotate_white)
//        } else {
//            // Change background color of rotation view to grey
//            binding.rotation.setBackgroundResource(R.drawable.remove_btn)
//            binding.rotationImage.setImageResource(R.drawable.rotate_grey)
//        }
//    }
//
//    override fun onSilentStatusChanged(isEnabled: Boolean) {
//        if (isEnabled) {
//            binding.mobiledata.setBackgroundResource(R.drawable.optimize_btn)
//            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_icon_white)
//        } else {
//            // Change background color of mobiledata view to grey
//            binding.mobiledata.setBackgroundResource(R.drawable.remove_btn)
//            binding.mobiledataImage.setImageResource(R.drawable.mobiledata_grey)
//        }
//    }

    private fun loadNativeAd(inflater: LayoutInflater) {
        val adLoader = AdLoader.Builder(requireContext(),"ca-app-pub-3940256099942544/2247696110" )
            .forNativeAd { ad: NativeAd ->
                nativeAd?.destroy()
                nativeAd = ad
                val adView = inflater.inflate(R.layout.ad_large_native, binding.adFrameLarge, false) as NativeAdView
                populateLargeNativeAdView(ad, adView)
                binding.adFrameLarge.removeAllViews()
                binding.adFrameLarge.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, etc.
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
    private fun populateLargeNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.ad_headline_large)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action_large)
        adView.iconView = adView.findViewById(R.id.ad_app_icon_large)
        adView.bodyView = adView.findViewById(R.id.ad_body_large)
        adView.mediaView = adView.findViewById(R.id.ad_media_large) as MediaView

        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as AppCompatButton).text = nativeAd.callToAction

        nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
            adView.iconView?.visibility = View.VISIBLE
        } ?: run {
            adView.iconView?.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
    }

}
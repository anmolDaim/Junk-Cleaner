package com.mobcleaner.app.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobcleaner.app.Activity.AppDiaryActivity
import com.mobcleaner.app.Activity.AppManagerActivity
import com.mobcleaner.app.Activity.DeviceStatusActivity
import com.mobcleaner.app.Activity.FileBigActivity
import com.mobcleaner.app.Activity.GameAssistantActivity
import com.mobcleaner.app.Activity.NetworkTrafficActivity
import com.mobcleaner.app.Adapter.appCategoryToolAdapter
import com.mobcleaner.app.DataClass.appCategoryToolDataClass
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.FragmentToolsFirstBinding


class ToolsFirstFragment : Fragment() {

    private var _binding: FragmentToolsFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding= FragmentToolsFirstBinding.inflate(inflater,container,false)


        val itemArr=ArrayList<appCategoryToolDataClass>()
        itemArr.add(appCategoryToolDataClass(R.drawable.game_assistant,"Game Assistant"))
        itemArr.add(appCategoryToolDataClass(R.drawable.app_manager,"App Manager"))
        itemArr.add(appCategoryToolDataClass(R.drawable.app_diary,"App Diary"))
        itemArr.add(appCategoryToolDataClass(R.drawable.device,"Device Status"))
        itemArr.add(appCategoryToolDataClass(R.drawable.notification_cleaner,"Notification Cleaner"))
        itemArr.add(appCategoryToolDataClass(R.drawable.recent_app,"Recent App Usage"))

        val linearLayout= GridLayoutManager(requireContext(),3)
        binding.appCategoryToolRecyclerView.layoutManager=linearLayout
        val adapter= appCategoryToolAdapter(itemArr){ categoryName ->
            // Handle item click here and navigate to the appropriate fragment
            when (categoryName) {
                "Game Assistant" -> gameAssistant()
                "App Manager" -> appManager()
                "App Diary" -> appDiary()
                "Device Status" -> deviceStatus()
  //              "Notification Cleaner" -> notificationCleaner()
//                "Recent App Usage" -> recentappUsage()

            }
        }
        binding.appCategoryToolRecyclerView.adapter=adapter

        val  Arr=ArrayList<appCategoryToolDataClass>()
        Arr.add(appCategoryToolDataClass(R.drawable.photos,"Similar photos"))
        Arr.add(appCategoryToolDataClass(R.drawable.file_icon,"Big File Cleaner"))
        Arr.add(appCategoryToolDataClass(R.drawable.whtasapp,"WhatsApp Cleaner"))
        Arr.add(appCategoryToolDataClass(R.drawable.duplicate,"Duplicate file Cleaner"))
        Arr.add(appCategoryToolDataClass(R.drawable.folder,"Empty Folder Cleaner"))
        Arr.add(appCategoryToolDataClass(R.drawable.screenshot,"Screenshot Cleaner"))
        Arr.add(appCategoryToolDataClass(R.drawable.compressor,"Photo Compressor"))
        Arr.add(appCategoryToolDataClass(R.drawable.video_compressor,"Video Compressor"))

        val LinearLayout= GridLayoutManager(requireContext(),3)
        binding.freeUpSpaceRecyclerView.layoutManager=LinearLayout
        val categoryAdapter= appCategoryToolAdapter(Arr){ categoryName ->
            // Handle item click here and navigate to the appropriate fragment
            when (categoryName) {
//                "Game Assistant" -> gameAssistant(categoryName)
                "Big File Cleaner" -> bigFileCleaner(categoryName)
//                "App Diary" -> appDiary(categoryName)
//                "Device Status" -> deviceStatus(categoryName)
//                "Notification Cleaner" -> notificationCleaner(categoryName)
//                "Recent App Usage" -> recentappUsage(categoryName)

            }
        }
        binding.freeUpSpaceRecyclerView.adapter=categoryAdapter


        val networkArr=ArrayList<appCategoryToolDataClass>()
        networkArr.add(appCategoryToolDataClass(R.drawable.photos,"Network Traffic"))
        networkArr.add(appCategoryToolDataClass(R.drawable.file_icon,"Network Speed Test"))
        networkArr.add(appCategoryToolDataClass(R.drawable.whtasapp,"Wifi-Security"))

        val networkLayout= GridLayoutManager(requireContext(),3)
        binding.networkRecyclerView.layoutManager=networkLayout
        val networkAdapter= appCategoryToolAdapter(networkArr){
                categoryArr ->
            when(categoryArr) {


                "Network Traffic"->networkTraffic()
//            "Network Speed Test"->networkSpeedTest()
//            "Wifi-Security"-> wifiSecurity()
            }
        }

        binding.networkRecyclerView.adapter=networkAdapter


        val securityArr=ArrayList<appCategoryToolDataClass>()
        securityArr.add(appCategoryToolDataClass(R.drawable.lock,"App Lock"))
        securityArr.add(appCategoryToolDataClass(R.drawable.secure_browser,"Secure Browser"))
        securityArr.add(appCategoryToolDataClass(R.drawable.permission,"Permission Manager"))

        val securityLayout= GridLayoutManager(requireContext(),3)
        binding.securityRecyclerView.layoutManager=securityLayout
        val securtyAdapter= appCategoryToolAdapter(securityArr){
                categoryArr ->
            when(categoryArr) {


//                "Network Traffic"->networkTraffic()
//            "Network Speed Test"->networkSpeedTest()
//            "Wifi-Security"-> wifiSecurity()
            }
        }
        binding.securityRecyclerView.adapter=securtyAdapter

        return binding.root
    }


    private fun networkTraffic() {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), NetworkTrafficActivity::class.java)
        startActivity(intent)
    }

    private fun bigFileCleaner(categoryName: String) {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), FileBigActivity::class.java)
        startActivity(intent)
    }

    private fun deviceStatus() {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), DeviceStatusActivity::class.java)
        startActivity(intent)
    }

    private fun appDiary() {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), AppDiaryActivity::class.java)
        startActivity(intent)
    }

    private fun appManager() {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), AppManagerActivity::class.java)
        startActivity(intent)
    }

    private fun gameAssistant() {
        binding.toolsProgressBar.visibility=View.VISIBLE
        val intent= Intent(requireContext(), GameAssistantActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

}
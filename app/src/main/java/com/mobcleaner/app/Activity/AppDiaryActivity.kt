package com.mobcleaner.app.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat

import androidx.viewpager2.widget.ViewPager2
import com.mobcleaner.app.Adapter.ViewPagerAdapter
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.ActivityAppDiaryBinding
import com.google.android.material.tabs.TabLayout

class AppDiaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppDiaryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.tabLayout.apply {
            addTab(newTab().setText("Today"))
            addTab(newTab().setText("This Year"))
        }
        binding.backBtn.setOnClickListener(){
            super.onBackPressed()
        }
        binding.tabLayout.setTabTextColors(
            ContextCompat.getColor(this, R.color.white), // Unselected tab text color
            ContextCompat.getColor(this, R.color.blue) // Selected tab text color
        )

        binding.viewPagerTab.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
               if(tab!=null){
                   binding.viewPagerTab.currentItem=tab.position
               }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Not needed for this implementation
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Not needed for this implementation
            }
        })

        binding.viewPagerTab.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }
}
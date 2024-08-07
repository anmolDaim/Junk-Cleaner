package com.mobcleaner.mcapp.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobcleaner.mcapp.Fragment.ThisYearFragment
import com.mobcleaner.mcapp.Fragment.TodayFragment

class ViewPagerAdapter(fm:FragmentManager,lifecycle:Lifecycle): FragmentStateAdapter(fm,lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> ThisYearFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }


}
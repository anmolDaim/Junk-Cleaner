package com.mobcleaner.app.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobcleaner.app.Fragment.ColdShowerProcessManagFragment
import com.mobcleaner.app.Fragment.ToolsSecondFragment

class ToolsViewpagerAdapter(fragmentManager: FragmentManager,
                            lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
//            0 -> ToolsFirstFragment()
            0 -> ToolsSecondFragment()
            1 -> ColdShowerProcessManagFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
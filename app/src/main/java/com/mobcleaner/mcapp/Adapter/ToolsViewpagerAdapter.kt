package com.mobcleaner.mcapp.Adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobcleaner.mcapp.Fragment.ColdShowerProcessManagFragment
import com.mobcleaner.mcapp.Fragment.ToolsSecondFragment

class ToolsViewpagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fragments = mutableListOf<Fragment>(
        ToolsSecondFragment(),
        ColdShowerProcessManagFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun replaceFragment(fragment: Fragment, position: Int) {
        Log.d("ToolsViewpagerAdapter", "Replacing fragment at position $position")
        fragments[position] = fragment
        notifyItemChanged(position)
    }
}

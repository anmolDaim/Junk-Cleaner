package com.example.junkcleaner.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.junkcleaner.Fragment.OnePagerFragment
import com.example.junkcleaner.Fragment.TwoPagerFragment


class TabPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnePagerFragment()
            1 -> TwoPagerFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }

    override fun getCount(): Int {
        return 2
    }
}


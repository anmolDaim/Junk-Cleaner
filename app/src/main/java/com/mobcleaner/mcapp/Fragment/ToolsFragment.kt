package com.mobcleaner.mcapp.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.mobcleaner.mcapp.Adapter.ToolsViewpagerAdapter
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ToolsViewpagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ToolsViewpagerAdapter(childFragmentManager, lifecycle)
        binding.toolsViewPager.adapter = adapter

        binding.tabLayout.apply {
            addTab(newTab().setText("Check Network"))
            addTab(newTab().setText("Junk Cleaner"))
        }

        binding.tabLayout.setTabTextColors(
            ContextCompat.getColor(requireContext(), R.color.white), // Unselected tab text color
            ContextCompat.getColor(requireContext(), R.color.blue) // Selected tab text color
        )

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.toolsViewPager.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.toolsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    fun navigateToColdShowerFragment() {
        Log.d("ToolsFragment", "Navigating to ColdShowerProcessManagFragment")
        adapter.replaceFragment(ColdShowerProcessManagFragment(), 1)
        binding.toolsViewPager.setCurrentItem(1, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }
}

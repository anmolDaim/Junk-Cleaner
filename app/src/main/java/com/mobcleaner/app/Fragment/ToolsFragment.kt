package com.mobcleaner.app.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.mobcleaner.app.Adapter.ToolsViewpagerAdapter
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.FragmentToolsBinding
import com.google.android.material.tabs.TabLayout


class ToolsFragment : Fragment() {


    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)

//        val viewPager: ViewPager2 = binding.toolsViewPager
//        viewPager.adapter = ToolsViewpagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ToolsViewpagerAdapter(childFragmentManager, lifecycle)
        binding.tabLayout.apply {
            addTab(newTab().setText("Check Network"))
            addTab(newTab().setText("Junk Cleaner"))
        }
        binding.tabLayout.setTabTextColors(
            ContextCompat.getColor(requireContext(), R.color.white), // Unselected tab text color
            ContextCompat.getColor(requireContext(), R.color.blue) // Selected tab text color
        )
        binding.toolsViewPager.adapter = adapter
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!=null){
                    binding.toolsViewPager.currentItem=tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Not needed for this implementation
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Not needed for this implementation
            }
        })

        binding.toolsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

}
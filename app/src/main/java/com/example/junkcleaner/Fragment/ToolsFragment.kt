package com.example.junkcleaner.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.junkcleaner.Activity.AppDiaryActivity
import com.example.junkcleaner.Activity.AppManagerActivity
import com.example.junkcleaner.Activity.BigFileActivity
import com.example.junkcleaner.Activity.DeviceStatusActivity
import com.example.junkcleaner.Activity.GameAssistantActivity
import com.example.junkcleaner.Activity.NetworkTrafficActivity
import com.example.junkcleaner.Adapter.ToolsViewpagerAdapter
import com.example.junkcleaner.Adapter.ViewPagerAdapter
import com.example.junkcleaner.Adapter.appCategoryToolAdapter
import com.example.junkcleaner.DataClass.appCategoryToolDataClass
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.FragmentToolsBinding


class ToolsFragment : Fragment() {


    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)

        val viewPager: ViewPager2 = binding.toolsViewPager
        viewPager.adapter = ToolsViewpagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

}
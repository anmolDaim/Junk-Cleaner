package com.example.junkcleaner.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import com.example.junkcleaner.Activity.ColdShowerTempScannerActivity
import com.example.junkcleaner.R


class ColdShowerProcessManagFragment : Fragment() {
    private lateinit var coldShowerBtn:AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View=inflater.inflate(R.layout.fragment_cold_shower_process_manag,container,false)
        coldShowerBtn=view.findViewById(R.id.coldShowerBtn)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coldShowerBtn.setOnClickListener {
            val intent=Intent(requireContext(), ColdShowerTempScannerActivity()::class.java)
            startActivity(intent)
        }
    }
}
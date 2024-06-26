package com.mobcleaner.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetDialogFragment:BottomSheetDialogFragment() {
    private lateinit var binding:BottomSheetLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=BottomSheetLayoutBinding.inflate(layoutInflater)

        binding.cancelBtn.setOnClickListener(){
            dismiss()
        }

        binding.grantbtn.setOnClickListener(){
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, ColdShowerProcessManagFragment())
            fragmentTransaction.addToBackStack(null) // Optional: add to back stack to allow back navigation
            fragmentTransaction.commit()
        }
        return binding.root
    }


}
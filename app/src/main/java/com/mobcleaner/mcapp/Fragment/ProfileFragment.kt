package com.mobcleaner.mcapp.Fragment

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.mobcleaner.mcapp.Activity.PrivacyActivity
import com.mobcleaner.mcapp.Activity.TermsAndConditionsActivity
import com.mobcleaner.mcapp.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentProfileBinding.inflate(inflater,container,false)
//        sharedPreferences = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE)
//        editor = sharedPreferences?.edit()
//
//        sharedPreferences?.let {
//            if (it.getBoolean("isDarkTheme", false)) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            }
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.settingsProfile.setOnClickListener(){
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
        binding.termsProfile.setOnClickListener(){
            val intent= Intent(requireContext(),TermsAndConditionsActivity::class.java)
            startActivity(intent)
        }
        binding.privacyProfile.setOnClickListener(){
            val intent= Intent(requireContext(), PrivacyActivity::class.java)
            startActivity(intent)
        }
        binding.mailUsProfile.setOnClickListener(){
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com")) // Replace with your support email
                putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            }

            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            } else {
                Toast.makeText(requireContext(), "No email client found", Toast.LENGTH_SHORT).show()
            }
        }
        // Get the version name of the current app
        val versionName = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName

        // Set the version name as the text of the version TextView
        binding.version.text = "$versionName"

        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Dark mode is enabled
            binding.toggleDarkModeButton.text = "Light"
        } else {
            // Light mode is enabled
            binding.toggleDarkModeButton.text = "Dark"
        }
        binding.toggleDarkModeButton.setOnClickListener {
            toggleDarkMode()
        }

//        sharedPreferences?.let { binding.switchBtn.setChecked(it.getBoolean("isDarkTheme", false)) }
//
//        binding.switchBtn.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                editor?.putBoolean("isDarkTheme", true)
//            } else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                editor?.putBoolean("isDarkTheme", false)
//            }
//            editor?.apply()
//        }
    }

    private fun toggleDarkMode() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Dark mode is currently enabled, disable it
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.toggleDarkModeButton.text = "Dark"
        } else {
            // Dark mode is currently disabled, enable it
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.toggleDarkModeButton.text = "Light"
        }
    }

//    private fun toggleDarkMode() {
//        val currentMode = AppCompatDelegate.getDefaultNightMode()
//        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
//            // Dark mode is currently enabled, disable it
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        } else {
//            // Dark mode is currently disabled, enable it
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
//    }
}
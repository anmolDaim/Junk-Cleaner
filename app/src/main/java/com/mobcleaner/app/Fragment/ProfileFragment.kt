package com.mobcleaner.app.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mobcleaner.app.Activity.PrivacyActivity
import com.mobcleaner.app.Activity.TermsAndConditionsActivity
import com.mobcleaner.app.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentProfileBinding.inflate(inflater,container,false)
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
    }
}
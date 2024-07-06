package com.mobcleaner.mcapp.Activity

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.mobcleaner.mcapp.databinding.ActivityTermsAndConditionsBinding

class TermsAndConditionsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityTermsAndConditionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding=ActivityTermsAndConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.backBtn.setOnClickListener(){
            super.onBackPressed()
        }

        val url = "https://orilshorts.app/mob_cleaner_terms.html"

        // Initialize WebView
        val webSettings: WebSettings = binding.termsAndConditionsWebView.settings

        // Enable JavaScript
        webSettings.javaScriptEnabled = true

        // Set WebViewClient to handle links within the WebView
        binding.termsAndConditionsWebView.webViewClient = WebViewClient()

        // Set WebChromeClient to show progress
        binding.termsAndConditionsWebView.webChromeClient = WebChromeClient()

        // Load the URL into the WebView
        if (url != null) {
            binding.termsAndConditionsWebView.loadUrl(url)
        }

    }
}
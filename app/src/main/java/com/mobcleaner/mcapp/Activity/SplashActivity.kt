package com.mobcleaner.mcapp.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mobcleaner.mcapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash)
//        binding.videoView.setVideoURI(videoUri)
//
//        binding.videoView.setOnPreparedListener { mp ->
//            binding.placeholderImageView.visibility = View.GONE // Hide the placeholder image
//            //binding.videoView.visibility = View.VISIBLE
//            mp.isLooping = true // Optional, if you want the video to loop
//            binding.videoView.start()
//        }

        Handler(Looper.myLooper()!!).postDelayed({
            val intent = Intent(
                this@SplashActivity,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }, 4000)


    }
}
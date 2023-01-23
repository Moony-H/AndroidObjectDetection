package com.dpectrum.androidobjectdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dpectrum.androidobjectdetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
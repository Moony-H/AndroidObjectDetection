package com.dpectrum.androidobjectdetection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dpectrum.androidobjectdetection.databinding.FragmentCameraxTestBinding

class CameraXTestFragment:Fragment() {

    private var _binding:FragmentCameraxTestBinding?=null
    private val binding:FragmentCameraxTestBinding
        get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentCameraxTestBinding.inflate(layoutInflater,container,false)
        return binding.root

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }

}
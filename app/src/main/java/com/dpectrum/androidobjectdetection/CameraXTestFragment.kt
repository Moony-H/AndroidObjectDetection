package com.dpectrum.androidobjectdetection

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.dpectrum.androidobjectdetection.databinding.FragmentCameraxTestBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

        permissionLauncher.launch(permissions)
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive){
                binding.prevView.bitmap?.let {

                }
            }
        }
        return binding.root

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.prevView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private val permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        it.values.forEach { isGranted->
            if(!isGranted)
                return@registerForActivityResult
        }
        startCamera()
    }

    private val permissions=arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)

}
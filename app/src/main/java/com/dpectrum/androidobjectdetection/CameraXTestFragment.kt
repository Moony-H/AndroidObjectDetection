package com.dpectrum.androidobjectdetection

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dpectrum.androidobjectdetection.databinding.FragmentCameraxTestBinding

import kotlinx.coroutines.*
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class CameraXTestFragment : Fragment() {

    private var _binding: FragmentCameraxTestBinding? = null
    private val binding: FragmentCameraxTestBinding
        get() = _binding!!

    private val option = ObjectDetector.ObjectDetectorOptions.builder()
        .setMaxResults(6)
        .setScoreThreshold(0.6f)
        .build()

    private val objectDetector: ObjectDetector by lazy {
        ObjectDetector.createFromFileAndOptions(
            requireContext(), // the application context
            "model_v2.tflite", // must be same as the filename in assets folder
            option
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraxTestBinding.inflate(layoutInflater, container, false)

        permissionLauncher.launch(permissions)
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val bitmap = withContext(Dispatchers.Main) {
                    binding.prevView.bitmap
                }
                detectObject(bitmap)

            }
        }
        return binding.root

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
                    this, cameraSelector, preview
                )

            } catch (exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.values.forEach { isGranted ->
                if (!isGranted)
                    return@registerForActivityResult
            }
            startCamera()
        }

    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    private fun detectObject(bitmap: Bitmap?) {
        bitmap?.let {
            val image = TensorImage.fromBitmap(bitmap)
            val result = objectDetector.detect(image)
            debugPrint(result)
            val resultToDisplay = result.map {
                // Get the top-1 category and craft the display text
                val category = it.categories.first()
                val text = "${category.label}, ${category.score.times(100).toInt()}%"

                // Create a data object to display the detection result
                DetectionResult(it.boundingBox, text)
            }
// Draw the detection result on the bitmap and show it.
            val imgWithResult = drawDetectionResult(bitmap, resultToDisplay)
            CoroutineScope(Dispatchers.Main).launch {
                binding.imageView.setImageBitmap(imgWithResult)
            }

        }
    }

    private fun debugPrint(results: List<Detection>) {
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox

            Log.d("test", "Detected object: ${i} ")
            Log.d("test", "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d("test", "    Label $j: ${category.label}")
                val confidence: Int = category.score.times(100).toInt()
                Log.d("test", "    Confidence: ${confidence}%")
            }
        }
    }

    data class BoxWithText(val box: Rect, val text: String)

    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<DetectionResult>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        detectionResults.forEach {
            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
            canvas.drawRect(box, pen)


            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = 25f
            pen.getTextBounds(it.text, 0, it.text.length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            canvas.drawText(
                it.text, box.left + margin,
                box.top + tagSize.height().times(1F), pen
            )
        }
        return outputBitmap
    }
}

data class DetectionResult(val boundingBox: RectF, val text: String)

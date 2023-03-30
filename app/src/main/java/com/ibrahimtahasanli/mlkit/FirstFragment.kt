package com.ibrahimtahasanli.mlkit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.ibrahimtahasanli.mlkit.databinding.FragmentFirstBinding
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.acos
import kotlin.math.sqrt
import android.speech.tts.TextToSpeech
import java.util.*

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var stepp: SteppingUp= SteppingUp()

    private lateinit var tts: TextToSpeech;
    private lateinit var lastFitness: Training.Fitness;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        this.tts = TextToSpeech(this.activity, TextToSpeech.OnInitListener { tts.setLanguage(Locale.US) })
        this.lastFitness = Training.Fitness()

        this.binding.CurrentTraining.text = "Exercise Number: 1"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            this.activity?.let {
                ActivityCompat.requestPermissions(
                    it, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()

        this.tts.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this.context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = this.context?.let { ProcessCameraProvider.getInstance(it) }

        cameraProviderFuture!!.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(this.binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, AngleAnalyzer{
                            pose:Angles ->
                            //this.binding.LeftFeetText.text = "Left leg angle: " + pose.leftLegDeg.toString()
                            //this.binding.RightFeetText.text = "Right leg angle: " + pose.rightLegDeg.toString()
                            //this.binding.LeftArmText.text = "Left arm angle: " + pose.leftArmDeg.toString()
                            //this.binding.RightArmText.text = "Right arm angle: " + pose.rightArmDeg.toString()
                            //this.binding.LeftShoulderText.text = "Left shoulder  angle: " + pose.leftShoulderDeg.toString()
                            //this.binding.RightShoulderText.text = "Right shoulder  angle: " + pose.rightShoulderDeg.toString()
                            //this.binding.LeftHipText.text = "Left hip angle: " + pose.leftHipDeg.toString()
                            //this.binding.RightHipText.text = "Right hip angle: " + pose.rightHipDeg.toString()

                            var str: String = ""
                            var fitness: Training.Fitness = this.stepp.CheckFitness(pose)

                            if (!fitness.Compare(this.lastFitness)) {
                                for (i in fitness.messages)
                                    str += i + "\n";

                                if (str != "")
                                    str = str.substring(0,str.length - 1);

                                this.tts.speak(str, TextToSpeech.QUEUE_FLUSH, null)

                                this.binding.FitnessText.text = str
                            }

                            this.lastFitness = fitness

                            this.binding.CurrentRepeat.text = "Current repeat: " + this.stepp.currentRepaet
                        })
                    }

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)


            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, this.context?.let { ContextCompat.getMainExecutor(it) })

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
            this.context?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1, it)
            } == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }



    private class AngleAnalyzer(private val callback: AngleListener) : ImageAnalysis.Analyzer {
        private val _poseDetector: PoseDetector = PoseDetection.getClient(
            AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE).build()
        );


        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {
            val mediaImage = image.image

            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

                this._poseDetector.process(image).addOnSuccessListener { results ->
                    println(results.allPoseLandmarks.size)
                    println()
                    callback(
                        Angles(
                        this.GetAngleDeg(results, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
                        this.GetAngleDeg(results, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
                        this.GetAngleDeg(results, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
                        this.GetAngleDeg(results, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
                        this.GetAngleDeg(results, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
                        this.GetAngleDeg(results, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
                        this.GetAngleDeg(results, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
                        this.GetAngleDeg(results, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),)
                    )
                }
            }
            image.close()
        }

        fun GetAngleDeg(
            results: Pose?,
            top: Int,
            middle: Int,
            bottom: Int
        ): Float {
            val _top = results?.getPoseLandmark(top)
            if (_top == null || _top.inFrameLikelihood < 0.8f)
                return 0f;
            val topVec = Vector2(_top.position.x, _top.position.y)

            val _middle = results.getPoseLandmark(middle)
            if (_middle == null || _middle.inFrameLikelihood < 0.8f)
                return 0f
            val middleVec = Vector2(_middle.position.x, _middle.position.y)

            val _bottom = results.getPoseLandmark(bottom)
            if (_bottom == null || _bottom.inFrameLikelihood < 0.8f)
                return 0f
            val bottomVec = Vector2(_bottom.position.x, _bottom.position.y)

            val vec1 = topVec.sub(middleVec).normalize()
            val vec2 = bottomVec.sub(middleVec).normalize()

            val angle = acos(vec1.dot(vec2).toDouble()).toFloat() * 57.2957795f
            return angle
        }
    }
}

class Vector3(public val x: Float, public val y: Float, public val z: Float) {
    fun sub(vec: Vector3): Vector3{
        return Vector3(this.x - vec.x, this.y- vec.y, this.z- vec.z)
    }

    fun dot(vec: Vector3): Float{
        return this.x * vec.x + this.y * vec.y + this.z + vec.z
    }

    fun magnitude(): Float{
        return sqrt( this.x* this.x + this.y* this.y +this.z * this.z)
    }

    fun normalize(): Vector3 {
        return Vector3(this.x/this.magnitude(), this.y/this.magnitude(), this.z/this.magnitude())
    }
}

class Vector2(public val x: Float, public val y: Float) {
    fun sub(vec: Vector2): Vector2{
        return Vector2(this.x - vec.x, this.y- vec.y)
    }

    fun dot(vec: Vector2): Float{
        return this.x * vec.x + this.y * vec.y
    }

    fun magnitude(): Float{
        return sqrt( this.x* this.x + this.y* this.y)
    }

    fun normalize(): Vector2 {
        return Vector2(this.x/this.magnitude(), this.y/this.magnitude())
    }
}

typealias AngleListener = (angle: Angles)->Unit
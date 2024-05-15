package com.azhar.absensi.view.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityCameraPrevBinding
import com.azhar.absensi.view.absen.AbsenActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.Manifest

class CameraPrev : AppCompatActivity() {

    private lateinit var binding: ActivityCameraPrevBinding
    private var imageCapture: ImageCapture? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var fileDirectory: File
    private lateinit var cameraExecutorService: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraPrevBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()
        if(allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS , REQUEST_CODE_PERMISSION)
        }

        findViewById<FloatingActionButton>(R.id.btn_capture).setOnClickListener {
            takePicture()
        }

        binding.switchCamera.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }

        fileDirectory = getOutputDirectory()
        cameraExecutorService = Executors.newSingleThreadExecutor()
    }

    companion object {
        private const val TAG = "CameraXAHE"
        private const val FILENAME_FORMAT = "dd-MM-yyyy-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSION = 20
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    private fun startCamera(){
        val cameraProvider = ProcessCameraProvider.getInstance(this)
        cameraProvider.addListener(Runnable {
            val camProvider: ProcessCameraProvider = cameraProvider.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.layPreview.createSurfaceProvider())
                }
            imageCapture = ImageCapture.Builder().build()

            try{
                camProvider.unbindAll()
                camProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }catch (exc: Exception){
                Log.e(TAG, "failed to start cameara", exc)
            }
        },
            ContextCompat.getMainExecutor(this)
        )

    }

     private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all{
         ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
     }

     private fun getOutputDirectory():File {
         val mediaDir = externalMediaDirs.firstOrNull() ?.let {
             File(it, resources.getString(R.string.app_name))
         }
         return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
     }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode  == REQUEST_CODE_PERMISSION){
            if(allPermissionGranted()){
                startCamera()
            }else{
                Toast.makeText(this, "Permission not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

    fun takePicture() {
        val imgCap = imageCapture ?: return
        val externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val fileFoto = File(
            externalStorage,
            SimpleDateFormat(FILENAME_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(fileFoto).build()
        imgCap.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uriFile = Uri.fromFile(fileFoto)
                    val msg = "Foto tersimpan : ${uriFile}"
                    val intent = Intent()
                    //kembali ke absen page
                    intent.putExtra("uri_file", uriFile.toString())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)

                    // Scan file to make it visible in gallery apps
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uriFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "File failed : ${exception.message}")
                }

            }
        )

    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}
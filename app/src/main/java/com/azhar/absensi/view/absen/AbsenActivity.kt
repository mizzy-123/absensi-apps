package com.azhar.absensi.view.absen

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import com.azhar.absensi.R
import com.azhar.absensi.utils.BitmapManager.bitmapToBase64
import com.azhar.absensi.view.camera.CameraPrev
import com.azhar.absensi.viewmodel.AbsenViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import kotlinx.android.synthetic.main.activity_absen.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AbsenActivity : AppCompatActivity() {
    var REQ_CAMERA = 101
    var strCurrentLatitude = 0.0
    var strCurrentLongitude = 0.0
    var strFilePath: String = ""
    var strLatitude = "0"
    var strLongitude = "0"
    lateinit var fileDirectoty: File
    lateinit var imageFilename: File
    lateinit var exifInterface: ExifInterface
    lateinit var strBase64Photo: String
    lateinit var strCurrentLocation: String
    lateinit var strTitle: String
    lateinit var strTimeStamp: String
    lateinit var strImageName: String
    lateinit var absenViewModel: AbsenViewModel
    lateinit var progressDialog: ProgressDialog
    var uriFile : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absen)
        //get uri foto from camera page
        setInitLayout()
        setCurrentLocation()
        setUploadData()


    }

    private fun setCurrentLocation() {
        progressDialog.show()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                progressDialog.dismiss()
                if (location != null) {
                    strCurrentLatitude = location.latitude
                    strCurrentLongitude = location.longitude
                    val geocoder = Geocoder(this@AbsenActivity, Locale.getDefault())
                    try {
                        val addressList =
                            geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
                        if (addressList != null && addressList.size > 0) {
                            strCurrentLocation = addressList[0].getAddressLine(0)
                            inputLokasi.setText(strCurrentLocation)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this@AbsenActivity,
                        "Ups, harap mengaktifkan geolocation ponsel Anda",
                        Toast.LENGTH_SHORT).show()
                    enableLoc()

                    strLatitude = "0"
                    strLongitude = "0"
                }
            }
    }

    /*
    * function untuk menampilkan pop up alert to enable location
    * */
    private fun enableLoc() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(30 * 1000)
        locationRequest.setFastestInterval(5 * 1000)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                        try {
                            val resolvable = exception as ResolvableApiException

                            resolvable.startResolutionForResult(
                                this@AbsenActivity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Toast.makeText(this, "Something occured to be error!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                    }
                }
            }
        }}


    private fun setInitLayout() {
        progressDialog = ProgressDialog(this)
        strTitle = intent.extras?.getString(DATA_TITLE).toString()

        if (strTitle != null) {
            tvTitle.text = strTitle
        }

        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        absenViewModel = ViewModelProvider(
            this, (ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.application) as ViewModelProvider.Factory)
        ).get(AbsenViewModel::class.java)

        inputTanggal.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date =
                OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalAbsen[Calendar.YEAR] = year
                    tanggalAbsen[Calendar.MONTH] = monthOfYear
                    tanggalAbsen[Calendar.DAY_OF_MONTH] = dayOfMonth
                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    inputTanggal.setText(simpleDateFormat.format(tanggalAbsen.time))
                }
            DatePickerDialog(
                this@AbsenActivity, date,
                tanggalAbsen[Calendar.YEAR],
                tanggalAbsen[Calendar.MONTH],
                tanggalAbsen[Calendar.DAY_OF_MONTH]
            ).show()
        }

        layoutImage.setOnClickListener {
            val move = Intent(this.application, CameraPrev::class.java)
            startActivityForResult(move, REQUEST_CODE)
        }

    }
/*
* bisa dihapus --nt:manifest_tech
* */
//        layoutImage.setOnClickListener {
//            Dexter.withContext(this@AbsenActivity)
//                .withPermissions(
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//                .withListener(object : MultiplePermissionsListener {
//                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
//                        if (report.areAllPermissionsGranted()) {
//                            try {
//                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                                cameraIntent.putExtra(
//                                    "com.google.assistant.extra.USE_FRONT_CAMERA",
//                                    true
//                                )
//                                cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
//                                cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
//                                cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)
//
//                                // Samsung
//                                cameraIntent.putExtra("camerafacing", "front")
//                                cameraIntent.putExtra("previous_mode", "front")
//
//                                // Huawei
//                                cameraIntent.putExtra("default_camera", "1")
//                                cameraIntent.putExtra(
//                                    "default_mode",
//                                    "com.huawei.camera2.mode.photo.PhotoMode")
//                                cameraIntent.putExtra(
//                                    MediaStore.EXTRA_OUTPUT,
//                                    FileProvider.getUriForFile(
//                                        this@AbsenActivity,
//                                        BuildConfig.APPLICATION_ID + ".provider",
//                                        createImageFile()
//                                    )
//                                )
//                                startActivityForResult(cameraIntent, REQ_CAMERA)
//                            } catch (ex: IOException) {
//                                Toast.makeText(this@AbsenActivity,
//                                    "Ups, gagal membuka kamera", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//
//                    override fun onPermissionRationaleShouldBeShown(
//                        permissions: List<PermissionRequest>,
//                        token: PermissionToken) {
//                        token.continuePermissionRequest()
//                    }
//                }).check()
//        }




    private fun setUploadData() {
        btnAbsen.setOnClickListener {
            val strNama = inputNama.text.toString()
            val strTanggal = inputTanggal.text.toString()
            val strKeterangan = inputKeterangan.text.toString()
            val strjumlahspp = inputjumlahspp.text.toString()
//            convertImage(uriFile)
            if (uriFile.equals(null) || strNama.isEmpty() || strCurrentLocation.isEmpty()
                || strTanggal.isEmpty() || strKeterangan.isEmpty()) {
                Toast.makeText(this@AbsenActivity,
                    "Data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                absenViewModel.addDataAbsen(
                    uriFile!!,
                    strNama,
                    strTanggal,
                    strCurrentLocation,
                    strKeterangan,
                    strjumlahspp)
                Toast.makeText(this@AbsenActivity,
                    "Laporan Anda terkirim, tunggu info selanjutnya ya!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /*
    * bagian ini boleh dihapus --nt:manifest_tech
    * */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        strTimeStamp = SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(Date())
        strImageName = "IMG_"
        fileDirectoty = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "")
        imageFilename = File.createTempFile(strImageName, ".jpg", fileDirectoty)
        strFilePath = imageFilename.getAbsolutePath()
        return imageFilename
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //get data uri file foto dari camera page
        if(requestCode == REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                uriFile = data?.getStringExtra("uri_file")
                imageSelfie.setImageURI(Uri.parse(uriFile))
            }
        }
        //result jika lokasi berhasil di enable
        else if(requestCode == REQUEST_CHECK_SETTINGS){
            when(resultCode) {
                Activity.RESULT_OK -> {
                    Toast.makeText(this, "Successfully get location", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "failed get location, please enable location", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }
        }
    }

    /*
    * bagian ini boleh di hapus --nt:manifest_tech
    * */
    private fun convertImage(imageFilePath: String?) {
        val imageFile = File(imageFilePath)
        if (imageFile.exists()) {
            val options = BitmapFactory.Options()
            var bitmapImage = BitmapFactory.decodeFile(strFilePath, options)

            try {
                exifInterface = ExifInterface(imageFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }

            bitmapImage = Bitmap.createBitmap(
                bitmapImage,
                0,
                0,
                bitmapImage.width,
                bitmapImage.height,
                matrix,
                true
            )

            if (bitmapImage == null) {
                Toast.makeText(this@AbsenActivity,
                    "Ups, foto kamu belum ada!", Toast.LENGTH_LONG).show()
            } else {
                val resizeImage = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 512, resizeImage, true)
                Glide.with(this)
                    .load(scaledBitmap)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_photo_camera)
                    .into(imageSelfie)
                strBase64Photo = bitmapToBase64(scaledBitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val DATA_TITLE = "TITLE"
        const val REQUEST_CODE = 123
        const val REQUEST_CHECK_SETTINGS = 888
    }
}
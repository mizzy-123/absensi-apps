package com.azhar.absensi.view.spp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityInputSppBinding
import com.azhar.absensi.utils.uriToByteArray
import com.azhar.absensi.view.camera.CameraPrev
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputSppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputSppBinding
    var jatuhTempo: Long = 0
    var tanggalBayar: Long = 0
    private var fotoByte: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputSppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction(){

        binding.edJatuhTempo.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalAbsen[Calendar.YEAR] = year
                    tanggalAbsen[Calendar.MONTH] = monthOfYear
                    tanggalAbsen[Calendar.DAY_OF_MONTH] = dayOfMonth
                    jatuhTempo = tanggalAbsen.timeInMillis
                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    binding.edJatuhTempo.setText(simpleDateFormat.format(tanggalAbsen.time))
                }
            DatePickerDialog(
                this, date,
                tanggalAbsen[Calendar.YEAR],
                tanggalAbsen[Calendar.MONTH],
                tanggalAbsen[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.edTanggalBayar.setOnClickListener {
            val tanggalAbsen = Calendar.getInstance()
            val date =
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    tanggalAbsen[Calendar.YEAR] = year
                    tanggalAbsen[Calendar.MONTH] = monthOfYear
                    tanggalAbsen[Calendar.DAY_OF_MONTH] = dayOfMonth
                    tanggalBayar = tanggalAbsen.timeInMillis
                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    binding.edTanggalBayar.setText(simpleDateFormat.format(tanggalAbsen.time))
                }
            DatePickerDialog(
                this, date,
                tanggalAbsen[Calendar.YEAR],
                tanggalAbsen[Calendar.MONTH],
                tanggalAbsen[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.layoutImage.setOnClickListener {
            val intent = Intent(this, CameraPrev::class.java)
            launcherIntentCameraX.launch(intent)
        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null){
            val uriFile: String? = it.data?.getStringExtra("uri_file")
            val getUriFile: Uri = Uri.parse(uriFile)
            binding.imageSelfie.setImageURI(getUriFile)
            fotoByte = uriToByteArray(this, getUriFile)
        }
    }

    companion object {
        const val DATA_TITLE = "TITLE"
        const val REQUEST_CODE = 123
        const val REQUEST_CHECK_SETTINGS = 255
    }
}
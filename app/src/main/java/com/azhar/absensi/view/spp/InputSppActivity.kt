package com.azhar.absensi.view.spp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityInputSppBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.DataSpp
import com.azhar.absensi.model.GetDataSpp
import com.azhar.absensi.utils.uriToByteArray
import com.azhar.absensi.view.camera.CameraPrev
import com.azhar.absensi.view.penggajian.penggajianguru.ClearValue
import com.azhar.absensi.view.penggajian.penggajianguru.RupiahTextWatcher
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class InputSppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputSppBinding
    var jatuhTempo: Long = 0
    var tanggalBayar: Long = 0
    private var fotoByte: ByteArray? = null
    private lateinit var firestore: Firestore
    private lateinit var storage: FirebaseStorage
    private lateinit var pDialog: SweetAlertDialog
    private lateinit var pref: SharedPreferences
    var uid: String? = null
    var currentImage: Uri? = null
    var imageUrl = ""
    var updateCondition = false
    var uidDocument = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputSppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras
        if (bundle != null){
            val getDataSpp = if (Build.VERSION.SDK_INT >= 33){
                bundle.getParcelable(RiwayatSppActivity.SEND_DATA, GetDataSpp::class.java)
            } else {
                bundle.getParcelable<GetDataSpp>(RiwayatSppActivity.SEND_DATA)
            }

            if (getDataSpp != null){
                updateCondition = true
                uidDocument = getDataSpp.id
                binding.apply {
                    edNamaMurid.setText(getDataSpp.nama_murid)
                    edNamaGuru.setText(getDataSpp.nama_guru)
                    edLes.setText(getDataSpp.jenis_les)

                    imageUrl = getDataSpp.foto

                    jatuhTempo = getDataSpp.jatuh_tempo
                    tanggalBayar = getDataSpp.tgl_bayar

                    val strFormatDefault = "dd MMMM yyyy HH:mm"
                    val simpleDateFormat = SimpleDateFormat(strFormatDefault, Locale.getDefault())
                    val dateJatuhTempo = Date(getDataSpp.jatuh_tempo)
                    val dateTanggalBayar = Date(getDataSpp.tgl_bayar)

                    edTanggalBayar.setText(simpleDateFormat.format(dateTanggalBayar))
                    edJatuhTempo.setText(simpleDateFormat.format(dateJatuhTempo))

                    val cleanString = getDataSpp.nominal.toString().replace("[Rp ,.]".toRegex(), "")
                    val df = DecimalFormat("#,###")
                    val resultNominal = if (cleanString.isNotEmpty()){
                        val parsed = cleanString.toDouble()
                        val formatted = "Rp " + df.format(parsed).replace(",", ".")
                        formatted
                    }else {
                        ""
                    }
                    inputjumlahspp.setText(resultNominal)

                    Glide.with(this@InputSppActivity)
                        .load(getDataSpp.foto)
                        .into(imageSelfie)
                }
            }
        }

        initComponents()
        setupAction()
    }

    private fun initComponents(){
        firestore = Firestore.instance
        storage = Firebase.storage
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        uid = pref.getString("uid", "")
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Process..."
        pDialog.progressHelper.barColor = ContextCompat.getColor(this, android.R.color.holo_red_light)
        pDialog.setCancelable(false)
    }

    private fun deleteImageFromStorage(){
        val storage = Firebase.storage
        val storageRef = storage.getReferenceFromUrl(imageUrl)

        storageRef.delete()
            .addOnSuccessListener {
                Log.d("InputSppActivity", "Image successfully deleted")
                uploadImageToFirebasStorage(currentImage!!)
            }
            .addOnFailureListener { exception ->
                Log.d("InputSppActivity", "Error deleting image: ${exception.message}")
            }
    }

    private fun uploadImageToFirebasStorage(imageUri: Uri){
        pDialog.show()
        val imageName = "${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference

        val imageRef = storageRef.child("images/$imageName")

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUrlToFirestore(uri.toString())
            }.addOnFailureListener {
                pDialog.dismiss()
                Toast.makeText(this, "Download image gagal", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            pDialog.dismiss()
            Toast.makeText(this, "Upload gagal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String){
        val nama_murid = binding.edNamaMurid.text.toString()
        val nama_guru = binding.edNamaGuru.text.toString()
        val jenis_les = binding.edLes.text.toString()
        val nominal = ClearValue.hapus(binding.inputjumlahspp.text.toString()).toInt()
        if (updateCondition){
            firestore
                .getDocument(uid!!)
                .collection("spp")
                .document(uidDocument)
                .set(DataSpp(
                    nama_murid = nama_murid,
                    nama_guru = nama_guru,
                    foto = imageUrl,
                    jatuh_tempo = jatuhTempo,
                    tgl_bayar = tanggalBayar,
                    jenis_les = jenis_les,
                    nominal = nominal,
                    timestamp = System.currentTimeMillis()
                )).addOnSuccessListener {
                    pDialog.dismiss()
                    Toast.makeText(this, "Berhasil di simpan", Toast.LENGTH_LONG).show()
                    inputReset()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal di simpan", Toast.LENGTH_SHORT).show()
                    pDialog.dismiss()
                }
        } else {
            firestore
                .getDocument(uid!!)
                .collection("spp")
                .add(DataSpp(
                    nama_murid = nama_murid,
                    nama_guru = nama_guru,
                    foto = imageUrl,
                    jatuh_tempo = jatuhTempo,
                    tgl_bayar = tanggalBayar,
                    jenis_les = jenis_les,
                    nominal = nominal,
                    timestamp = System.currentTimeMillis()
                )).addOnSuccessListener {
                    pDialog.dismiss()
                    Toast.makeText(this, "Berhasil di simpan", Toast.LENGTH_LONG).show()
                    inputReset()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal di simpan", Toast.LENGTH_SHORT).show()
                    pDialog.dismiss()
                }
        }
    }

    private fun saveDataSppWithoutImage(){
        val nama_murid = binding.edNamaMurid.text.toString()
        val nama_guru = binding.edNamaGuru.text.toString()
        val jenis_les = binding.edLes.text.toString()
        val nominal = ClearValue.hapus(binding.inputjumlahspp.text.toString()).toInt()
        if (updateCondition){
            firestore
                .getDocument(uid!!)
                .collection("spp")
                .document(uidDocument)
                .set(DataSpp(
                    nama_murid = nama_murid,
                    nama_guru = nama_guru,
                    foto = imageUrl,
                    jatuh_tempo = jatuhTempo,
                    tgl_bayar = tanggalBayar,
                    jenis_les = jenis_les,
                    nominal = nominal,
                    timestamp = System.currentTimeMillis()
                )).addOnSuccessListener {
                    pDialog.dismiss()
                    Toast.makeText(this, "Berhasil di simpan", Toast.LENGTH_LONG).show()
                    inputReset()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal di simpan", Toast.LENGTH_SHORT).show()
                    pDialog.dismiss()
                }
        } else {
            firestore
                .getDocument(uid!!)
                .collection("spp")
                .add(DataSpp(
                    nama_murid = nama_murid,
                    nama_guru = nama_guru,
                    foto = imageUrl,
                    jatuh_tempo = jatuhTempo,
                    tgl_bayar = tanggalBayar,
                    jenis_les = jenis_les,
                    nominal = nominal,
                    timestamp = System.currentTimeMillis()
                )).addOnSuccessListener {
                    pDialog.dismiss()
                    Toast.makeText(this, "Berhasil di simpan", Toast.LENGTH_LONG).show()
                    inputReset()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal di simpan", Toast.LENGTH_SHORT).show()
                    pDialog.dismiss()
                }
        }
    }

    private fun inputReset() {
        binding.edNamaMurid.setText("")
        binding.edNamaGuru.setText("")
        binding.edLes.setText("")
        binding.inputjumlahspp.setText("")
        binding.edJatuhTempo.setText("")
        binding.edTanggalBayar.setText("")
        binding.imageSelfie.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_photo_camera))
        currentImage = null
    }

    private fun setupAction(){

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

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

        binding.btnSimpan.setOnClickListener {
            if (currentImage != null){
                if (updateCondition){
                    deleteImageFromStorage()
                } else {
                    uploadImageToFirebasStorage(currentImage!!)
                }
            } else {
                saveDataSppWithoutImage()
            }
        }

        binding.inputjumlahspp.addTextChangedListener(RupiahTextWatcher(binding.inputjumlahspp))

        binding.btnGallery.setOnClickListener {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){ uri: Uri? ->
        if (uri != null){
            currentImage = uri
            binding.imageSelfie.setImageURI(uri)
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
            currentImage = getUriFile
        }
    }

    companion object {
        const val DATA_TITLE = "TITLE"
        const val REQUEST_CODE = 123
        const val REQUEST_CHECK_SETTINGS = 255
    }
}
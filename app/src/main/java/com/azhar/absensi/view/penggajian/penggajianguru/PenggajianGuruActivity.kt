package com.azhar.absensi.view.penggajian.penggajianguru
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityPenggajianGuruBinding
import com.azhar.absensi.firebase.Firestore
import java.text.NumberFormat
import java.util.Locale


class PenggajianGuruActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPenggajianGuruBinding
    private var jumlahGaji1: Int = 0
    private var jumlahGaji2: Int = 0
    private lateinit var viewModel: PenggajianGuruViewModel
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var pDialog: SweetAlertDialog
    private fun initComponents(){
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        editor = pref.edit()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenggajianGuruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        val firestore = Firestore.instance
        val viewModelFactory = PenggajianGuruViewModelFactory(firestore)
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Process..."
        pDialog.progressHelper.barColor = ContextCompat.getColor(this, android.R.color.holo_red_light)
        pDialog.setCancelable(false)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PenggajianGuruViewModel::class.java)

        val datePicker = DatePickerHelper(this,binding.etTgl)

        val anim = AnimationUtils.loadAnimation(this, R.anim.btn_click)
        binding.etTgl.setOnClickListener {
            datePicker.showDatePickerDialog()
        }


        binding.btnMinus.setOnClickListener {
            it.startAnimation(anim)
            if (jumlahGaji1 > 0) {
                jumlahGaji1--
                binding.tvJumlah.text=jumlahGaji1.toString()
                updateNilai(jumlahGaji1,binding.etGaji1,140000)
            }
        }


        binding.btnPlus.setOnClickListener {
            it.startAnimation(anim)
            jumlahGaji1++
            binding.tvJumlah.text=jumlahGaji1.toString()
            updateNilai(jumlahGaji1,binding.etGaji1,140000)
        }

        binding.btnMinus2.setOnClickListener {
            it.startAnimation(anim)
            if (jumlahGaji2 > 0) {
                jumlahGaji2--
                binding.tvJumlah2.text=jumlahGaji2.toString()
                updateNilai(jumlahGaji2,binding.etGaji2,160000)
            }
        }

        binding.btnPlus2.setOnClickListener {
            it.startAnimation(anim)
            jumlahGaji2++
            binding.tvJumlah2.text=jumlahGaji2.toString()
            updateNilai(jumlahGaji2,binding.etGaji2,160000)
        }

        binding.btnSimpan.setOnClickListener {
           validateFields()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.etBonus.addTextChangedListener(RupiahTextWatcher(binding.etBonus))
        binding.etGaji1.addTextChangedListener(RupiahTextWatcher(binding.etGaji1))
        binding.etGaji2.addTextChangedListener(RupiahTextWatcher(binding.etGaji2))

        viewModel.uploadState.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_LONG).show()
                pDialog.dismiss()
                clearEditText()
            }.onFailure { exception ->
                Toast.makeText(this, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_LONG).show()
                pDialog.dismiss()
            }
        }

    }

    private fun updateNilai(jumlah: Int, etGaji: EditText,nilai:Int) {
        val nilaiRupiah = jumlah * nilai
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatRupiah.maximumFractionDigits = 0

        val nilaiFormatted = if (nilaiRupiah >= nilai) {
            formatRupiah.format(nilaiRupiah)
        } else {
            formatRupiah.format(nilai)
        }

        etGaji.setText(nilaiFormatted)
    }

    private fun simpanPenggajian() {
        pDialog.show()
        val uid = pref.getString("uid", null)
        val namaGuru = binding.inputNama.text.toString()
        val gaji1 = ClearValue.hapus(binding.etGaji1.text.toString())
        val gaji2 = ClearValue.hapus(binding.etGaji2.text.toString())
        val tgl = binding.etTgl.text.toString()
        val bonus = ClearValue.hapus(binding.etBonus.text.toString())

        val penggajianData = mapOf(
            "nama_guru" to namaGuru,
            "gaji1" to gaji1.toInt(),
            "gaji2" to gaji2.toInt(),
            "tgl_gaji" to tgl,
            "bonus" to bonus.toInt()
        )

        if (uid != null) {
            viewModel.uploadPenggajian(uid, penggajianData)
        }
        //Toast.makeText(this, "Data Yang Disimpan :$uid $namaGuru, $gaji1, $gaji2, $tgl, $bonus", Toast.LENGTH_LONG).show()
    }

    fun clearEditText(){
        binding.inputNama.setText("")
        binding.etGaji2.setText("")
        binding.etGaji1.setText("")
        binding.etBonus.setText("")
        binding.etTgl.setText("")
    }

    private fun validateFields() {
        if (TextUtils.isEmpty(binding.inputNama.getText())) {
            binding.inputNama.error = "Nama tidak boleh kosong"
        }
        if (TextUtils.isEmpty(binding.etGaji1.getText())) {
            binding.etGaji1.error = "Gaji 1 tidak boleh kosong"
        }
        if (TextUtils.isEmpty(binding.etGaji2.getText())) {
            binding.etGaji2.error = "Gaji 2 tidak boleh kosong"
        }
        if (TextUtils.isEmpty(binding.etBonus.getText())) {
            binding.etBonus.error = "Bonus tidak boleh kosong"
        }
        if (TextUtils.isEmpty(binding.etTgl.getText())) {
            binding.etTgl.error = "Tanggal tidak boleh kosong"
        }
        if (!isAllFieldsValid()) {
            Toast.makeText(this, "Silahkan isi semua field yang diperlukan", Toast.LENGTH_SHORT)
                .show()
        } else {
           simpanPenggajian()
        }
    }

    private fun isAllFieldsValid(): Boolean {
        return !TextUtils.isEmpty(binding.inputNama.getText()) &&
                !TextUtils.isEmpty(binding.etGaji1.getText()) &&
                !TextUtils.isEmpty(binding.etGaji2.getText()) &&
                !TextUtils.isEmpty(binding.etBonus.getText()) &&
                !TextUtils.isEmpty(binding.etTgl.getText())
    }


}
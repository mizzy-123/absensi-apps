package com.azhar.absensi.view.penggajian.penggajianguru
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityPenggajianGuruBinding
import java.text.NumberFormat
import java.util.Locale

class PenggajianGuruActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPenggajianGuruBinding
    private var jumlahGaji1: Int = 0
    private var jumlahGaji2: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenggajianGuruBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                updateNilai(jumlahGaji1,binding.etGaji1)
            }
        }


        binding.btnPlus.setOnClickListener {
            it.startAnimation(anim)
            jumlahGaji1++
            binding.tvJumlah.text=jumlahGaji1.toString()
            updateNilai(jumlahGaji1,binding.etGaji1)
        }

        binding.btnMinus2.setOnClickListener {
            it.startAnimation(anim)
            if (jumlahGaji2 > 0) {
                jumlahGaji2--
                binding.tvJumlah2.text=jumlahGaji2.toString()
                updateNilai(jumlahGaji2,binding.etGaji2)
            }
        }

        binding.btnPlus2.setOnClickListener {
            it.startAnimation(anim)
            jumlahGaji2++
            binding.tvJumlah2.text=jumlahGaji2.toString()
            updateNilai(jumlahGaji2,binding.etGaji2)
        }

        binding.btnSimpan.setOnClickListener {
            simpanPenggajian()
        }

        binding.etBonus.addTextChangedListener(RupiahTextWatcher(binding.etBonus))
        binding.etGaji1.addTextChangedListener(RupiahTextWatcher(binding.etGaji1))
        binding.etGaji2.addTextChangedListener(RupiahTextWatcher(binding.etGaji2))
    }

    private fun updateNilai(jumlah: Int, etGaji: EditText) {
        val nilaiRupiah = jumlah * 140000
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatRupiah.maximumFractionDigits = 0

        val nilaiFormatted = if (nilaiRupiah >= 140000) {
            formatRupiah.format(nilaiRupiah)
        } else {
            formatRupiah.format(140000)
        }

        etGaji.setText(nilaiFormatted)
    }

    private fun simpanPenggajian() {
        val namaGuru = binding.inputNama.text.toString()
        val gaji1 = ClearValue.hapus(binding.etGaji1.text.toString())
        val gaji2 = ClearValue.hapus(binding.etGaji2.text.toString())
        val tgl = binding.etTgl.text.toString()
        val bonus = ClearValue.hapus(binding.etBonus.text.toString())

        Toast.makeText(this, "Data Yang Disimpan : $namaGuru, $gaji1, $gaji2, $tgl, $bonus", Toast.LENGTH_LONG).show()
    }


}
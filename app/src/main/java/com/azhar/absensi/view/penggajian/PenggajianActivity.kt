package com.azhar.absensi.view.penggajian

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityPenggajianBinding
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruActivity
import com.azhar.absensi.view.penggajian.riwayatpenggajian.RiwayatActivity

class PenggajianActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPenggajianBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenggajianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val anim = AnimationUtils.loadAnimation(this, R.anim.btn_click)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.cvPenggajianguru.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this@PenggajianActivity,PenggajianGuruActivity::class.java))
        }

        binding.cvRiwayat.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this@PenggajianActivity,RiwayatActivity::class.java))
        }
    }
}
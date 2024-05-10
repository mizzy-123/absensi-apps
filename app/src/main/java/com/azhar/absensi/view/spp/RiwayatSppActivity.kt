package com.azhar.absensi.view.spp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityRiwayatSppBinding

class RiwayatSppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatSppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatSppBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
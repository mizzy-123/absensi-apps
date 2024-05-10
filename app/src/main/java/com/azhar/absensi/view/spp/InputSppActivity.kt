package com.azhar.absensi.view.spp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityInputSppBinding

class InputSppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputSppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputSppBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
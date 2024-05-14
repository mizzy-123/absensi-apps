package com.azhar.absensi.view.spp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityMainSppBinding
import com.azhar.absensi.firebase.Firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MainSppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction(){
        binding.cvInputSpp.setOnClickListener {
            val intent = Intent(this@MainSppActivity, InputSppActivity::class.java)
            startActivity(intent)
        }

        binding.cvRiwayatSpp.setOnClickListener {
            val intent = Intent(this@MainSppActivity, RiwayatSppActivity::class.java)
            startActivity(intent)
        }
    }
}
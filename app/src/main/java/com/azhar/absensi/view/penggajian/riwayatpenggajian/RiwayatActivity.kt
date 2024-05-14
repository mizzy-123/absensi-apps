package com.azhar.absensi.view.penggajian.riwayatpenggajian

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.databinding.ActivityRiwayatBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModel
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModelFactory

class RiwayatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRiwayatBinding
    private lateinit var riwayatAdapter: RiwayatAdapter
    private lateinit var penggajianViewModel: PenggajianGuruViewModel
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private fun initComponents(){
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        editor = pref.edit()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        val firestore = Firestore.instance
        val viewModelFactory = PenggajianGuruViewModelFactory(firestore)
        penggajianViewModel = ViewModelProvider(this, viewModelFactory).get(PenggajianGuruViewModel::class.java)

        riwayatAdapter = RiwayatAdapter(emptyList())
        binding.recyclerRiwayat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = riwayatAdapter
        }

        penggajianViewModel.penggajianList.observe(this) { penggajianList ->
            riwayatAdapter.updateData(penggajianList)
        }

        penggajianViewModel.error.observe(this) { exception ->
            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
        }
        val uid = pref.getString("uid", null)
        penggajianViewModel.fetchPenggajian(uid.toString())



    }
}
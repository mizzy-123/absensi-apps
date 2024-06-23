package com.azhar.absensi.view.penggajian.riwayatpenggajian

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.databinding.ActivityRiwayatBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModel
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModelFactory

class RiwayatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatBinding
    private lateinit var riwayatAdapter: RiwayatAdapter
    private lateinit var penggajianViewModel: PenggajianGuruViewModel
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var uid: String = ""
    private fun initComponents() {
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
        penggajianViewModel =
            ViewModelProvider(this, viewModelFactory).get(PenggajianGuruViewModel::class.java)

        riwayatAdapter = RiwayatAdapter(emptyList())
        binding.recyclerRiwayat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = riwayatAdapter
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        penggajianViewModel.error.observe(this) { exception ->
            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
        penggajianViewModel.infoMessage.observe(this) { msg ->
            Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
        }

        penggajianViewModel.isLoading.observe(this) {
            showLoading(it)
        }
        uid = pref.getString("uid", null).toString()
        penggajianViewModel.fetchPenggajian(uid)
        observeData()
        observeDataSearch()
        binding.searchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    newText.let {
                        if (uid != null) {
                            penggajianViewModel.setSearchQuery(it, uid)
                        }
                    }
                } else {
                    observeData()
                    binding.searchview.clearFocus()
                }

                return true
            }

        })

        binding.refresh.setOnRefreshListener {
            penggajianViewModel.fetchPenggajian(uid)
            observeData()
            binding.refresh.isRefreshing = false
        }
    }

    private fun observeData() {
        penggajianViewModel.penggajianList.observe(this) { penggajianList ->
            riwayatAdapter.updateData(penggajianList)
        }
    }

    private fun observeDataSearch() {
        penggajianViewModel.searchResults.observe(this) { penggajianList ->
            penggajianList?.let {
                riwayatAdapter.updateData(it)
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
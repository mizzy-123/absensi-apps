package com.azhar.absensi.view.penggajian.riwayatpenggajian

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.azhar.absensi.databinding.ActivityRiwayatBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.GetDataSpp
import com.azhar.absensi.model.Penggajian
import com.azhar.absensi.view.penggajian.PenggajianActivity
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruActivity
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModel
import com.azhar.absensi.view.penggajian.penggajianguru.PenggajianGuruViewModelFactory
import com.azhar.absensi.view.spp.CardListSppAdapter
import com.azhar.absensi.view.spp.InputSppActivity
import com.azhar.absensi.view.spp.RiwayatSppActivity
import com.azhar.absensi.view.spp.RiwayatSppActivity.Companion
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

class RiwayatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRiwayatBinding
    private lateinit var riwayatAdapter: RiwayatAdapter
    private lateinit var penggajianViewModel: PenggajianGuruViewModel
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var listPenggajian: ArrayList<Penggajian>
    private lateinit var listDataPenggajianOriginal: ArrayList<Penggajian>
    private lateinit var firestore: Firestore
    private lateinit var pDialog: SweetAlertDialog
    private var uid: String = ""
    private fun initComponents() {
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        editor = pref.edit()
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Process..."
        pDialog.progressHelper.barColor =
            ContextCompat.getColor(this, android.R.color.holo_red_light)
        pDialog.setCancelable(false)
    }

    companion object {
        const val SEND_DATA = "PENGGAJIAN"
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
        uid = pref.getString("uid", null).toString()
        riwayatAdapter.setOnItemClickCallback(object : RiwayatAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Penggajian) {
                val dialogItem: Array<CharSequence> = arrayOf("Update data", "Delete data")
                val dialog = AlertDialog.Builder(this@RiwayatActivity)
                dialog.setCancelable(true)
                dialog.setItems(dialogItem, DialogInterface.OnClickListener { dialogInterface, i ->
                    when (i) {
                        0 -> {
                            val intent =
                                Intent(this@RiwayatActivity, PenggajianGuruActivity::class.java)
                            intent.putExtra(SEND_DATA, data)
                            startActivity(intent)
                        }

                        1 -> {
                            pDialog.show()
                            firestore.getDocument(uid!!)
                                .collection("gaji")
                                .document(data.id)
                                .delete()
                                .addOnSuccessListener {
                                    pDialog.dismiss()
                                    Toast.makeText(
                                        this@RiwayatActivity,
                                        "Delete succes",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    penggajianViewModel.fetchPenggajian(uid)
                                    observeData()
                                }
                                .addOnFailureListener {
                                    pDialog.dismiss()
                                    Toast.makeText(
                                        this@RiwayatActivity,
                                        "Delete failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        else -> {
                            pDialog.dismiss()
                            Toast.makeText(
                                this@RiwayatActivity,
                                "Delete failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                }).show()
            }
        })

        binding.toolbar.setNavigationOnClickListener()
        {
            finish()
        }

        penggajianViewModel.error.observe(this)
        { exception ->
            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
        penggajianViewModel.infoMessage.observe(this)
        { msg ->
            Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
        }

        penggajianViewModel.isLoading.observe(this)
        {
            showLoading(it)
        }

        penggajianViewModel.fetchPenggajian(uid)
        observeData()
        observeDataSearch()
        binding.searchview.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
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

        binding.refresh.setOnRefreshListener()
        {
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

    override fun onResume() {
        super.onResume()
        penggajianViewModel.fetchPenggajian(uid)
        observeData()
    }
}
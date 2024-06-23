package com.azhar.absensi.view.spp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityRiwayatSppBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.DataSpp
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RiwayatSppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatSppBinding
    private lateinit var cardListSppAdapter: CardListSppAdapter
    private lateinit var listSpp: ArrayList<DataSpp>
    private lateinit var listDataSppOriginal: ArrayList<DataSpp>
    private lateinit var firestore: Firestore
    private lateinit var pref: SharedPreferences
    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatSppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        setAction()

    }

    private fun initComponents(){
        listSpp = ArrayList()
        listDataSppOriginal = ArrayList()
        firestore = Firestore.instance
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        uid = pref.getString("uid", "")
    }

    private fun setAction(){
        firestore.getDocument(uid!!)
            .collection("spp")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {  result ->
                listSpp.clear()
                listDataSppOriginal.clear()
                for (document in result){
                    val getdataSpp = document.data

                    val dataSpp = DataSpp(
                        nama_murid = getdataSpp["nama_murid"].toString(),
                        nama_guru = getdataSpp["nama_guru"].toString(),
                        foto = getdataSpp["foto"].toString(),
                        jatuh_tempo = getdataSpp["jatuh_tempo"].toString().toLong(),
                        tgl_bayar = getdataSpp["tgl_bayar"].toString().toLong(),
                        nominal = getdataSpp["nominal"].toString().toInt(),
                        timestamp = getdataSpp["timestamp"].toString().toLong(),
                        jenis_les = getdataSpp["jenis_les"].toString()
                    )
                    listSpp.add(dataSpp)
                    listDataSppOriginal.add(dataSpp)
                    showRecyclerCardList()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Oops something wrong..", Toast.LENGTH_SHORT).show()
                binding.tvNotFound.visibility = View.VISIBLE
            }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.edSearch.clearFocus()
        binding.edSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    try {
                        filter(newText)
                    } catch (e: Exception){
                        Toast.makeText(this@RiwayatSppActivity, "Ops.. something wrong", Toast.LENGTH_SHORT).show()
                        Log.e("search", e.message.toString())
                    }
                }
                return true
            }
        })
    }

    private fun filter(newText: String?) {
        val listM: ArrayList<DataSpp> = ArrayList()
        for (item in listDataSppOriginal) {
            if (item.nama_guru.lowercase().contains(newText?.lowercase() ?: "") ||
                item.nama_murid.lowercase().contains(newText?.lowercase() ?: "")
            ) {
                listM.add(item)
            }
        }
        if (listM.isEmpty()) {
//            Toast.makeText(this@LihatActivity, "Not found", Toast.LENGTH_SHORT).show()
        } else {
            cardListSppAdapter.submitList(listM)
        }
    }

    private fun showRecyclerCardList(){
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        cardListSppAdapter = CardListSppAdapter()
        binding.rvHistory.adapter = cardListSppAdapter
        cardListSppAdapter.submitList(listSpp)

        if (listSpp.isEmpty()){
            binding.tvNotFound.visibility = View.VISIBLE
        } else {
            binding.tvNotFound.visibility = View.GONE
        }
    }
}
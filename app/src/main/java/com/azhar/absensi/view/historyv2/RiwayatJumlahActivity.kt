package com.azhar.absensi.view.historyv2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityRiwayatJumlahBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.DataSpp
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatJumlahActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatJumlahBinding
    private lateinit var listSpp: ArrayList<DataSpp>
    private lateinit var riwayatJumlahAdapter: RiwayatJumlahAdapter
    private lateinit var firestore: Firestore
    private lateinit var pref: SharedPreferences
    var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatJumlahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        setAction()
    }

    private fun initComponents(){
        listSpp = ArrayList()
        firestore = Firestore.instance
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        uid = pref.getString("uid", "")
    }

    private fun setAction(){
        firestore.getDocument(uid!!)
            .collection("spp")
            .orderBy("tgl_bayar", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listSpp.clear()
                val temp = arrayListOf<String>()
                val newListSpp = ArrayList<DataSpp>()
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

                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    val resultFormat = dateFormat.format(dataSpp.tgl_bayar)

                    var cek: Boolean = false

                    if (newListSpp.isEmpty()){
                        newListSpp.add(dataSpp)
                        temp.add(resultFormat)
                    } else {
                        for (y in temp){
                            if (resultFormat == y){
                                cek = true
                                newListSpp.forEachIndexed { index, value ->
                                    if (dateFormat.format(value.tgl_bayar) == resultFormat){
                                        newListSpp[index].nominal += dataSpp.nominal
                                    }
                                }
                                break
                            }
                        }

                        if (!cek){
                            newListSpp.add(dataSpp)
                            temp.add(resultFormat)
                        }
                    }
                }

                listSpp.addAll(newListSpp)
                showRecycleList()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Oops something wrong..", Toast.LENGTH_SHORT).show()
            }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun showRecycleList(){
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        riwayatJumlahAdapter = RiwayatJumlahAdapter(listSpp)
        binding.rvHistory.adapter = riwayatJumlahAdapter
    }
}
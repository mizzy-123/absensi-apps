package com.azhar.absensi.view.spp

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityRiwayatSppBinding
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.DataSpp
import com.azhar.absensi.model.GetDataSpp
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class RiwayatSppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatSppBinding
    private lateinit var cardListSppAdapter: CardListSppAdapter
    private lateinit var listSpp: ArrayList<GetDataSpp>
    private lateinit var listDataSppOriginal: ArrayList<GetDataSpp>
    private lateinit var firestore: Firestore
    private lateinit var pDialog: SweetAlertDialog
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
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Process..."
        pDialog.progressHelper.barColor = ContextCompat.getColor(this, android.R.color.holo_red_light)
        pDialog.setCancelable(false)
    }

    private fun setAction(){

        getDataSppFromFirebase()

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

    private fun getDataSppFromFirebase(){
        showLoading(true)
        firestore.getDocument(uid!!)
            .collection("spp")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null){
                    // Tangani kesalahan saat mendapatkan snapshot
                    Log.e("Firestore", "Error getting documents: $firebaseFirestoreException")
                    showLoading(false)
                    return@addSnapshotListener
                }

                if (querySnapshot != null){
                    showLoading(false)
                    listSpp.clear()
                    listDataSppOriginal.clear()
                    for (document in querySnapshot){
                        val getdataSpp = document.data
                        val getdataId = document.id

                        val dataSpp = GetDataSpp(
                            id = getdataId,
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
                    }

                    showRecyclerCardList()
                }
            }
    }

    fun deleteImageFromStorage(downloadUrl: String, onComplete: (Boolean) -> Unit) {
        val storage = Firebase.storage
        val storageRef = storage.getReferenceFromUrl(downloadUrl)

        storageRef.delete()
            .addOnSuccessListener {
                Log.d("RiwayatSppActivity", "Image successfully deleted")
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.d("RiwayatSppActivity", "Error deleting image: ${exception.message}")
                onComplete(false)
            }
    }

    private fun filter(newText: String?) {
        val listM: ArrayList<GetDataSpp> = ArrayList()
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

        cardListSppAdapter.setOnItemClickCallback(object : CardListSppAdapter.OnItemClickCallback {
            override fun onItemClicked(data: GetDataSpp) {
                val dialogItem: Array<CharSequence> = arrayOf("Update data", "Delete data")
                val dialog = AlertDialog.Builder(this@RiwayatSppActivity)
                dialog.setCancelable(true)
                dialog.setItems(dialogItem, DialogInterface.OnClickListener { dialogInterface, i ->
                    when(i){
                        0 -> {

                        }
                        1 -> {
                            pDialog.show()
                            deleteImageFromStorage(data.foto){
                                if (it){
                                    firestore.getDocument(uid!!)
                                        .collection("spp")
                                        .document(data.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            pDialog.dismiss()
                                            Toast.makeText(this@RiwayatSppActivity, "Delete succes", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            pDialog.dismiss()
                                            Toast.makeText(this@RiwayatSppActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    pDialog.dismiss()
                                    Toast.makeText(this@RiwayatSppActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
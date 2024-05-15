package com.azhar.absensi.view.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.databinding.ActivityMainBinding
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.absen.AbsenActivity
import com.azhar.absensi.view.history.HistoryActivity
import com.azhar.absensi.view.historyv2.RiwayatJumlahActivity
import com.azhar.absensi.view.login.LoginActivity
import com.azhar.absensi.view.spp.MainSppActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.azhar.absensi.view.penggajian.PenggajianActivity

class MainActivity : AppCompatActivity() {

    lateinit var strTitle: String
    lateinit var session: SessionLogin
    private lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        initComponents()
        setInitLayout()
    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser != null){
            setInitLayout()
        }else{
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
    }

    private fun initComponents(){
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
    }

    private fun setInitLayout() {


        binding.cvAbsenMasuk.setOnClickListener {
            strTitle = "Absen Masuk"
            val intent = Intent(this@MainActivity, PenggajianActivity::class.java)
            intent.putExtra(AbsenActivity.DATA_TITLE, strTitle)
            startActivity(intent)
        }

        binding.cvAbsenKeluar.setOnClickListener {
            val intent = Intent(this@MainActivity, MainSppActivity::class.java)
            startActivity(intent)
        }

        /* Jika tidak digunakan bisa dihapus -nt:manifestas_tech */
//        binding.cvPerizinan.setOnClickListener {
//            strTitle = "Izin"
//            val intent = Intent(this@MainActivity, AbsenActivity::class.java)
//            intent.putExtra(AbsenActivity.DATA_TITLE, strTitle)
//            startActivity(intent)
//        }

        binding.cvHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, RiwayatJumlahActivity::class.java)
            startActivity(intent)
        }

        binding.imageLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage("Yakin Anda ingin Logout?")
            builder.setCancelable(true)
            builder.setNegativeButton("Batal") { dialog, which -> dialog.cancel() }
            builder.setPositiveButton("Ya") { dialog, which ->
//                session.logoutUser()
                FirebaseAuth.getInstance().signOut()

                val editor = pref.edit()
                editor.putString("uid", null)
                editor.apply()
                finishAffinity()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

}
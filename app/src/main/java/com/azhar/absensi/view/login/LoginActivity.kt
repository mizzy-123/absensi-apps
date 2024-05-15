package com.azhar.absensi.view.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityLoginBinding
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.main.MainActivity
import com.azhar.absensi.view.register.RegisterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var session: SessionLogin
    lateinit var strNama: String
    lateinit var strPassword: String
    private lateinit var binding: ActivityLoginBinding
    var REQ_PERMISSION = 101
    lateinit var auth: FirebaseAuth
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        initComponents()
        cekLogin()

        val usernameTXT = binding.inputNama.text.toString()
        val passTXT = binding.inputPassword.text.toString()
        binding.prgBar.visibility = View.GONE
        binding.btnLogin.setOnClickListener {
            SignIn(findViewById<TextInputEditText>(R.id.inputNama).text.toString(), findViewById<TextInputEditText>(R.id.inputPassword).text.toString())
//        Toast.makeText(this, findViewById<TextInputEditText>(R.id.inputNama).text.toString(),Toast.LENGTH_SHORT).show()
            binding.prgBar.visibility = View.VISIBLE
        }

        binding.tvRegis.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))

        }
    }

    private fun initComponents(){
        pref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    private fun cekLogin(){
        val uid = pref.getString("uid", null)
        if (uid != null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_PERMISSION
            )
        }
    }

    private fun SignIn(username:String, password:String){

        if(username.isNullOrBlank() || password.isNullOrBlank()){
            Toast.makeText(this, "Maaf, field tidak boleh kosong! ${username} | ${password}", Toast.LENGTH_SHORT).show()
        }

        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this) {
                task ->
            if(task.isSuccessful){
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                binding.prgBar.visibility = View.GONE
                val currUser = auth.currentUser

                //save to preferences
                editor.putString("uid", currUser!!.uid )
                editor.apply()

                //set all permission
                setPermission()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            }else if (task.isCanceled){
                Toast.makeText(this, "Failed SignIn!", Toast.LENGTH_SHORT).show()
                binding.prgBar.visibility = View.GONE

            }else{
                Toast.makeText(this, "Failed SignIn!", Toast.LENGTH_SHORT).show()
                binding.prgBar.visibility = View.GONE

            }
        }



    }




    private fun setInitLayout() {
        session = SessionLogin(applicationContext)

        if (session.isLoggedIn()) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            strNama = binding.inputNama.text.toString()
            strPassword = binding.inputPassword.text.toString()

            if (strNama.isEmpty() || strPassword.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Form tidak boleh kosong!",
                    Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                session.createLoginSession(strNama)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

}
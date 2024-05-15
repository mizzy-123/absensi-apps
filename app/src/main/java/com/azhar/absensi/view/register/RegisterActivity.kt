package com.azhar.absensi.view.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.transition.Visibility
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityRegisterBinding
import com.azhar.absensi.view.login.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    lateinit var binding : ActivityRegisterBinding
    lateinit var auth: FirebaseAuth
    lateinit var usernameTXT: String
    lateinit var passTXT: String
    lateinit var confTXT: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        usernameTXT = binding.inputNama.text.toString()
        passTXT = binding.inputPassword.text.toString()
        confTXT = binding.inputConfPassword.text.toString()
        binding.prgBar.visibility = View.GONE

        binding.btnRegis.setOnClickListener {
            SignUp(findViewById<TextInputEditText>(R.id.inputNama).text.toString(),
                findViewById<TextInputEditText>(R.id.inputPassword).text.toString(),
                findViewById<TextInputEditText>(R.id.inputConfPassword).text.toString()
            ).also {
                binding.btnRegis.visibility = View.GONE
                binding.tvLogin.visibility = View.GONE
                binding.prgBar.visibility = View.VISIBLE

            }

        }
    }

    fun SignUp(username:String, password:String, confPass:String) {

        if (username.isNullOrBlank() || password.isNullOrBlank() || confPass.isNullOrBlank()) {
            Toast.makeText(this, "Maaf, field tidak boleh kosong!", Toast.LENGTH_SHORT).show()
        } else if (password != confPass) {
            Toast.makeText(this, "Kesalahan, Password tidak sama", Toast.LENGTH_SHORT).show()
        } else {
            try {
                auth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Berhasil SignUp", Toast.LENGTH_SHORT).show()
                            val user = auth.currentUser
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
//                 finish()
                        } else if (task.isCanceled) {
                            Toast.makeText(this, "Gagal SignUp", Toast.LENGTH_SHORT).show()
                            binding.prgBar.visibility = View.GONE
                            binding.btnRegis.visibility = View.VISIBLE
                            binding.tvLogin.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this, "Gagal SignUp", Toast.LENGTH_SHORT).show()
                            binding.prgBar.visibility = View.GONE
                            binding.btnRegis.visibility = View.VISIBLE
                            binding.tvLogin.visibility = View.VISIBLE

                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()

            }

        }
    }

}
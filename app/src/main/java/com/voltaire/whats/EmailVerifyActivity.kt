package com.voltaire.whats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.voltaire.whats.databinding.ActivityEmailVerifyBinding

class EmailVerifyActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEmailVerifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        binding.editEmail.text = getString(R.string.txtConfirmEmail, auth.currentUser!!.email)

        binding.btnExit.setOnClickListener {
            auth.signOut()
            openMainActivity()
        }
        binding.btnSendAgain.setOnClickListener {
            load(true)
            binding.btnEmailConfirmed.text = "Carregando"

            auth.currentUser!!.sendEmailVerification().addOnCompleteListener { sendEmail ->
                if (sendEmail.isSuccessful) {
                    toastCreator("E-mail de verificação enviado novamente.")
                } else {
                    toastCreator(sendEmail.exception?.message!!)
                }
                load(false)
                binding.btnEmailConfirmed.text = "ENVIAR EMAIL NOVAMENTE"
            }
        }
        binding.btnEmailConfirmed.setOnClickListener {
            load(true)
            binding.btnEmailConfirmed.text = "Carregando"
            auth.currentUser!!.reload().addOnCompleteListener {
                if (auth.currentUser!!.isEmailVerified) {
                    openMainActivity()
                } else {
                    toastCreator("E-mail ainda não confirmado.")
                }
                load(false)
                binding.btnEmailConfirmed.text = "EU JÁ CONFIRMEI"
            }
        }
    }

    private fun load(b: Boolean) {
        binding.btnSendAgain.isEnabled = !b
        binding.btnExit.isEnabled = !b
        binding.btnEmailConfirmed.isEnabled = !b
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun toastCreator(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
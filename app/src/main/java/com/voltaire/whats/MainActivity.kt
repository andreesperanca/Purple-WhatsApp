package com.voltaire.whats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.voltaire.whats.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            if (!user.isEmailVerified) {
                openVerifyEmailActivity()
            } else {
                openHomeActivity()
            }
        }

        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnEnter.setOnClickListener {

            if (loginVerify()) {

                loginLoading(true)

                auth.signInWithEmailAndPassword(
                    binding.editEmailLogin.text.toString(),
                    binding.editPasswordLogin.text.toString()
                ).addOnCompleteListener { login ->
                    if (login.isSuccessful) {
                        openHomeActivity()
                    } else {

                        toastCreator("Dados inválidos, verifique se e-mail e senha estão corretos")
                    }
                    loginLoading(false)

                }
            }
        }
    }

    private fun loginLoading(b: Boolean) {
        with(binding) {
            editEmailLogin.isEnabled = !b
            editPasswordLogin.isEnabled = !b

            btnEnter.isEnabled = !b
            btnRegister.isEnabled = !b

            btnEnter.text = if (b) "CARREGANDO" else "ENTRAR"
            progressBarLogin.visibility =  if (b) View.VISIBLE else View.INVISIBLE
        }
    }


    private fun loginVerify(): Boolean {
        if (binding.editEmailLogin.text.isEmpty() ||
            binding.editEmailLogin.text.isBlank() ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmailLogin.text).matches()
        ) {
            binding.editEmailLogin.error = "Email inválido."
            binding.editEmailLogin.requestFocus()
            return false
        }
        if (binding.editPasswordLogin.text.isBlank() ||
            binding.editPasswordLogin.text.isEmpty() ||
            binding.editPasswordLogin.text.length < 4
        ) {
            binding.editPasswordLogin.error = "Senha inválida"
            binding.editPasswordLogin.requestFocus()
            return false
        }
        return true
    }

    private fun openHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openVerifyEmailActivity() {
        val intent = Intent(this, EmailVerifyActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun toastCreator(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
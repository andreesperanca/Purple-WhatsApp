package com.voltaire.whats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voltaire.whats.databinding.ActivityRegisterBinding
import com.voltaire.whats.model.User
import com.voltaire.whats.utils.Constants

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.toolbarRegister.setNavigationOnClickListener {
            openMainActivity()
        }

        binding.btnRegisterScreen.setOnClickListener {
            register()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun register() {
        if (verifyRegister()) {
            registerLoading(true)

            auth.createUserWithEmailAndPassword(
                binding.editEmailRegister.text.toString(),
                binding.editPassword.text.toString()
            ).addOnCompleteListener { register ->

                if (register.isSuccessful) {
                    openMainActivity()

                    auth.currentUser!!.sendEmailVerification().addOnCompleteListener { sendEmail ->

                        if (sendEmail.isSuccessful) {

                            val db = FirebaseFirestore.getInstance()
                            val user = User(
                                binding.editNameRegister.text.toString(),
                                auth.currentUser!!.email.toString(),
                                Constants.URL_DEFAULT_PROFILE_PHOTO
                            )

                            db.collection("users")
                                .document(auth.currentUser!!.uid)
                                .set(user)
                                .addOnSuccessListener {

                                    openMainActivity()

                                }.addOnFailureListener {
                                    auth.currentUser!!.delete().addOnCompleteListener {
                                        toastCreator("Falhas foram encontradas : ${register.exception?.message}")
                                        registerLoading(false)
                                    }
                                }
                        } else {
                            auth.currentUser!!.delete().addOnCompleteListener {
                                toastCreator("Falhas foram encontradas : ${register.exception?.message}")
                                registerLoading(false)
                            }
                        }
                    }
                } else {
                    toastCreator("Verique os dados atribuidos aos campos referentes ao cadastro")
                    registerLoading(false)
                }
            }
        }
    }

    private fun registerLoading(b: Boolean) {
        binding.editNameRegister.isEnabled = !b
        binding.editEmailRegister.isEnabled = !b
        binding.editPassword.isEnabled = !b
        binding.repeatPasswordRegister.isEnabled = !b
        binding.btnRegisterScreen.isEnabled = !b
        binding.btnRegisterScreen.text = if (b) "Carregando" else "Cadastrar"
        binding.progressBarRegister.visibility = if (b) View.VISIBLE else View.INVISIBLE


    }

    private fun verifyRegister(): Boolean {

        if (binding.editNameRegister.text.isBlank() || binding.editNameRegister.text.isEmpty()) {
            binding.editNameRegister.error = "Nome de usuário inválido."
            binding.editNameRegister.requestFocus()
            return false
        }

        if (binding.editEmailRegister.text.isEmpty() ||
            binding.editEmailRegister.text.isBlank() ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmailRegister.text).matches()
        ) {
            binding.editEmailRegister.error = "Email inválido."
            binding.editEmailRegister.requestFocus()
            return false
        }

        if (binding.editPassword.text.isBlank() ||
            binding.editPassword.text.isEmpty() ||
            binding.editPassword.text.length < 4
        ) {
            binding.editPassword.error = "Senha inválida"
            binding.editPassword.requestFocus()
            return false
        }

        if (binding.repeatPasswordRegister.text.isBlank() ||
            binding.repeatPasswordRegister.text.isEmpty() ||
            binding.repeatPasswordRegister.text.length < 4 ||
            binding.repeatPasswordRegister.text == binding.editPassword.text
        ) {

            binding.repeatPasswordRegister.error = "Campo repetir senha está inválido."
            binding.repeatPasswordRegister.requestFocus()
            return false
        }

        return true
    }

    private fun toastCreator(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
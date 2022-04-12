package com.voltaire.whats

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.voltaire.whats.ui.main.SectionsPagerAdapter
import com.voltaire.whats.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    private lateinit var userLogged : DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUser()

        binding.fab.setOnClickListener { view ->
            initialChat()

        }
    }

    private fun initialChat() {
    }

    private fun loadUser() {
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                userLogged = result
                initialInterface()

            }
            .addOnFailureListener { exception ->
                exception.message?.let { toastCreator(it) }
            }

    }

    private fun initialInterface() {
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab
    }

    private fun toastCreator(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

}
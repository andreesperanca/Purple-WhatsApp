package com.voltaire.whats

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.voltaire.whats.ui.main.SectionsPagerAdapter
import com.voltaire.whats.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    var db = FirebaseFirestore.getInstance()
    var auth = FirebaseAuth.getInstance()
    lateinit var userLogged : DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadUser()
    }

    private fun loadUser() {

        db.collection("users")
            .document(auth.currentUser!!.email.toString())
            .addSnapshotListener { user, error ->
                userLogged = user!!
            }

        db.collection("users")
            .document(auth.currentUser!!.email.toString())
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

        binding.progressBarHome.visibility = View.INVISIBLE

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
    }

    private fun toastCreator(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
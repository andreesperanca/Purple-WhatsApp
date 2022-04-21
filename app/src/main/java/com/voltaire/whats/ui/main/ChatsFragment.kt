package com.voltaire.whats.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.voltaire.whats.R
import com.voltaire.whats.adapters.ChatsFragmentAdapter
import com.voltaire.whats.databinding.FragmentHomeBinding
import com.voltaire.whats.model.User
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import kotlin.concurrent.thread

class ChatsFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var adapter: ChatsFragmentAdapter
    private lateinit var recyclerView: RecyclerView
    private var dbReference = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUsers()

    }

    private fun loadUsers() {
        val list = mutableListOf<User>()

        val requestUsers = dbReference.collection("users").get()

        requestUsers.addOnCompleteListener { it: Task<QuerySnapshot> ->
            if (it.isSuccessful) {
                it.result.documents.map { it: DocumentSnapshot? ->
                    val newUser =
                        User(
                            it?.get("name").toString(),
                            it?.getString("email").toString(),
                            it?.getString("urlPhoto").toString()
                        )
                    list.add(newUser)
                    adapter = ChatsFragmentAdapter(list as ArrayList<User>)
                    recyclerView = binding!!.rvContacts
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.failure), Toast.LENGTH_SHORT).show()
            }
        }

    }


    companion object {

        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): ChatsFragment {
            return ChatsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
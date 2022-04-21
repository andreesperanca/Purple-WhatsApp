package com.voltaire.whats.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.voltaire.whats.HomeActivity
import com.voltaire.whats.MainActivity
import com.voltaire.whats.R
import com.voltaire.whats.ShowImage
import com.voltaire.whats.databinding.FragmentProfileBinding
import com.voltaire.whats.utils.Constants
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    val REQUISITION_GALERY_PHOTO = 101
    val REQUISITION_CAMERA_PHOTO = 102
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImageUser()
        loadUserInfo()
        attInfosListener()


        binding.profileImage.setOnClickListener {
            showPhotoUser()
        }

        binding.btnLogout.setOnClickListener {
            logOut()
        }

        binding.btnExcludeAccount.setOnClickListener {

        }

        binding.editNameButton.setOnClickListener {
            editUserName()
        }

        binding.editPasswordButton.setOnClickListener {
            modifyPasswordUser()
        }

        binding.profileImage.setOnClickListener {
            changePhotoProfile()
        }

        binding.btnExcludeAccount.setOnClickListener {
            excludeAccount()
        }
    }

    private fun excludeAccount() {

        val bottomDialogExcludeAccount = layoutInflater.inflate(R.layout.bottom_sheet_exclude_account, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(bottomDialogExcludeAccount)

        val btnContinue = bottomDialogExcludeAccount?.findViewById<Button>(R.id.btnContinue)
        val btnCancel = bottomDialogExcludeAccount?.findViewById<Button>(R.id.btnCancel)

        btnContinue?.setOnClickListener {
        saveExcludeAccount(bottomDialogExcludeAccount)
        }

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveExcludeAccount(btmSheetDialogExcludeAccount: View) {

        val editPasswordExcludeAccount =
            btmSheetDialogExcludeAccount?.findViewById<EditText>(R.id.editPasswordExcludeAccount)

        editPasswordExcludeAccount.isEnabled = false

        val credencial = EmailAuthProvider.getCredential(
            (activity as HomeActivity).auth.currentUser?.email.toString(),
            editPasswordExcludeAccount?.text.toString()
        )

        (activity as HomeActivity).auth.currentUser?.reauthenticate(credencial)
            ?.addOnCompleteListener { reautenticate ->

                if (reautenticate.isSuccessful) {

                    (activity as HomeActivity).auth.currentUser
                        ?.delete()
                        ?.addOnCompleteListener { excludeUser ->

                            if (excludeUser.isSuccessful) {

                                activity?.startActivity(
                                    Intent(
                                        activity,
                                        MainActivity::class.java
                                    )
                                )
                                activity?.finish()

                            } else {
                                Toast.makeText(activity, reautenticate.exception?.message, Toast.LENGTH_LONG).show()
                            }
                        }

                } else {
                    Toast.makeText(activity, reautenticate.exception?.message, Toast.LENGTH_LONG).show()
                    editPasswordExcludeAccount.isEnabled = true
                }
            }
    }

    private fun changePhotoProfile() {

        val bottomDialogPhoto = layoutInflater.inflate(R.layout.bottom_sheet_edit_photo, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(bottomDialogPhoto)

        val btnOpenGalery = bottomDialogPhoto.findViewById<ImageButton>(R.id.galeryPhotoProfile)
        val btnOpenCamera = bottomDialogPhoto.findViewById<ImageButton>(R.id.openCameraProfile)
        val btnExcludePhoto = bottomDialogPhoto.findViewById<ImageButton>(R.id.removePhotoProfile)

        btnOpenGalery?.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Photo"),
                REQUISITION_GALERY_PHOTO
            )
            dialog.dismiss()
        }

        btnOpenCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(requireActivity().packageManager)
            startActivityForResult(intent, REQUISITION_CAMERA_PHOTO)
            dialog.dismiss()
        }

        btnExcludePhoto.setOnClickListener {
            updateProfileImage(Constants.URL_DEFAULT_PROFILE_PHOTO)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUISITION_GALERY_PHOTO || requestCode == REQUISITION_CAMERA_PHOTO) {

            val storageReference = FirebaseStorage.getInstance().reference
            val referenceArquivo = storageReference.child(
                "profile images/"
                        + (activity as HomeActivity).auth.currentUser?.email.toString()
                        + "/"
                        + System.currentTimeMillis().toString()
            )
            when (requestCode) {
                REQUISITION_CAMERA_PHOTO -> {

                    if (resultCode == RESULT_OK) {

                        val imageBitMap = data?.extras?.get("data") as Bitmap
                        val baos = ByteArrayOutputStream()
                        imageBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val arquivo = baos.toByteArray()

                        val taskUploadArquivo = referenceArquivo.putBytes(arquivo)
                        taskUploadArquivo.addOnCompleteListener { upload ->
                            uploadTratment(upload, referenceArquivo)
                        }
                    }
                }

                REQUISITION_GALERY_PHOTO -> {
                    if (data != null) {
                        val arquivo = data.data
                        val taskUploadArquivo = referenceArquivo.putFile(arquivo!!)
                        taskUploadArquivo.addOnCompleteListener { upload ->
                            uploadTratment(upload, referenceArquivo)
                        }
                    }
                }
            }

        }
    }

    private fun uploadTratment(
        upload: Task<UploadTask.TaskSnapshot>,
        referenceArquivo: StorageReference
    ) {

        if (upload.isSuccessful) {
            referenceArquivo.downloadUrl.addOnSuccessListener { uri ->
                updateProfileImage(uri.toString())
            }.addOnFailureListener { exception ->
                Toast.makeText(activity, exception?.message, Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(activity, upload.exception?.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProfileImage(photo: String) {
        (activity as HomeActivity).db.collection("users")
            .document((activity as HomeActivity).auth.currentUser!!.email.toString())
            .update("urlPhoto", photo)
            .addOnFailureListener { exeception ->
                Toast.makeText(activity, exeception?.message, Toast.LENGTH_LONG).show()
            }


    }


    private fun modifyPasswordUser() {

        val bottomDialogPassword = layoutInflater.inflate(R.layout.bottom_sheet_edit_password, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetStyle)

        val editBtnCancel = bottomDialogPassword.findViewById<Button>(R.id.cancelBtnEditPassword)
        val editBtnSave = bottomDialogPassword.findViewById<Button>(R.id.saveBtnEditPassword)


        dialog.setContentView(bottomDialogPassword)

        editBtnCancel.setOnClickListener {
            dialog.cancel()
        }

        editBtnSave.setOnClickListener {
            savePassword(bottomDialogPassword, dialog)
        }
        dialog.show()
    }

    private fun savePassword(bottomDialogPassword: View?, dialog: BottomSheetDialog) {

        if (verifyPassword(bottomDialogPassword)) {

            loadChangePassword(bottomDialogPassword, true)

            val credencial = EmailAuthProvider.getCredential(
                (activity as HomeActivity).auth.currentUser?.email.toString(),
                bottomDialogPassword?.findViewById<EditText>(R.id.editCurrentPassword)?.text.toString()
            )

            (activity as HomeActivity).auth.currentUser?.reauthenticate(credencial)
                ?.addOnCompleteListener { reautenticate ->
                    if (reautenticate.isSuccessful) {

                        (activity as HomeActivity).auth.currentUser?.updatePassword(
                            bottomDialogPassword?.findViewById<EditText>(R.id.editNewPassword)?.text.toString()
                        )
                            ?.addOnCompleteListener { attPassword ->
                                if (attPassword.isSuccessful) {
                                    Toast.makeText(
                                        activity,
                                        "Senha alterada com sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(
                                        activity,
                                        "Erro ao atualizar a senha",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadChangePassword(bottomDialogPassword, false)
                                }
                            }

                    } else {
                        Toast.makeText(
                            activity,
                            reautenticate.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        loadChangePassword(bottomDialogPassword, false)
                    }
                }


        } else {
            Toast.makeText(activity, "Verifique os campos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadChangePassword(bottomDialog: View?, b: Boolean) {
        val newPassword = bottomDialog?.findViewById<EditText>(R.id.editNewPassword)
        val newPasswordConfirm = bottomDialog?.findViewById<EditText>(R.id.editConfirmNewPassword)
        val currentPassword = bottomDialog?.findViewById<EditText>(R.id.editCurrentPassword)
        val btnCancel = bottomDialog?.findViewById<Button>(R.id.cancelBtnEditPassword)
        val btnSave = bottomDialog?.findViewById<Button>(R.id.saveBtnEditPassword)

        newPassword?.isEnabled = !b
        newPasswordConfirm?.isEnabled = !b
        currentPassword?.isEnabled = !b
        btnCancel?.isEnabled = !b
        btnSave?.isEnabled = !b
    }

    private fun verifyPassword(bottomDialog: View?): Boolean {

        val editNewPassword = bottomDialog?.findViewById<EditText>(R.id.editNewPassword)
        val editNewPasswordConfirm =
            bottomDialog?.findViewById<EditText>(R.id.editConfirmNewPassword)


        val newPasswordConfirm = editNewPasswordConfirm!!.text.toString()
        val newPassword = editNewPassword!!.text.toString()

        if (newPassword.isBlank() || newPassword.isEmpty() || newPassword.length < 4) {
            editNewPassword.error = "Senha inválida"
            editNewPassword.requestFocus()
            return false
        }
        if (newPassword.length < 4) {
            editNewPassword.error = "Senha deve ter ao menos 4 caracteres."
            editNewPassword.requestFocus()
            return false
        }


        if (newPasswordConfirm != newPassword) {
            editNewPassword.error = "Nova senha e confirmar senha devem ser iguais."
            editNewPassword.requestFocus()
            return false
        }

        return true
    }

    private fun attInfosListener() {
        (activity as HomeActivity).db.collection("users")
            .document((activity as HomeActivity).auth.currentUser!!.email.toString())
            .addSnapshotListener { value, error ->
                loadImageUser()
                loadUserInfo()
            }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        activity?.startActivity(Intent(activity, MainActivity::class.java))
        activity?.finish()
    }

    private fun showPhotoUser() {
        val intent = Intent(activity, ShowImage::class.java)
        intent.putExtra("TITLE", "Foto de perfil")
        intent.putExtra("URL", (activity as HomeActivity).userLogged.getString("urlPhoto"))
        activity?.startActivity(intent)
    }

    private fun loadImageUser() {
        Picasso.get()
            .load((activity as HomeActivity).userLogged.getString("urlPhoto"))
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .into(binding.profileImage)

    }

    private fun loadUserInfo() {
        binding.userName.text = (activity as HomeActivity).userLogged.getString("name")
        binding.userEmail.text = (activity as HomeActivity).userLogged.getString("email")

    }

    private fun editUserName() {

        val bottomDialog = layoutInflater.inflate(R.layout.bottom_dialog_edit_name, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetStyle)

        dialog.setContentView(bottomDialog)
        val editTextName = bottomDialog.findViewById<EditText>(R.id.editTextName2)
        val editBtnSave = bottomDialog.findViewById<Button>(R.id.saveBtnEditName)
        val editBtnCancel = bottomDialog.findViewById<Button>(R.id.cancelBtnEditName)

        editTextName.setText((activity as HomeActivity).userLogged.getString("name"))
        editTextName.selectAll()

        editBtnCancel.setOnClickListener {
            dialog.cancel()
        }

        editBtnSave.setOnClickListener {
            saveNameUser(bottomDialog, dialog)
        }

        dialog.show()
    }

    private fun saveNameUser(btnSheetDialog: View, dialog: BottomSheetDialog) {
        btnSheetDialog.findViewById<Button>(R.id.saveBtnEditName)
        val editBtnSave = btnSheetDialog.findViewById<Button>(R.id.saveBtnEditName)
        val editBtnCancel = btnSheetDialog.findViewById<Button>(R.id.cancelBtnEditName)
        val editTextName = btnSheetDialog.findViewById<EditText>(R.id.editTextName2)

        editBtnCancel?.isEnabled = false
        editBtnSave?.isEnabled = false
        editTextName?.isEnabled = false

        (activity as HomeActivity).db.collection("users")
            .document((activity as HomeActivity).auth.currentUser!!.email.toString())
            .update("name", editTextName?.text.toString())
            .addOnCompleteListener { update ->
                if (update.isSuccessful) {
                    dialog.dismiss()
                } else {
                    Toast.makeText(activity, update?.exception?.message, Toast.LENGTH_SHORT).show()
                    editBtnCancel?.isEnabled = true
                    editBtnSave?.isEnabled = true
                    editTextName?.isEnabled = true
                }
            }
    }

    companion object {
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
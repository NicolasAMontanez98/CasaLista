package com.example.casalista.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.data.Cart
import com.example.casalista.data.Datos
import com.example.casalista.databinding.FragmentProfileBinding
import com.example.casalista.ui.auth.AuthActivity
import com.example.casalista.util.SessionManager
import com.example.casalista.util.ThemeManager
import com.example.casalista.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let(binding.avatar::setImageBitmap)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentProfileBinding.bind(view)
        val session = SessionManager(requireContext())
        val user = session.user ?: return

        binding.emailText.text = user.email
        binding.nameInput.setText(user.name)
        binding.phoneInput.setText(user.phone)
        binding.photoButton.setOnClickListener { takePhoto.launch(null) }
        binding.saveButton.setOnClickListener {
            val error = Validators.nameError(binding.nameInput.text.toString().trim())
            binding.nameLayout.error = error?.let(::getString)
            if (error == null) {
                user.name = binding.nameInput.text.toString().trim()
                user.phone = binding.phoneInput.text.toString().trim()
                Snackbar.make(view, R.string.profile_saved, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.fingerprintSwitch.isChecked = session.fingerprintEnabled
        binding.fingerprintSwitch.setOnCheckedChangeListener { _, on -> session.fingerprintEnabled = on }
        val isDark = resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        binding.darkSwitch.isChecked = isDark
        binding.darkSwitch.setOnCheckedChangeListener { _, on ->
            ThemeManager.setDark(requireContext(), on)
        }

        binding.logoutButton.setOnClickListener { leave(session) }
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_delete_title)
                .setMessage(R.string.profile_delete_message)
                .setNegativeButton(R.string.profile_cancel, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    Datos.users.remove(user)
                    leave(session)
                }
                .show()
        }
    }

    private fun leave(session: SessionManager) {
        session.lastEmail = null
        Cart.clear()
        startActivity(
            Intent(requireContext(), AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
    }
}

package com.example.casalista.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Cart
import com.example.casalista.databinding.FragmentProfileBinding
import com.example.casalista.ui.auth.AuthActivity
import com.example.casalista.util.SessionManager
import com.example.casalista.util.ThemeManager
import com.example.casalista.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Perfil del usuario (igual para los tres roles): editar nombre y teléfono, foto, activar la huella,
 * cambiar entre tema claro y oscuro, cerrar sesión y eliminar la cuenta.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    /** Recibe la foto tomada con la cámara y la muestra como avatar (solo en pantalla, no se guarda). */
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
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        AuthRepository.updateProfile(user)
                        Snackbar.make(view, R.string.profile_saved, Snackbar.LENGTH_SHORT).show()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Snackbar.make(view, AuthRepository.errorMessage(e), Snackbar.LENGTH_SHORT).show()
                    }
                }
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

        binding.logoutButton.setOnClickListener { leave() }
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_delete_title)
                .setMessage(R.string.profile_delete_message)
                .setNegativeButton(R.string.profile_cancel, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            AuthRepository.deleteAccount(user)
                            leave()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Snackbar.make(view, AuthRepository.errorMessage(e), Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                .show()
        }
    }

    /** Cierra la sesión, limpia los datos en memoria y vuelve a la pantalla de acceso. */
    private fun leave() {
        AuthRepository.logout()
        SessionManager.current = null
        Cart.clear()
        startActivity(
            Intent(requireContext(), AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
    }
}

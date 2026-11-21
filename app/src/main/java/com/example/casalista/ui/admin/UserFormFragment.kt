package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.AccountNotFoundException
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.data.UserRepository
import com.example.casalista.databinding.FragmentUserFormBinding
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class UserFormFragment : Fragment(R.layout.fragment_user_form) {

    private lateinit var binding: FragmentUserFormBinding
    private var current: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentUserFormBinding.bind(view)

        val labels = Role.entries.map { it.label }
        binding.roleInput.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels),
        )
        binding.roleInput.setText(Role.BUYER.label, false)
        binding.saveButton.setOnClickListener { save() }

        val uid = arguments?.getString("uid").orEmpty()
        if (uid.isNotEmpty()) loadUser(uid)
    }

    private fun loadUser(uid: String) {
        binding.saveButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = UserRepository.get(uid) ?: throw AccountNotFoundException()
                current = user
                binding.nameInput.setText(user.name)
                binding.emailInput.setText(user.email)
                binding.emailLayout.isEnabled = false
                binding.passwordLayout.isVisible = false
                binding.roleInput.setText(user.role.label, false)
                setupAccountActions(user)
                binding.saveButton.isEnabled = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
                findNavController().popBackStack()
            }
        }
    }

    private fun setupAccountActions(user: User) {
        if (user.uid == SessionManager.current?.uid) {
            binding.roleLayout.isEnabled = false
            return
        }
        binding.suspendButton.isVisible = true
        binding.suspendButton.setText(if (user.active) R.string.user_suspend else R.string.user_activate)
        binding.suspendButton.setOnClickListener {
            finishWith {
                user.active = !user.active
                UserRepository.update(user)
            }
        }

        binding.deleteButton.isVisible = true
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_delete_title)
                .setMessage(R.string.profile_delete_message)
                .setNegativeButton(R.string.detail_back, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    finishWith { UserRepository.delete(user) }
                }
                .show()
        }
    }

    private fun save() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val user = current

        val nameError = Validators.requiredError(name)
        val emailError = if (user == null) Validators.emailError(email) else null
        val passwordError = if (user == null) Validators.passwordError(password) else null
        binding.nameLayout.error = nameError?.let(::getString)
        binding.emailLayout.error = emailError?.let(::getString)
        binding.passwordLayout.error = passwordError?.let(::getString)
        if (listOf(nameError, emailError, passwordError).any { it != null }) return

        val role = Role.entries.first { it.label == binding.roleInput.text.toString() }
        finishWith {
            if (user == null) {
                UserRepository.create(name, email, password, role)
            } else {
                user.name = name
                user.role = role
                UserRepository.update(user)
            }
        }
    }

    private fun finishWith(action: suspend () -> Unit) {
        setButtonsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                action()
                findNavController().popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
                setButtonsEnabled(true)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.saveButton.isEnabled = enabled
        binding.suspendButton.isEnabled = enabled
        binding.deleteButton.isEnabled = enabled
    }

    private fun showError(e: Exception) {
        val message = getString(AuthRepository.errorMessage(e))
        if (e is FirebaseAuthUserCollisionException) {
            binding.emailLayout.error = message
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.databinding.FragmentUserFormBinding
import com.example.casalista.util.SessionManager
import com.example.casalista.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserFormFragment : Fragment(R.layout.fragment_user_form) {

    private lateinit var binding: FragmentUserFormBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentUserFormBinding.bind(view)
        val user = Datos.findByEmail(arguments?.getString("email").orEmpty())

        val labels = Role.entries.map { it.label }
        binding.roleInput.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels),
        )
        binding.roleInput.setText((user?.role ?: Role.BUYER).label, false)

        if (user != null) {
            binding.nameInput.setText(user.name)
            binding.emailInput.setText(user.email)
            binding.emailLayout.isEnabled = false
            binding.passwordLayout.hint = getString(R.string.user_password_optional)
            setupAccountActions(user)
        }
        binding.saveButton.setOnClickListener { save(user) }
    }

    private fun setupAccountActions(user: User) {
        if (user.email == SessionManager(requireContext()).lastEmail) {
            binding.roleLayout.isEnabled = false
            return
        }
        binding.suspendButton.isVisible = true
        binding.suspendButton.setText(if (user.active) R.string.user_suspend else R.string.user_activate)
        binding.suspendButton.setOnClickListener {
            user.active = !user.active
            findNavController().popBackStack()
        }

        binding.deleteButton.isVisible = true
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.user_delete_title)
                .setMessage(R.string.profile_delete_message)
                .setNegativeButton(R.string.detail_back, null)
                .setPositiveButton(R.string.profile_delete_confirm) { _, _ ->
                    Datos.users.remove(user)
                    findNavController().popBackStack()
                }
                .show()
        }
    }

    private fun save(current: User?) {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        val nameError = Validators.requiredError(name)
        val emailError = if (current == null) Validators.newEmailError(email) else null
        val passwordError =
            if (current != null && password.isEmpty()) null else Validators.passwordError(password)
        binding.nameLayout.error = nameError?.let(::getString)
        binding.emailLayout.error = emailError?.let(::getString)
        binding.passwordLayout.error = passwordError?.let(::getString)
        if (listOf(nameError, emailError, passwordError).any { it != null }) return

        val role = Role.entries.first { it.label == binding.roleInput.text.toString() }
        if (current == null) {
            Datos.users.add(User(name, email, password, role))
        } else {
            current.name = name
            current.role = role
            if (password.isNotEmpty()) current.password = password
        }
        findNavController().popBackStack()
    }
}

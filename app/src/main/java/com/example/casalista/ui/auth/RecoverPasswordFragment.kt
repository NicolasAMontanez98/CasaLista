package com.example.casalista.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.casalista.R
import com.example.casalista.databinding.FragmentRecoverPasswordBinding
import com.example.casalista.util.Validators

class RecoverPasswordFragment : Fragment(R.layout.fragment_recover_password) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentRecoverPasswordBinding.bind(view)
        binding.sendButton.setOnClickListener {
            val error = Validators.emailError(binding.emailInput.text.toString().trim())
            binding.emailLayout.error = error?.let(::getString)
            if (error == null) {
                binding.formGroup.isVisible = false
                binding.confirmationGroup.isVisible = true
            }
        }
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}

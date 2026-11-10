package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.ui.common.goTo
import com.example.casalista.data.Datos
import com.example.casalista.data.Role
import com.example.casalista.databinding.FragmentUsersBinding
import com.example.casalista.ui.common.UserAdapter
import com.example.casalista.ui.common.addFilterChip
import com.example.casalista.ui.common.selectedValue
import com.google.android.material.chip.ChipGroup

class UsersFragment : Fragment(R.layout.fragment_users) {

    private var role: Role? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUsersBinding.bind(view)
        val adapter = UserAdapter { openForm(it.email) }
        binding.userList.adapter = adapter
        binding.addButton.setOnClickListener { openForm("") }

        fun refresh() {
            val found = Datos.searchUsers(binding.searchInput.text.toString(), role)
            adapter.submitList(found)
            binding.emptyText.isVisible = found.isEmpty()
        }

        binding.searchInput.doAfterTextChanged { refresh() }
        binding.roleChips.addFilterChip(getString(R.string.catalog_all), null, role == null)
        Role.entries.forEach { binding.roleChips.addFilterChip(it.label, it.name, it == role) }
        binding.roleChips.setOnCheckedStateChangeListener { group, ids ->
            role = (group as ChipGroup).selectedValue(ids)?.let(Role::valueOf)
            refresh()
        }
        refresh()
    }

    private fun openForm(email: String) =
        goTo(R.id.userFormFragment, bundleOf("email" to email))
}

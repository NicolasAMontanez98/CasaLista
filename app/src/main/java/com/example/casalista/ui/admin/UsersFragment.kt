package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Role
import com.example.casalista.data.User
import com.example.casalista.data.UserRepository
import com.example.casalista.data.filterUsers
import com.example.casalista.databinding.FragmentUsersBinding
import com.example.casalista.ui.common.UserAdapter
import com.example.casalista.ui.common.addFilterChip
import com.example.casalista.ui.common.goTo
import com.example.casalista.ui.common.selectedValue
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Lista de usuarios con búsqueda por nombre o correo y filtro por rol. Desde aquí se crea o edita un usuario. */
class UsersFragment : Fragment(R.layout.fragment_users) {

    private var role: Role? = null
    private var allUsers = listOf<User>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUsersBinding.bind(view)
        val adapter = UserAdapter { openForm(it.uid) }
        binding.userList.adapter = adapter
        binding.addButton.setOnClickListener { openForm("") }

        /** Aplica la búsqueda y el rol elegido sobre la lista completa y muestra el aviso si no hay resultados. */
        fun refresh() {
            val found = filterUsers(allUsers, binding.searchInput.text.toString(), role)
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

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allUsers = UserRepository.getAll()
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), AuthRepository.errorMessage(e), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Abre el formulario de usuario; con un uid vacío crea uno nuevo. */
    private fun openForm(uid: String) =
        goTo(R.id.userFormFragment, bundleOf("uid" to uid))
}

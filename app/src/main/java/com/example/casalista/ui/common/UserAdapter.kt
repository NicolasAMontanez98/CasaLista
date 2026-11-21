package com.example.casalista.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.casalista.R
import com.example.casalista.data.User
import com.example.casalista.databinding.ItemUserBinding

/** Adaptador de usuarios: cada fila muestra el nombre, el correo y el rol (o "suspendido"). */
class UserAdapter(private val onClick: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.Holder>() {

    private var items = listOf<User>()

    /** Reemplaza la lista y vuelve a dibujarla. */
    fun submitList(list: List<User>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    /** Guarda las vistas de una fila para reutilizarlas. */
    class Holder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /** Llena una fila con los datos del usuario. */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = items[position]
        with(holder.binding) {
            userName.text = user.name
            userEmail.text = user.email
            userRole.text = if (user.active) user.role.label
            else root.context.getString(R.string.user_role_suspended, user.role.label)
            root.setOnClickListener { onClick(user) }
        }
    }
}

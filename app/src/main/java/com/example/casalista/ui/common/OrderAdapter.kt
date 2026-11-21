package com.example.casalista.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.casalista.R
import com.example.casalista.data.SaleOrder
import com.example.casalista.databinding.ItemOrderBinding
import com.example.casalista.util.toPesos

/** Adaptador de pedidos: cada fila muestra el código, el resumen, el total y el estado. */
class OrderAdapter(private val onClick: (SaleOrder) -> Unit) :
    RecyclerView.Adapter<OrderAdapter.Holder>() {

    private var items = listOf<SaleOrder>()

    /** Reemplaza la lista y vuelve a dibujarla. */
    fun submitList(list: List<SaleOrder>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    /** Guarda las vistas de una fila para reutilizarlas. */
    class Holder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /** Llena una fila con los datos del pedido. */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val order = items[position]
        with(holder.binding) {
            orderCode.text = order.code
            orderSummary.text = root.context.getString(
                R.string.order_summary, order.buyer, order.houseName, order.quantity,
            )
            orderTotal.text = order.total.toPesos()
            orderStatus.text = order.status.label
            root.setOnClickListener { onClick(order) }
        }
    }
}

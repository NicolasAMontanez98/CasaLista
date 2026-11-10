package com.example.casalista.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.casalista.data.CartItem
import com.example.casalista.databinding.ItemCartBinding
import com.example.casalista.util.toPesos

class CartAdapter(
    private val onMinus: (CartItem) -> Unit,
    private val onPlus: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit,
) : RecyclerView.Adapter<CartAdapter.Holder>() {

    private var items = listOf<CartItem>()

    fun submitList(list: List<CartItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    class Holder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            cartName.text = item.house.name
            cartUnitPrice.text = item.house.price.toPesos()
            quantityText.text = item.quantity.toString()
            lineTotal.text = (item.house.price * item.quantity).toPesos()
            minusButton.setOnClickListener { onMinus(item) }
            plusButton.setOnClickListener { onPlus(item) }
            removeButton.setOnClickListener { onRemove(item) }
        }
    }
}

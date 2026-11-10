package com.example.casalista.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.casalista.R
import com.example.casalista.data.House
import com.example.casalista.databinding.ItemHouseBinding
import com.example.casalista.util.toPesos

class HouseAdapter(private val onClick: (House) -> Unit) :
    RecyclerView.Adapter<HouseAdapter.Holder>() {

    private var items = listOf<House>()

    fun submitList(list: List<House>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    class Holder(val binding: ItemHouseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemHouseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val house = items[position]
        with(holder.binding) {
            houseName.text = house.name
            houseDetails.text = root.context.getString(
                R.string.house_details, house.area, house.bedrooms, house.system,
            )
            housePrice.text = house.price.toPesos()
            houseStatus.text = house.status
            root.setOnClickListener { onClick(house) }
        }
    }
}

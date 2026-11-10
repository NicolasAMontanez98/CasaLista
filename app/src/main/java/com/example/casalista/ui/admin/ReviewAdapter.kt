package com.example.casalista.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.casalista.R
import com.example.casalista.data.House
import com.example.casalista.databinding.ItemReviewBinding
import com.example.casalista.util.toPesos

class ReviewAdapter(private val onReview: (House, Boolean) -> Unit) :
    RecyclerView.Adapter<ReviewAdapter.Holder>() {

    private var items = listOf<House>()

    fun submitList(list: List<House>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    class Holder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val house = items[position]
        with(holder.binding) {
            reviewName.text = house.name
            reviewDetails.text = root.context.getString(
                R.string.house_details, house.area, house.bedrooms, house.system,
            )
            reviewPrice.text = house.price.toPesos()
            approveButton.setOnClickListener { onReview(house, true) }
            rejectButton.setOnClickListener { onReview(house, false) }
        }
    }
}

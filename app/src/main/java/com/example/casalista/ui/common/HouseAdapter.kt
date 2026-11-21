package com.example.casalista.ui.common

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.casalista.R
import com.example.casalista.data.House
import com.example.casalista.databinding.ItemHouseBinding
import com.example.casalista.util.toPesos

/** Adaptador de listas de casas: cada fila muestra la foto, el nombre, los datos, el precio y el estado. */
class HouseAdapter(private val onClick: (House) -> Unit) :
    RecyclerView.Adapter<HouseAdapter.Holder>() {

    private var items = listOf<House>()

    /** Reemplaza la lista y vuelve a dibujarla. */
    fun submitList(list: List<House>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    /** Guarda las vistas de una fila para reutilizarlas. */
    class Holder(val binding: ItemHouseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemHouseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /** Llena una fila: carga la primera foto con Glide o, si no hay, muestra el ícono de casa. */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val house = items[position]
        with(holder.binding) {
            houseName.text = house.name
            houseDetails.text = root.context.getString(
                R.string.house_details, house.area, house.bedrooms, house.system,
            )
            housePrice.text = house.price.toPesos()
            houseStatus.text = house.status
            val photo = house.images.firstOrNull()
            if (photo == null) {
                Glide.with(houseImage).clear(houseImage)
                houseImage.setImageResource(R.drawable.ic_home)
                houseImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                houseImage.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.gold))
            } else {
                houseImage.imageTintList = null
                houseImage.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(houseImage).load(photo).into(houseImage)
            }
            root.setOnClickListener { onClick(house) }
        }
    }
}

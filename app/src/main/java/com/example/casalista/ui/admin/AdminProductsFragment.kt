package com.example.casalista.ui.admin

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.casalista.R
import com.example.casalista.data.Datos
import com.example.casalista.data.Status
import com.example.casalista.databinding.FragmentAdminProductsBinding

class AdminProductsFragment : Fragment(R.layout.fragment_admin_products) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAdminProductsBinding.bind(view)
        lateinit var adapter: ReviewAdapter

        fun refresh() {
            val queue = Datos.houses.filter { it.status == Status.REVIEW }
            adapter.submitList(queue)
            binding.emptyText.isVisible = queue.isEmpty()
        }

        adapter = ReviewAdapter { house, approved ->
            Datos.reviewHouse(house.id, approved)
            refresh()
        }
        binding.reviewList.adapter = adapter
        refresh()
    }
}

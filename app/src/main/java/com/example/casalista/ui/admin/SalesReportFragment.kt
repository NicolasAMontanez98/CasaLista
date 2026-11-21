package com.example.casalista.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.casalista.R
import com.example.casalista.data.AuthRepository
import com.example.casalista.data.Datos
import com.example.casalista.data.HouseRepository
import com.example.casalista.data.OrderRepository
import com.example.casalista.databinding.FragmentSalesReportBinding
import com.example.casalista.ui.common.addFilterChip
import com.example.casalista.ui.common.selectedValue
import com.example.casalista.util.salesCsv
import com.example.casalista.util.toPesos
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Reporte de ventas: gráfica por mes (últimos 3 o 6 meses), gráfica por sistema constructivo y
 * botón para compartir los datos en formato CSV.
 */
class SalesReportFragment : Fragment(R.layout.fragment_sales_report) {

    private var months = 6

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentSalesReportBinding.bind(view)
        var bySystem = listOf<Pair<String, Long>>()
        /** Redibuja la gráfica mensual y el total del periodo elegido. */
        fun refresh() {
            val sales = Datos.monthlySales(months)
            binding.monthlyChart.bars = sales
            binding.monthlyChart.contentDescription = getString(R.string.report_chart_desc, months)
            binding.periodTotal.text = getString(R.string.report_total, sales.sumOf { it.second }.toPesos())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                HouseRepository.load()
                OrderRepository.loadAll()
                bySystem = Datos.salesBySystem()
                binding.systemChart.bars = bySystem
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Snackbar.make(view, AuthRepository.errorMessage(e), Snackbar.LENGTH_SHORT).show()
            }
        }


        binding.periodChips.addFilterChip(getString(R.string.report_period_3), "3", months == 3)
        binding.periodChips.addFilterChip(getString(R.string.report_period_6), "6", months == 6)
        binding.periodChips.setOnCheckedStateChangeListener { group, ids ->
            months = (group as ChipGroup).selectedValue(ids)!!.toInt()
            refresh()
        }
        refresh()

        binding.csvButton.setOnClickListener {
            val csv = salesCsv(Datos.monthlySales(months), bySystem)
            val send = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_share_title))
                .putExtra(Intent.EXTRA_TEXT, csv)
            startActivity(Intent.createChooser(send, null))
        }
        binding.pdfButton.setOnClickListener {
            Snackbar.make(view, R.string.report_pdf_soon, Snackbar.LENGTH_SHORT).show()
        }
    }
}

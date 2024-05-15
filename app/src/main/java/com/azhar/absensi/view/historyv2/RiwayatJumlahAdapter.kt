package com.azhar.absensi.view.historyv2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.databinding.ListHistorySppBinding
import com.azhar.absensi.model.DataSpp
import com.azhar.absensi.utils.formatToRupiah
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatJumlahAdapter(val dataListSpp: ArrayList<DataSpp>) :
RecyclerView.Adapter<RiwayatJumlahAdapter.ListViewHolder>() {

    inner class ListViewHolder(var binding: ListHistorySppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ListViewHolder {
        val binding = ListHistorySppBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataListSpp.size
    }

    override fun onBindViewHolder(p0: ListViewHolder, p1: Int) {
        val dataSpp = dataListSpp[p1]
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        p0.binding.tvTanggal.text = dateFormat.format(dataSpp.tgl_bayar)
        p0.binding.total.text = formatToRupiah(dataSpp.nominal)
    }
}
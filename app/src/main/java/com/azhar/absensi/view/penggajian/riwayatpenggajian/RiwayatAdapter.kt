package com.azhar.absensi.view.penggajian.riwayatpenggajian

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.model.Penggajian
import java.text.NumberFormat
import java.util.Locale

class RiwayatAdapter(private var penggajianList: List<Penggajian>) : RecyclerView.Adapter<RiwayatAdapter.PenggajianViewHolder>() {

    inner class PenggajianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namaGuru: TextView = itemView.findViewById(R.id.tv_nama_guru)
        private val gaji1: TextView = itemView.findViewById(R.id.tv_gaji_guru)
        private val tgl: TextView = itemView.findViewById(R.id.tv_tgl)

        fun bind(penggajian: Penggajian) {
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            formatRupiah.maximumFractionDigits = 0
            val totalGaji = penggajian.gaji1 + penggajian.gaji2 + penggajian.bonus
            val formatTotalGaji = formatRupiah.format(totalGaji)
            namaGuru.text = penggajian.nama_guru
            gaji1.text = formatTotalGaji.toString()
            tgl.text = penggajian.tgl_gaji
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenggajianViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_riwayat_penggajian, parent, false)
        return PenggajianViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PenggajianViewHolder, position: Int) {
        holder.bind(penggajianList[position])
    }

    override fun getItemCount() = penggajianList.size

    fun updateData(newPenggajianList: List<Penggajian>) {
        penggajianList = newPenggajianList
        notifyDataSetChanged()
    }
}

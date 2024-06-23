package com.azhar.absensi.view.spp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.databinding.ListRiwayatSppBinding
import com.azhar.absensi.model.DataSpp
import com.azhar.absensi.model.GetDataSpp
import com.azhar.absensi.utils.formatToRupiah
import com.azhar.absensi.utils.stampToSimpleDate
import com.bumptech.glide.Glide

class CardListSppAdapter() :
 ListAdapter<GetDataSpp, CardListSppAdapter.ListViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemClicked(data: GetDataSpp)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    inner class ListViewHolder(var binding: ListRiwayatSppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ListViewHolder {
        val binding = ListRiwayatSppBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(p0: ListViewHolder, p1: Int) {
        val dataSpp = getItem(p1)
        p0.binding.also { view ->
            Glide.with(p0.itemView.context)
                .load(dataSpp.foto)
                .into(view.imageProfile)
            view.tvNamaMurid.text = dataSpp.nama_murid
            view.tvNamaGuru.text = dataSpp.nama_guru
            view.tvJatuhTempo.text = stampToSimpleDate(dataSpp.jatuh_tempo)
            view.tvTanggalBayar.text = stampToSimpleDate(dataSpp.tgl_bayar)
            view.tvJenisLes.text = dataSpp.jenis_les
            view.tvjumlahspp.text = formatToRupiah(dataSpp.nominal)
        }
        p0.itemView.setOnClickListener { onItemClickCallback.onItemClicked(dataSpp) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GetDataSpp>(){
            override fun areItemsTheSame(p0: GetDataSpp, p1: GetDataSpp): Boolean {
                return p0 == p1
            }

            override fun areContentsTheSame(p0: GetDataSpp, p1: GetDataSpp): Boolean {
                return p0.timestamp == p1.timestamp
            }

        }
    }
}
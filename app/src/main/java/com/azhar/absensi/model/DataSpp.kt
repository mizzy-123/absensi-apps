package com.azhar.absensi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class DataSpp(
    val nama_murid: String,
    val nama_guru: String,
    val foto: String,
    val jatuh_tempo: Long,
    val tgl_bayar: Long,
    val jenis_les: String,
    var nominal: Int,
    val timestamp: Long
)

@Parcelize
class GetDataSpp(
    val id: String,
    val nama_murid: String,
    val nama_guru: String,
    val foto: String,
    val jatuh_tempo: Long,
    val tgl_bayar: Long,
    val jenis_les: String,
    var nominal: Int,
    val timestamp: Long
) : Parcelable
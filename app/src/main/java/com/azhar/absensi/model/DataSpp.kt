package com.azhar.absensi.model

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
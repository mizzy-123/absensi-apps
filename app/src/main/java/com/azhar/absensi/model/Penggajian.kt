package com.azhar.absensi.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Penggajian(
    val id: String = "",
    val nama_guru: String = "",
    val gaji1: Int = 0,
    val gaji2: Int = 0,
    val tgl_gaji: String = "",
    val bonus: Int = 0
) : Parcelable
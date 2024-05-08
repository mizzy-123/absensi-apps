package com.azhar.absensi.utils

import java.text.NumberFormat
import java.util.Locale

fun formatToRupiah(number: Int): String {
    val localeID = Locale("id", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return numberFormat.format(number)
}
package com.azhar.absensi.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatToRupiah(number: Int): String {
    val localeID = Locale("id", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    return numberFormat.format(number)
}

fun stampToSimpleDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    return sdf.format(date)
}
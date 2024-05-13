package com.azhar.absensi.view.penggajian.penggajianguru

class ClearValue {
    companion object {
        fun hapus(value: String): String {
            return value.replace(Regex("[^\\d]"), "")
        }
    }
}
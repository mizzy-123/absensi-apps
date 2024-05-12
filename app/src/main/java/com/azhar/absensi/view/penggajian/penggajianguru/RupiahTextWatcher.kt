package com.azhar.absensi.view.penggajian.penggajianguru

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.ParseException

class RupiahTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""
    private val df = DecimalFormat("#,###")

    init {
        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace("[Rp ,.]".toRegex(), "")
            if (cleanString.isNotEmpty()) {
                try {
                    val parsed = cleanString.toDouble()
                    val formatted = "Rp " + df.format(parsed).replace(",", ".")
                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                } catch (ex: ParseException) {
                    ex.printStackTrace()
                }
            }

            editText.addTextChangedListener(this)
        }
    }
}

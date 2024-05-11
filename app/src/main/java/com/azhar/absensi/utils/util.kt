package com.azhar.absensi.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.File

fun uriToByteArray(con: Context,imageUri: Uri): ByteArray? {
    val inputStream = con.contentResolver.openInputStream(imageUri)
    val cursor = con.contentResolver.query(imageUri, null, null, null, null)
    var byteArray: ByteArray? = null
    cursor?.use { c ->
        val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (c.moveToFirst()){
            val name = c.getString(nameIndex)
            inputStream?.let { inputStream ->
                val file = File(con.cacheDir, name)
                val os = file.outputStream()
                os.use {
                    inputStream.copyTo(it)
                }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
                val byteArray2 = byteArrayOutputStream.toByteArray()
                byteArray = byteArray2
            }
        }
    }

    return byteArray
}
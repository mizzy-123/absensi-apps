package com.azhar.absensi.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Firestore private constructor() {
    val db = Firebase.firestore

    companion object {
        val instance: Firestore by lazy { Firestore() }
    }

    fun getDatabase(): FirebaseFirestore {
        return db
    }
}
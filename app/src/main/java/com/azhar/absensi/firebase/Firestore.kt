package com.azhar.absensi.firebase

import com.azhar.absensi.model.Penggajian
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
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

    fun getCollection(): CollectionReference {
        return db.collection("users")
    }

    fun getDocument(userId: String): DocumentReference {
        return getCollection().document(userId)
    }

    fun getPenggajian(userId: String, onSuccess: (List<Penggajian>) -> Unit, onFailure: (Exception) -> Unit) {
        getDocument(userId).collection("gaji").get()
            .addOnSuccessListener { querySnapshot ->
                val penggajianList = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Penggajian::class.java)
                }
                onSuccess(penggajianList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
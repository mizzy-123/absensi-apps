package com.azhar.absensi.view.penggajian.penggajianguru

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azhar.absensi.firebase.Firestore
import com.azhar.absensi.model.Penggajian

class PenggajianGuruViewModel(private val firestore: Firestore) : ViewModel() {

    private val _uploadState = MutableLiveData<Result<Unit>>()
    val uploadState: LiveData<Result<Unit>> = _uploadState

    private val _penggajianList = MutableLiveData<List<Penggajian>>()
    val penggajianList: LiveData<List<Penggajian>> = _penggajianList

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    fun uploadPenggajian(userId: String, penggajianData: Map<String, Any>) {
        firestore.getDocument(userId).collection("gaji").add(penggajianData)
            .addOnSuccessListener {
                _uploadState.value = Result.success(Unit)
            }
            .addOnFailureListener { exception ->
                _uploadState.value = Result.failure(exception)
            }
    }

    fun fetchPenggajian(userId: String) {
        firestore.getPenggajian(userId,
            onSuccess = { penggajianList ->
                _penggajianList.value = penggajianList
            },
            onFailure = { exception ->
                _error.value = exception
            }
        )
    }
}

class PenggajianGuruViewModelFactory(private val firestore: Firestore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PenggajianGuruViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PenggajianGuruViewModel(firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

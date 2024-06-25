package com.azhar.absensi.view.penggajian.penggajianguru

import android.os.Handler
import android.os.Looper
import android.util.Log
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

    private val _searchResults = MutableLiveData<List<Penggajian>>()
    val searchResults: LiveData<List<Penggajian>> = _searchResults

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _infoMessage = MutableLiveData<String>()
    val infoMessage: LiveData<String> = _infoMessage

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val searchDelay = 600L // 500 milliseconds delay

    fun uploadPenggajian(userId: String, penggajianData: Map<String, Any>) {
        firestore.getDocument(userId).collection("gaji").add(penggajianData)
            .addOnSuccessListener {
                _uploadState.value = Result.success(Unit)

            }
            .addOnFailureListener { exception ->
                _uploadState.value = Result.failure(exception)

            }
    }
    fun updatePenggajian(userId: String,documentId:String, penggajianData: Map<String, Any>) {
        firestore.getDocument(userId).collection("gaji").document(documentId).set(penggajianData)
            .addOnSuccessListener {
                _uploadState.value = Result.success(Unit)

            }
            .addOnFailureListener { exception ->
                _uploadState.value = Result.failure(exception)

            }
    }

    fun fetchPenggajian(userId: String) {
        _isLoading.value =true
        firestore.getPenggajian(userId,
            onSuccess = { penggajianList ->
                _penggajianList.value = penggajianList
                _isLoading.value=false
            },
            onFailure = { exception ->
                _isLoading.value=false
                _error.value = exception
            }
        )
    }

    fun setSearchQuery(query: String, userId: String) {
        searchRunnable?.let { handler.removeCallbacks(it) }

        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _isLoading.value = false
        } else if (query.length > 1) {
            searchRunnable = Runnable { searchPenggajian(query, userId) }
            handler.postDelayed(searchRunnable!!, searchDelay)
        }
    }

    private fun searchPenggajian(query: String,userId: String) {
        _isLoading.value=true
        firestore.searchPenggajianByNamaGuru(query,userId,
            onSuccess = { penggajianList ->
                _isLoading.value=false
                if (penggajianList.isEmpty()) {
                    _infoMessage.value = "Mohon maaf, data yang anda cari tidak ada"
                }
                _searchResults.value = penggajianList
                Log.d("SEARCH", "${_searchResults.value}")
            },
            onFailure = { exception ->
                _isLoading.value=false
                _error.value = exception
                Log.d("SEARCH", "${_error.value}")
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

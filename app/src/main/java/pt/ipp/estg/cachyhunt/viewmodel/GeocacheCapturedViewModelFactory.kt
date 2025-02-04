package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository

class GeocacheCapturedViewModelFactory(
    private val repository: GeocacheCapturedRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeocacheCapturedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeocacheCapturedViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
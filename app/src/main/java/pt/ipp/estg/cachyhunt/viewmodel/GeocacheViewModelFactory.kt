package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository

class GeocacheViewModelFactory(
    private val questionrepository: QuestionRepository,
    private val repository: GeocacheRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeocacheViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeocacheViewModel(questionrepository, repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
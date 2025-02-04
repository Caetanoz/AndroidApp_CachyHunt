package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository

class QuestionViewModelFactory(
    private val questionRepository: QuestionRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionViewModel(questionRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
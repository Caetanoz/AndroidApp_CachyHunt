package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.utils.NetworkConnection

class QuestionViewModel(private val questionRepository: QuestionRepository, private val context: Context) : ViewModel() {

    fun insertQuestion(question: Question, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("QuestionViewModel", "Inserting question into local database")
                // Insert question into local database and get the generated ID
                val generatedId = questionRepository.insertQuestion(question)
                val updatedQuestion = question.copy(id = generatedId)
                Log.d("QuestionViewModel", "Question inserted with ID: $generatedId")

                if (NetworkConnection.isInternetAvailable(context)) {
                    Log.d("QuestionViewModel", "Internet is available, saving question to Firestore")
                    // Save the updated question to Firestore
                    questionRepository.saveQuestionToFirestore(updatedQuestion, {
                        Log.d("QuestionViewModel", "Question successfully saved to Firestore")
                        onSuccess()
                    }, {
                        Log.e("QuestionViewModel", "Failed to save question to Firestore")
                        onError()
                    })
                } else {
                    Log.w("QuestionViewModel", "Internet is not available, skipping Firestore save for question")
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("QuestionViewModel", "Error inserting question: ${e.message}", e)
                onError()
            }
        }
    }

    fun getQuestionById(id: Int): LiveData<Question?> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("QuestionViewModel", "Fetching question with ID $id from Firestore")
            questionRepository.getQuestionByIdFromFirestore(id)
        } else {
            Log.d("QuestionViewModel", "Fetching question with ID $id from local database")
            questionRepository.getQuestionById(id)
        }
    }

    fun getAllQuestions(): LiveData<List<Question>> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("QuestionViewModel", "Fetching all questions from Firestore")
            questionRepository.getAllQuestionsFromFirestore()
        } else {
            Log.d("QuestionViewModel", "Fetching all questions from local database")
            questionRepository.getAllQuestions()
        }
    }
}
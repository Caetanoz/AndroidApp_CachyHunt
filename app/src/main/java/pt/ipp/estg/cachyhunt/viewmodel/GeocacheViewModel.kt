package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.utils.NetworkConnection

class GeocacheViewModel(private val questionRepository: QuestionRepository, private val repository: GeocacheRepository, private val context: Context) : ViewModel() {

    fun insertGeocache(geocache: Geocache, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("GeocacheViewModel", "Inserting geocache into local database")
                val generatedId = repository.insertGeocache(geocache)
                val updatedGeocache = geocache.copy(id = generatedId.toInt())
                Log.d("GeocacheViewModel", "Geocache inserted with ID: ${updatedGeocache.id}")

                if (NetworkConnection.isInternetAvailable(context)) {
                    Log.d("GeocacheViewModel", "Internet is available, inserting geocache into Firestore")
                    repository.insertGeocacheToFirestore(updatedGeocache, {
                        Log.d("GeocacheViewModel", "Geocache successfully inserted into Firestore")
                        onSuccess()
                    }, {
                        Log.e("GeocacheViewModel", "Failed to insert geocache into Firestore")
                        onError()
                    })
                } else {
                    Log.w("GeocacheViewModel", "Internet is not available, skipping Firestore insertion")
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("GeocacheViewModel", "Error inserting geocache: ${e.message}", e)
                onError()
            }
        }
    }

    fun getGeocacheById(geocacheId: Int): LiveData<Geocache?> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("GeocacheViewModel", "Fetching geocache from Firestore with ID: $geocacheId")
            repository.getGeocacheFromFirestore(geocacheId)
        } else {
            Log.d("GeocacheViewModel", "Fetching geocache from local database with ID: $geocacheId")
            repository.getGeocacheById(geocacheId)
        }
    }

    fun getAllGeocaches(): LiveData<List<Geocache>> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("GeocacheViewModel", "Fetching all geocaches from Firestore")
            repository.getAllGeocachesFromFirestore()
        } else {
            Log.d("GeocacheViewModel", "Fetching all geocaches from local database")
            repository.getAllGeocaches()
        }
    }

    fun updateGeocache(geocache: Geocache, onSuccess: () -> Unit, onError: () -> Unit) {
        Log.d("GeocacheViewModel", "Starting updateGeocache for geocache: $geocache")

        if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("GeocacheViewModel", "Internet is available, updating geocache in Firestore: $geocache")
            repository.updateGeocacheInFirestore(geocache, {
                Log.d("GeocacheViewModel", "Successfully updated geocache in Firestore: $geocache")
                viewModelScope.launch {
                    repository.updateGeocache(geocache)
                    Log.d("GeocacheViewModel", "Successfully updated geocache in local database: $geocache")
                    onSuccess()
                }
            }, {
                Log.e("GeocacheViewModel", "Error updating geocache in Firestore: $geocache")
                onError()
            })
        } else {
            Log.d("GeocacheViewModel", "Internet is not available, updating geocache in local database: $geocache")
            viewModelScope.launch {
                repository.updateGeocache(geocache)
                Log.d("GeocacheViewModel", "Successfully updated geocache in local database: $geocache")
                onSuccess()
            }
        }
    }

    fun insertQuestionAndGeocache(
        question: Question,
        geocache: Geocache,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("GeocacheViewModel", "Inserting question into local database")
                // Insert question into local database and get the generated ID
                val generatedQuestionId = questionRepository.insertQuestion(question)
                val updatedQuestion = question.copy(id = generatedQuestionId)
                Log.d("GeocacheViewModel", "Question inserted with ID: $generatedQuestionId")

                if (NetworkConnection.isInternetAvailable(context)) {
                    Log.d("GeocacheViewModel", "Internet is available, saving question to Firestore")
                    // Save the updated question to Firestore
                    questionRepository.saveQuestionToFirestore(updatedQuestion, {
                        Log.d("GeocacheViewModel", "Question successfully saved to Firestore")
                        // Insert geocache after question is successfully saved
                        insertGeocacheWithQuestionId(geocache, generatedQuestionId, onSuccess, onError)
                    }, {
                        Log.e("GeocacheViewModel", "Failed to save question to Firestore")
                        onError()
                    })
                } else {
                    Log.w("GeocacheViewModel", "Internet is not available, skipping Firestore save for question")
                    // Insert geocache after question is successfully saved locally
                    insertGeocacheWithQuestionId(geocache, generatedQuestionId, onSuccess, onError)
                }
            } catch (e: Exception) {
                Log.e("GeocacheViewModel", "Error inserting question: ${e.message}", e)
                onError()
            }
        }
    }

    private fun insertGeocacheWithQuestionId(
        geocache: Geocache,
        questionId: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("GeocacheViewModel", "Inserting geocache into local database with question ID: $questionId")
                val generatedId = repository.insertGeocache(geocache.copy(questionId = questionId))
                val updatedGeocache = geocache.copy(id = generatedId, questionId = questionId)
                Log.d("GeocacheViewModel", "Geocache inserted with ID: ${updatedGeocache.id}")
                Log.d("GeocacheViewModel", "Geocache inserted with Question ID: ${updatedGeocache.questionId}")
                if (NetworkConnection.isInternetAvailable(context)) {
                    Log.d("GeocacheViewModel", "Internet is available, inserting geocache into Firestore")
                    repository.insertGeocacheToFirestore(updatedGeocache, {
                        Log.d("GeocacheViewModel", "Geocache successfully inserted into Firestore")
                        onSuccess()
                    }, {
                        Log.e("GeocacheViewModel", "Failed to insert geocache into Firestore")
                        onError()
                    })
                } else {
                    Log.w("GeocacheViewModel", "Internet is not available, skipping Firestore insertion for geocache")
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("GeocacheViewModel", "Error inserting geocache: ${e.message}", e)
                onError()
            }
        }
    }
}
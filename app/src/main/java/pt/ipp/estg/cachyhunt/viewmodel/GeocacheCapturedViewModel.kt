package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.utils.NetworkConnection

class GeocacheCapturedViewModel(
    private val geocacheCapturedRepository: GeocacheCapturedRepository,
    private val context: Context
) : ViewModel() {

    fun insertGeocacheCaptured(
        geocacheCaptured: GeocacheCaptured,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Insert geocacheCaptured into local database and get the generated ID
                val generatedId =
                    geocacheCapturedRepository.insertGeocacheCaptured(geocacheCaptured)
                val updatedGeocacheCaptured = geocacheCaptured.copy(id = generatedId)

                if (NetworkConnection.isInternetAvailable(context)) {
                    // Save the updated geocacheCaptured to Firestore
                    geocacheCapturedRepository.saveGeocacheCapturedToFirestore(
                        updatedGeocacheCaptured,
                        {
                            onSuccess()
                        },
                        onError
                    )
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun getGeocachesByUserId(userId: Int): LiveData<List<GeocacheCaptured>> {
        Log.d("GeocacheCapturedViewModel", "Checking internet connection for user ID: $userId")
        return if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("GeocacheCapturedViewModel", "Internet is available, fetching geocaches from Firestore for user ID: $userId")
            geocacheCapturedRepository.getGeocachesByUserIdFromFirestore(userId)
        } else {
            Log.d("GeocacheCapturedViewModel", "Internet is not available, fetching geocaches from local database for user ID: $userId")
            geocacheCapturedRepository.getGeocachesByUserId(userId)
        }
    }

    fun getAllGeocachesCaptured(): LiveData<List<GeocacheCaptured>> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            geocacheCapturedRepository.getAllGeocachesCapturedFromFirestore()
        } else {
            geocacheCapturedRepository.getAllGeocachesCaptured()
        }
    }

    fun getGeocacheCapturedById(geocacheCapturedId: Int): LiveData<GeocacheCaptured?> {
        return if (NetworkConnection.isInternetAvailable(context)) {
            geocacheCapturedRepository.getGeocacheCapturedByIdFromFirestore(geocacheCapturedId)
        } else {
            geocacheCapturedRepository.getGeocacheCapturedById(geocacheCapturedId)
        }
    }
}
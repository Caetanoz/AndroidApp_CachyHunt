package pt.ipp.estg.cachyhunt.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import pt.ipp.estg.cachyhunt.data.local.dao.GeocacheCapturedDao
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured

class GeocacheCapturedRepository(private val geocacheCapturedDao: GeocacheCapturedDao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertGeocacheCaptured(geocacheCaptured: GeocacheCaptured): Int {
        return geocacheCapturedDao.insertGeocacheCaptured(geocacheCaptured).toInt()
    }

    fun getGeocachesByUserId(userId: Int): LiveData<List<GeocacheCaptured>> {
        return geocacheCapturedDao.getGeocachesByUserId(userId)
    }

    fun getAllGeocachesCaptured(): LiveData<List<GeocacheCaptured>> {
        return geocacheCapturedDao.getAllGeocachesCaptured()
    }

    fun getGeocacheCapturedById(geocacheCapturedId: Int): LiveData<GeocacheCaptured?> {
        return geocacheCapturedDao.getGeocacheCapturedById(geocacheCapturedId)
    }

    fun saveGeocacheCapturedToFirestore(geocacheCaptured: GeocacheCaptured, onSuccess: () -> Unit, onError: () -> Unit) {
        firestore.collection("geocache_captured").document(geocacheCaptured.id.toString())
            .set(geocacheCaptured)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }

    fun getAllGeocachesCapturedFromFirestore(): LiveData<List<GeocacheCaptured>> {
        val liveData = MutableLiveData<List<GeocacheCaptured>>()
        firestore.collection("geocache_captured")
            .get()
            .addOnSuccessListener { result ->
                val geocaches = result.mapNotNull { it.toObject(GeocacheCaptured::class.java) }
                liveData.value = geocaches
            }
            .addOnFailureListener {
                liveData.value = emptyList()
            }
        return liveData
    }

    fun getGeocachesByUserIdFromFirestore(userId: Int): LiveData<List<GeocacheCaptured>> {
        val liveData = MutableLiveData<List<GeocacheCaptured>>()
        firestore.collection("geocache_captured")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val geocaches = result.mapNotNull { it.toObject(GeocacheCaptured::class.java) }
                liveData.value = geocaches
            }
            .addOnFailureListener {
                liveData.value = emptyList()
            }
        return liveData
    }

    fun getGeocacheCapturedByIdFromFirestore(geocacheCapturedId: Int): LiveData<GeocacheCaptured?> {
        val liveData = MutableLiveData<GeocacheCaptured?>()
        firestore.collection("geocache_captured").document(geocacheCapturedId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val geocacheCaptured = document.toObject(GeocacheCaptured::class.java)
                    liveData.value = geocacheCaptured
                } else {
                    liveData.value = null
                }
            }
            .addOnFailureListener {
                liveData.value = null
            }
        return liveData
    }
}
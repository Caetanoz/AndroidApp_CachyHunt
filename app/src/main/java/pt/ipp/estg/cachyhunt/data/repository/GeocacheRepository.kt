package pt.ipp.estg.cachyhunt.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import pt.ipp.estg.cachyhunt.data.local.dao.GeocacheDao
import pt.ipp.estg.cachyhunt.data.models.Geocache

class GeocacheRepository(private val geocacheDao: GeocacheDao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertGeocache(geocache: Geocache): Int {
        return geocacheDao.insertGeocache(geocache).toInt()
    }

    fun getGeocacheById(geocacheId: Int): LiveData<Geocache?> {
        return geocacheDao.getGeocacheById(geocacheId)
    }

    fun getAllGeocaches(): LiveData<List<Geocache>> {
        return geocacheDao.getAllGeocaches()
    }

    fun getGeocachesByUserId(userId: Int): LiveData<List<Geocache>> {
        return geocacheDao.getGeocachesByUserId(userId)
    }

    fun getAllActiveGeocaches(): LiveData<List<Geocache>> {
        return geocacheDao.getAllActiveGeocaches()
    }

    suspend fun updateGeocache(geocache: Geocache) {
        geocacheDao.updateGeocache(geocache)
    }

    fun insertGeocacheToFirestore(geocache: Geocache, onSuccess: () -> Unit, onError: () -> Unit) {
        firestore.collection("geocaches").document(geocache.id.toString())
            .set(geocache)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }

    fun getGeocacheFromFirestore(geocacheId: Int): LiveData<Geocache?> {
        val liveData = MutableLiveData<Geocache?>()
        firestore.collection("geocaches").document(geocacheId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val geocache = document.toObject(Geocache::class.java)
                    liveData.value = geocache
                } else {
                    liveData.value = null
                }
            }
            .addOnFailureListener {
                liveData.value = null
            }
        return liveData
    }

    fun getAllGeocachesFromFirestore(): LiveData<List<Geocache>> {
        val liveData = MutableLiveData<List<Geocache>>()
        firestore.collection("geocaches")
            .get()
            .addOnSuccessListener { result ->
                val geocaches = result.mapNotNull { it.toObject(Geocache::class.java) }
                liveData.value = geocaches
            }
            .addOnFailureListener {
                liveData.value = emptyList()
            }
        return liveData
    }

    fun updateGeocacheInFirestore(geocache: Geocache, onSuccess: () -> Unit, onError: () -> Unit) {
        firestore.collection("geocaches").document(geocache.id.toString())
            .update(
                "name", geocache.name,
                "description", geocache.description,
                "latitude", geocache.latitude,
                "longitude", geocache.longitude,
                "location", geocache.location,
                "points", geocache.points,
                "clues", geocache.clues,
                "createdByUserId", geocache.createdByUserId,
                "createdAt", geocache.createdAt,
                "dificuldade", geocache.dificuldade,
                "lastdiscovered", geocache.lastdiscovered,
                "rating", geocache.rating,
                "numberofratings", geocache.numberofratings,
                "status", geocache.status
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }
}

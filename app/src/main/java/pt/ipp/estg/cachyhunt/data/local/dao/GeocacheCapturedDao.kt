package pt.ipp.estg.cachyhunt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured

@Dao
interface GeocacheCapturedDao {

    @Insert
    suspend fun insertGeocacheCaptured(geocacheCaptured: GeocacheCaptured): Long

    @Query("SELECT * FROM geocache_captured WHERE userId = :userId")
    fun getGeocachesByUserId(userId: Int): LiveData<List<GeocacheCaptured>>

    @Query("SELECT * FROM geocache_captured")
    fun getAllGeocachesCaptured(): LiveData<List<GeocacheCaptured>>

    @Query("SELECT * FROM geocache_captured WHERE id = :geocacheCapturedId")
    fun getGeocacheCapturedById(geocacheCapturedId: Int): LiveData<GeocacheCaptured?>
}
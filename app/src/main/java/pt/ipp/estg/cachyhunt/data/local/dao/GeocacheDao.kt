package pt.ipp.estg.cachyhunt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pt.ipp.estg.cachyhunt.data.models.Geocache

@Dao
interface GeocacheDao {

    @Insert
    suspend fun insertGeocache(geocache: Geocache): Long

    @Query("SELECT * FROM geocache WHERE id = :geocacheId")
    fun getGeocacheById(geocacheId: Int): LiveData<Geocache?>

    @Query("SELECT * FROM geocache")
    fun getAllGeocaches(): LiveData<List<Geocache>>

    @Query("SELECT * FROM geocache WHERE createdByUserId = :userId")
    fun getGeocachesByUserId(userId: Int): LiveData<List<Geocache>>

    @Query("SELECT * FROM geocache WHERE status = 'ACTIVE'")
    fun getAllActiveGeocaches(): LiveData<List<Geocache>>

    @Update
    suspend fun updateGeocache(geocache: Geocache)
}
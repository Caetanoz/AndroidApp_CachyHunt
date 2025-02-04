package pt.ipp.estg.cachyhunt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pt.ipp.estg.cachyhunt.data.models.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): LiveData<User>

    @Query("SELECT * FROM user")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Int): LiveData<User>
}
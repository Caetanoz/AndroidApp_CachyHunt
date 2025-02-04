package pt.ipp.estg.cachyhunt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cachyhunt.data.models.Question

@Dao
interface QuestionDao {

    @Insert
    suspend fun insertQuestion(question: Question): Long

    @Query("SELECT * FROM Question WHERE id = :id")
    fun getQuestionById(id: Int): LiveData<Question?>

    @Query("SELECT * FROM Question")
    fun getAllQuestions(): LiveData<List<Question>>
}
package pt.ipp.estg.cachyhunt.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import pt.ipp.estg.cachyhunt.data.local.dao.QuestionDao
import pt.ipp.estg.cachyhunt.data.models.Question

class QuestionRepository(private val questionDao: QuestionDao) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertQuestion(question: Question): Int {
        return questionDao.insertQuestion(question).toInt()
    }

    fun getQuestionById(id: Int): LiveData<Question?> {
        return questionDao.getQuestionById(id)
    }

    fun getAllQuestions(): LiveData<List<Question>> {
        return questionDao.getAllQuestions()
    }

    fun saveQuestionToFirestore(question: Question, onSuccess: () -> Unit, onError: () -> Unit) {
        firestore.collection("questions").document(question.id.toString())
            .set(question)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }

    fun getAllQuestionsFromFirestore(): LiveData<List<Question>> {
        val liveData = MutableLiveData<List<Question>>()
        firestore.collection("questions")
            .get()
            .addOnSuccessListener { result ->
                val questions = result.mapNotNull { it.toObject(Question::class.java) }
                liveData.value = questions
            }
            .addOnFailureListener {
                liveData.value = emptyList()
            }
        return liveData
    }

    fun getQuestionByIdFromFirestore(id: Int): LiveData<Question?> {
        val liveData = MutableLiveData<Question?>()
        firestore.collection("questions").document(id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val question = document.toObject(Question::class.java)
                    liveData.value = question
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
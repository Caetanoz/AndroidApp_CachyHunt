package pt.ipp.estg.cachyhunt.data.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository

class SyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val tag = "SyncWorker"

    private val geocacheDao = AppDatabase.getDatabase(context).geocacheDao()
    private val geocacheCapturedDao = AppDatabase.getDatabase(context).geocacheCapturedDao()
    private val questionDao = AppDatabase.getDatabase(context).questionDao()
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val userRepository = UserRepository(userDao, context)
    private val geocacheRepository = GeocacheRepository(geocacheDao)
    private val geocacheCapturedRepository = GeocacheCapturedRepository(geocacheCapturedDao)
    private val questionRepository = QuestionRepository(questionDao)
    private val firestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        Log.d(tag, "SyncWorker started")
        return try {
            runBlocking {
                val localUsers = userDao.getAllUsers().value ?: emptyList()
                val localGeocaches = geocacheDao.getAllGeocaches().value ?: emptyList()
                val localGeocachesCaptured = geocacheCapturedDao.getAllGeocachesCaptured().value ?: emptyList()
                val localQuestions = questionDao.getAllQuestions().value ?: emptyList()

                val userResult = firestore.collection("users").get().await()
                val remoteUsers = userResult.mapNotNull { it.toObject(User::class.java) }

                val geocacheResult = firestore.collection("geocaches").get().await()
                val remoteGeocaches = geocacheResult.mapNotNull { it.toObject(Geocache::class.java) }

                val geocacheCapturedResult = firestore.collection("geocache_captured").get().await()
                val remoteGeocachesCaptured = geocacheCapturedResult.mapNotNull { it.toObject(
                    GeocacheCaptured::class.java) }

                val questionResult = firestore.collection("questions").get().await()
                val remoteQuestions = questionResult.mapNotNull { it.toObject(Question::class.java) }

                syncData(localUsers, remoteUsers, localGeocaches, remoteGeocaches, localGeocachesCaptured, remoteGeocachesCaptured, localQuestions, remoteQuestions)
                Log.d(tag, "SyncWorker completed successfully")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "SyncWorker failed", e)
            Result.failure()
        }
    }

    private fun syncData(
        localUsers: List<User>, remoteUsers: List<User>,
        localGeocaches: List<Geocache>, remoteGeocaches: List<Geocache>,
        localGeocachesCaptured: List<GeocacheCaptured>, remoteGeocachesCaptured: List<GeocacheCaptured>,
        localQuestions: List<Question>, remoteQuestions: List<Question>
    ) {
        val remoteUsersMap = remoteUsers.associateBy { it.email }
        localUsers.forEach { localUser ->
            val remoteUser = remoteUsersMap[localUser.email]
            if (remoteUser == null || localUser != remoteUser) {
                userRepository.saveUserToFirestore(localUser, {}, {})
            }
        }

        val remoteGeocachesMap = remoteGeocaches.associateBy { it.id }
        localGeocaches.forEach { localGeocache ->
            val remoteGeocache = remoteGeocachesMap[localGeocache.id]
            if (remoteGeocache == null || localGeocache != remoteGeocache) {
                geocacheRepository.insertGeocacheToFirestore(localGeocache, {}, {})
            }
        }

        val remoteGeocachesCapturedMap = remoteGeocachesCaptured.associateBy { it.id }
        localGeocachesCaptured.forEach { localGeocacheCaptured ->
            val remoteGeocacheCaptured = remoteGeocachesCapturedMap[localGeocacheCaptured.id]
            if (remoteGeocacheCaptured == null || localGeocacheCaptured != remoteGeocacheCaptured) {
                geocacheCapturedRepository.saveGeocacheCapturedToFirestore(localGeocacheCaptured, {}, {})
            }
        }

        val remoteQuestionsMap = remoteQuestions.associateBy { it.id }
        localQuestions.forEach { localQuestion ->
            val remoteQuestion = remoteQuestionsMap[localQuestion.id]
            if (remoteQuestion == null || localQuestion != remoteQuestion) {
                questionRepository.saveQuestionToFirestore(localQuestion, {}, {})
            }
        }
    }

    companion object {
        fun scheduleSyncWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncWorkRequest)
        }
    }
}
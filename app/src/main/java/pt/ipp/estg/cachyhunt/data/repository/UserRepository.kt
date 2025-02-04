package pt.ipp.estg.cachyhunt.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pt.ipp.estg.cachyhunt.data.local.dao.UserDao
import pt.ipp.estg.cachyhunt.data.models.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UserRepository(private val userDao: UserDao, private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertUser(user: User): Int {
        return userDao.insertUser(user).toInt()
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    fun getUserByEmail(email: String): LiveData<User> {
        return userDao.getUserByEmail(email)
    }

    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }

    fun getUserById(id: Int): LiveData<User> {
        return userDao.getUserById(id)
    }

    fun registerUser(user: User, onSuccess: (User) -> Unit, onError: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(user)
                } else {
                    onError()
                }
            }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError()
                }
            }
    }

    fun saveUserToFirestore(user: User, onSuccess: (User) -> Unit, onError: () -> Unit) {
        firestore.collection("users").document(user.email)
            .set(user)
            .addOnSuccessListener {
                onSuccess(user)
            }
            .addOnFailureListener {
                onError()
            }
    }

    fun getUserFromFirestore(email: String): LiveData<User> {
        val liveData = MutableLiveData<User>()
        firestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        liveData.value = user
                    } else {
                        Log.e("UserRepository", "User data is null for email: $email")
                    }
                } else {
                    Log.e("UserRepository", "No document exists for email: $email")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserRepository", "Error fetching user from Firestore", exception)
            }
        return liveData
    }

    fun getAllUsersFromFirestore(): LiveData<List<User>> {
        val liveData = MutableLiveData<List<User>>()
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.mapNotNull { it.toObject(User::class.java) }
                liveData.value = users
            }
            .addOnFailureListener {
                Log.e("UserRepository", "Error fetching users from Firestore")
            }
        return liveData
    }

    fun updateUserInFirestore(user: User, onSuccess: () -> Unit, onError: () -> Unit) {
        firestore.collection("users").document(user.email)
            .update(
                "id", user.id,
                "nickName", user.nickName,
                "email", user.email,
                "password", user.password,
                "photo", user.photo,
                "userLevel", user.userLevel,
                "currentPoints", user.currentPoints,
                "totalPoints", user.totalPoints,
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError()
            }
    }

    fun saveUserImage(user: User, imageData: ByteArray, onSuccess: (String) -> Unit, onError: () -> Unit) {
        // Delete existing image if it exists
        user.photo.let { existingImagePath ->
            if (existingImagePath.isNotEmpty()) {
                deleteImageFromInternalStorage(existingImagePath)
            }
        }

        // Save new image
        val imageName = "user_${user.id}_profile_image.jpg"
        val imagePath = saveImageToInternalStorage(context, imageName, imageData)
        if (imagePath != null) {
            user.photo = imagePath
            saveUserToFirestore(user, { onSuccess(imagePath) }, onError)
        } else {
            onError()
        }
    }

    fun getUserImage(user: User, onSuccess: (Bitmap?) -> Unit, onError: () -> Unit) {
        val imagePath = user.photo
        if (imagePath.isNotEmpty()) {
            val bitmap = getImageFromInternalStorage(imagePath)
            if (bitmap != null) {
                onSuccess(bitmap)
            } else {
                onError()
            }
        } else {
            onError()
        }
    }

    private fun saveImageToInternalStorage(context: Context, imageName: String, imageData: ByteArray): String? {
        val file = File(context.filesDir, imageName)
        return try {
            val fos = FileOutputStream(file)
            fos.write(imageData)
            fos.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(imagePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteImageFromInternalStorage(imagePath: String): Boolean {
        val file = File(imagePath)
        return file.exists() && file.delete()
    }
}
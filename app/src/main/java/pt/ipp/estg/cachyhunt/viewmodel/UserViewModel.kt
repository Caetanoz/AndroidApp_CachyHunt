package pt.ipp.estg.cachyhunt.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.data.utils.NetworkConnection

class UserViewModel(private val userRepository: UserRepository, private val context: Context) : ViewModel() {

    fun saveUserImage(user: User, imageData: ByteArray, onSuccess: (String) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            userRepository.saveUserImage(user, imageData, onSuccess, onError)
        }
    }

    fun getUserImage(user: User, onSuccess: (Bitmap?) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            userRepository.getUserImage(user, onSuccess, onError)
        }
    }

    fun getUser(email: String, onSuccess: (User) -> Unit, onError: () -> Unit) {
        if (NetworkConnection.isInternetAvailable(context)) {
            userRepository.getUserFromFirestore(email).observeForever { user ->
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError()
                }
            }
        } else {
            userRepository.getUserByEmail(email).observeForever { user ->
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError()
                }
            }
        }
    }

    fun getAllUsers(onSuccess: (List<User>) -> Unit, onError: () -> Unit) {
        if (NetworkConnection.isInternetAvailable(context)) {
            Log.d("UserViewModel", "Internet is available, fetching all users from Firestore")
            userRepository.getAllUsersFromFirestore().observeForever { users ->
                if (users != null) {
                    viewModelScope.launch {
                        users.forEach { userRepository.updateUser(it) }
                        Log.d("UserViewModel", "All users inserted into local database")
                        onSuccess(users)
                    }
                } else {
                    Log.e("UserViewModel", "Error fetching all users from Firestore")
                    onError()
                }
            }
        } else {
            Log.d("UserViewModel", "No internet, fetching all users from local database")
            userRepository.getAllUsers().observeForever { users ->
                if (users != null) {
                    Log.d("UserViewModel", "Users fetched from local database: $users")
                    onSuccess(users)
                } else {
                    Log.e("UserViewModel", "No users found in local database")
                    onError()
                }
            }
        }
    }

    fun updateUser(user: User, onSuccess: () -> Unit, onError: () -> Unit) {
        if (NetworkConnection.isInternetAvailable(context)) {
            userRepository.updateUserInFirestore(user, {
                viewModelScope.launch {
                    userRepository.updateUser(user)
                    onSuccess()
                }
            }, onError)
        } else {
            viewModelScope.launch {
                userRepository.updateUser(user)
                onSuccess()
            }
        }
    }

    fun registerAndSaveUser(user: User, onSuccess: (User) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                // Register user with Firebase
                userRepository.registerUser(user, {
                    // Insert user into local database
                    viewModelScope.launch {
                        try {
                            val generatedId = userRepository.insertUser(user)
                            val updatedUser = user.copy(id = generatedId)
                            // Save user to Firestore
                            userRepository.saveUserToFirestore(updatedUser, {
                                onSuccess(updatedUser)
                            }, {
                                Log.e("RegisterViewModel", "Error saving user to Firestore")
                                onError()
                            })
                        } catch (e: Exception) {
                            Log.e("RegisterViewModel", "Error inserting user into database", e)
                            onError()
                        }
                    }
                }, {
                    Log.e("RegisterViewModel", "Error registering user with Firebase")
                    onError()
                })
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error in registerAndSaveUser", e)
                onError()
            }
        }
    }

    fun loginUser(email: String, password: String, onLoginSuccess: () -> Unit, onLoginError: () -> Unit) {
        userRepository.loginUser(email, password, {
            onLoginSuccess()
        }, {
            onLoginError()
        })
    }
}
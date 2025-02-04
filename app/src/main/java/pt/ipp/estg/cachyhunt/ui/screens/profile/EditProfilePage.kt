package pt.ipp.estg.cachyhunt.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.data.utils.NetworkConnection
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory

@Composable
fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    userEmail: String?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context).userDao(), context), context)
    )

    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf(false) }

    LaunchedEffect(userEmail) {
        userEmail?.let {
            viewModel.getUser(
                email = it,
                onSuccess = { fetchedUser ->
                    user = fetchedUser
                    isLoading = false
                },
                onError = {
                    fetchError = true
                    isLoading = false
                }
            )
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NatureGreen)
        }
        return
    }

    if (fetchError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.error_loading_user),
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    user?.let {
        var nickName by remember { mutableStateOf(it.nickName) }
        var email by remember { mutableStateOf(it.email) }
        var password by remember { mutableStateOf(it.password ?: "") }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.edit_profile_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = NatureGreen
                    )
                )
            },
            containerColor = Beige
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .background(Beige),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                ProfileEditField(
                    label = stringResource(id = R.string.name_label),
                    value = nickName,
                    onValueChange = { nickName = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileEditField(
                    label = stringResource(id = R.string.email_label),
                    value = email,
                    onValueChange = { email = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfileEditField(
                    label = stringResource(id = R.string.password),
                    value = password,
                    onValueChange = { password = it },
                    keyboardType = KeyboardType.Password
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isSaving = true
                        val updatedUser = it.copy(
                            nickName = nickName,
                            email = email,
                            password = password
                        )
                        viewModel.updateUser(
                            user = updatedUser,
                            onSuccess = {
                                isSaving = false
                                onBack()
                            },
                            onError = {
                                isSaving = false
                                Log.e("EditProfilePage", "Error updating user")
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
                ) {
                    Text(text = stringResource(id = R.string.save_button), color = Color.White)
                }

                if (isSaving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = NatureGreen)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePagePreview() {
    EditProfilePage(userEmail = "1")
}

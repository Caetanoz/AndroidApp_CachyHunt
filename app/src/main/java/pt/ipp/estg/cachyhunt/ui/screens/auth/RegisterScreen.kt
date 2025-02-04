package pt.ipp.estg.cachyhunt.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.MediumGreen
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: (String) -> Unit, onNavigateToLogin: () -> Unit) {
    var nickName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context = LocalContext.current).userDao(), context), context)
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to",
                color = MediumGreen,
                fontSize = 35.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "CachyHunt",
                color = MediumGreen,
                fontSize = 50.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Image(
                painter = painterResource(id = R.drawable.cachyhunt),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(125.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickName,
                onValueChange = { nickName = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MediumGreen,
                    unfocusedBorderColor = MediumGreen,
                    cursorColor = Color.White,
                    containerColor = NatureGreen,
                    focusedLabelColor = NatureGreen,
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MediumGreen,
                    unfocusedBorderColor = MediumGreen,
                    cursorColor = Color.White,
                    containerColor = NatureGreen,
                    focusedLabelColor = NatureGreen,
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = Color.White)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MediumGreen,
                    unfocusedBorderColor = MediumGreen,
                    cursorColor = Color.White,
                    containerColor = NatureGreen,
                    focusedLabelColor = NatureGreen,
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password", tint = Color.White)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MediumGreen,
                    unfocusedBorderColor = MediumGreen,
                    cursorColor = Color.White,
                    containerColor = NatureGreen,
                    focusedLabelColor = NatureGreen,
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (registerError) {
                Text("Passwords do not match or fields are empty", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (password == confirmPassword && password.isNotEmpty() && email.isNotEmpty() && nickName.isNotEmpty()) {
                        val newUser = User(
                            id = 0,
                            nickName = nickName,
                            email = email.lowercase(),
                            password = password,
                            photo = "",
                            userLevel = "Beginner",
                            currentPoints = 0,
                            totalPoints = 0
                        )
                        viewModel.registerAndSaveUser(newUser, onSuccess = {
                            registerError = false
                            onRegisterSuccess(email.lowercase())
                        }, onError = {
                            registerError = true
                        })
                    } else {
                        registerError = true
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text("Let's get started", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text("Already have an account?", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onRegisterSuccess = {}, onNavigateToLogin = {})
}
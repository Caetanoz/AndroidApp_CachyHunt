package pt.ipp.estg.cachyhunt.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.MediumGreen
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var loginError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context = LocalContext.current).userDao(), context), context)
    )

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        currentUser?.email?.let { email ->
            onLoginSuccess(email)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome),
                color = NatureGreen,
                fontSize = 60.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.back),
                color = NatureGreen,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(id = R.drawable.cachyhunt),
                contentDescription = stringResource(R.string.logo_description),
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
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
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password), tint = Color.White)
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

            if (loginError) {
                Text(stringResource(R.string.invalid_email_password), color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    viewModel.loginUser(email.text, password.text, onLoginSuccess = {
                        onLoginSuccess(email.text/*User(0, "", email.text, password.text, "", "Beginner", 0, 0)*/)
                    }, onLoginError = {
                        loginError = true
                    })
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text(stringResource(R.string.login))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.wrapContentWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text(stringResource(R.string.new_user_register), color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(onLoginSuccess = {}, onRegisterClick = {})
}
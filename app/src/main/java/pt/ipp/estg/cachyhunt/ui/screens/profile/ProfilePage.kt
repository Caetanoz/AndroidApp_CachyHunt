package pt.ipp.estg.cachyhunt.ui.screens.profile

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.utils.LocaleUtils
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    userEmail: String?,
    visitGeocaches: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
                if (!snapshot.isEmpty) {
                    user = snapshot.documents.first().toObject(User::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val geocachecapturedViewModel: GeocacheCapturedViewModel = viewModel(
        factory = GeocacheCapturedViewModelFactory(
            GeocacheCapturedRepository(AppDatabase.getDatabase(context).geocacheCapturedDao()), context)
    )

    var geocachescaptured by remember { mutableStateOf<List<GeocacheCaptured>>(emptyList()) }

    LaunchedEffect(user?.id) {
        user?.id?.let { userId ->
            Log.d("MapScreen", "Fetching geocaches captured by user with ID: $userId")
            geocachecapturedViewModel.getGeocachesByUserId(userId).observeForever { fetchedGeocachesCaptured ->
                geocachescaptured = fetchedGeocachesCaptured
                Log.d("MapScreen", "Fetched geocaches captured: $geocachescaptured")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newLanguage = if (LocaleUtils.getSavedLanguage(context) == "en") "pt" else "en"
                            LocaleUtils.setLocale(context, newLanguage)
                            (context as? Activity)?.recreate()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = stringResource(id = R.string.change_language),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = NatureGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Beige)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ProfileImage(photoUrl = user?.photo)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetails(user = user)
            Spacer(modifier = Modifier.height(32.dp))
            CachesCounter(geocachescaptured.size, onClick = { visitGeocaches(user?.email ?: "") })
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Default.Email, stringResource(id = R.string.email), user?.email ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Default.Grade, "User Level", user?.userLevel ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Default.Star, "Number of points", user?.currentPoints?.toString() ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(Icons.Default.StarBorder, "Number of current points", user?.totalPoints?.toString() ?: "")
            Spacer(modifier = Modifier.height(32.dp))
            ProfileActions(onEditClick = { onEditClick(user?.email ?: "") }, onLogoutClick = onLogoutClick)
        }
    }
}

@Composable
fun ProfileImage(photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(color = Color.Gray, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNullOrEmpty()) {
            Text(text = "Photo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        } else {
            // Load photo from URL (e.g., Coil or Glide can be integrated here)
        }
    }
}

@Composable
fun ProfileDetails(user: User?) {
    user?.let {
        Text(text = "@${user.nickName}", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(id = R.string.profile_since), fontSize = 18.sp)
    } ?: Text(text = stringResource(id = R.string.loading), fontSize = 18.sp)
}

@Composable
fun CachesCounter(size: Int, onClick: () -> Unit) {
    Log.d("ProfilePage", "Number of geocaches captured caches counter: $size")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.caches_found),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NatureGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = NatureGreen,
                contentColor = Color.White
            ),
            shape = CircleShape,
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = size.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = NatureGreen
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 16.sp, color = Color.Gray)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}


fun logout() {
    val auth = FirebaseAuth.getInstance()
    auth.signOut()
}

@Composable
fun ProfileActions(onEditClick: () -> Unit, onLogoutClick: () -> Unit) {
    Button(
        onClick = onEditClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = NatureGreen,
            contentColor = Color.White
        )
    ) {
        Text(text = "Edit Profile")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            logout()
            onLogoutClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White
        )
    ) {
        Text(text = "Logout")
    }
}
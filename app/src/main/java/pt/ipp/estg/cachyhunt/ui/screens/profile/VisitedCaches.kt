package pt.ipp.estg.cachyhunt.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapturedGeocachesScreen(
    userEmail: String,
    onClick: (Int) -> Unit
) {
    val context = LocalContext.current

    val geoCacheCapturedViewModel: GeocacheCapturedViewModel = viewModel(
        factory = GeocacheCapturedViewModelFactory(
            GeocacheCapturedRepository(AppDatabase.getDatabase(context).geocacheCapturedDao()),
            context
        )
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            UserRepository(
                AppDatabase.getDatabase(context).userDao(),
                context
            ), context
        )
    )

    val geoCacheViewModel: GeocacheViewModel = viewModel(
        factory = GeocacheViewModelFactory(
            QuestionRepository(
                AppDatabase.getDatabase(context).questionDao()
            ), GeocacheRepository(AppDatabase.getDatabase(context).geocacheDao()), context
        )
    )

    var geocachesretrived by remember { mutableStateOf(emptyList<Geocache>()) }
    var filteredGeocaches by remember { mutableStateOf<List<Geocache>>(emptyList()) }

    LaunchedEffect(Unit) {
        geoCacheViewModel.getAllGeocaches().observeForever { fetchedGeocaches ->
            geocachesretrived = fetchedGeocaches
            Log.d("MapScreen", "Geocaches loaded successfully: $geocachesretrived")
        }
    }

    var user by remember { mutableStateOf<User?>(null) }
    var userId by remember { mutableStateOf(-1) }

    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            userViewModel.getUser(userEmail, onSuccess = { fetchedUser ->
                user = fetchedUser
                userId = fetchedUser.id
            }, onError = {
                // Handle error
                Log.e("ProfilePage", "Error fetching user")
            })
        }
    }

    val capturedGeocaches by geoCacheCapturedViewModel.getGeocachesByUserId(userId)
        .observeAsState(emptyList())

    LaunchedEffect(capturedGeocaches, geocachesretrived) {
        val capturedGeocacheIds = capturedGeocaches.map { it.geocacheId }.toSet()
        Log.d("MapScreen", "Captured geocache IDs: $capturedGeocacheIds")
        filteredGeocaches = geocachesretrived.filter { geocache ->
            val isNotCaptured = geocache.id in capturedGeocacheIds
            Log.d("MapScreen", "Geocache ID: ${geocache.id}, isNotCaptured: $isNotCaptured")
            isNotCaptured
        }
        Log.d("MapScreen", "Filtered geocaches: $filteredGeocaches")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Caches Found",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NatureGreen
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Beige)
        ) {
            items(filteredGeocaches) { geocache ->
                GeocacheItem(capturedGeocaches, geocache, onClick)
            }
        }
    }
}

@Composable
fun GeocacheItem(capturedGeocaches: List<GeocacheCaptured>, geocache: Geocache, onClick: (Int) -> Unit) {
    val geocacheCaptured = capturedGeocaches.filter { it.geocacheId == geocache.id }.firstOrNull()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB7E4C7))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = geocache.name,
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = stringResource(R.string.location_format2),
                    fontSize = 18.sp, // Larger font size for the label
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = geocache.location,
                    fontSize = 16.sp, // Regular font size for the value
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(2.dp))

            geocacheCaptured?.let {
                Column {
                    Text(
                        text = stringResource(R.string.last_discovery2),
                        fontSize = 18.sp, // Larger font size for the label
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = it.capturedAt,
                        fontSize = 16.sp, // Regular font size for the value
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onClick(geocache.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NatureGreen,
                    contentColor = Color(0xFFE8ECD7)
                )
            ) {
                Text(stringResource(R.string.view_details))
            }
        }
    }
}
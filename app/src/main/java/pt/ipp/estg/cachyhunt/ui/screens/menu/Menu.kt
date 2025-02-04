package pt.ipp.estg.cachyhunt.ui.screens.menu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.models.GeocacheStatus
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.data.retrofit.RetrofitHelper
import pt.ipp.estg.cachyhunt.data.retrofit.WeatherApi
import pt.ipp.estg.cachyhunt.data.retrofit.WeatherResponse
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import retrofit2.Call
import java.io.IOException
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userEmail: String, onClick: (Int) -> Unit, onAddClick: (String) -> Unit, onAboutUsClick: () -> Unit, onLeaderboardClick: () -> Unit) {

    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context).userDao(), context), context)
    )
    val GeocacheViewModel: GeocacheViewModel = viewModel(
        factory = GeocacheViewModelFactory(QuestionRepository(AppDatabase.getDatabase(context).questionDao()),GeocacheRepository(AppDatabase.getDatabase(context).geocacheDao()), context)
    )

    val geocachecapturedViewModel: GeocacheCapturedViewModel = viewModel(
        factory = GeocacheCapturedViewModelFactory(
            GeocacheCapturedRepository(AppDatabase.getDatabase(context).geocacheCapturedDao()), context)
    )

    var userId by remember { mutableStateOf(-1) }
    var userPoints by remember { mutableStateOf(0) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val locationPermissionGranted = remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
    }

    var geocachesretrived by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var geocachescaptured by remember { mutableStateOf<List<GeocacheCaptured>>(emptyList()) }
    var filteredGeocaches by remember { mutableStateOf<List<Geocache>>(emptyList()) }

    LaunchedEffect(userEmail) {
        Log.d("MainScreen", "User email: $userEmail")
        userEmail.let {
            Log.d("MainScreen", "Fetching user data for email: $it")
            viewModel.getUser(it, onSuccess = { user ->
                userPoints = user.currentPoints
                userId = user.id
                Log.d("MainScreen", "User points: ${user.currentPoints}")
                Log.d("MainScreen", "User ID: ${user.id}")
            }, onError = {
                Log.e("MainScreen", "Error fetching user data")
            })
        }
    }
//TO-DO: Fetch only geocaches that the user did not capture
    LaunchedEffect(Unit) {
        GeocacheViewModel.getAllGeocaches().observeForever { fetchedGeocaches ->
            geocachesretrived = fetchedGeocaches.filter { it.status == GeocacheStatus.ACTIVE && it.createdByUserId != userId }
            Log.d("MapScreen", "Geocaches loaded successfully: $geocachesretrived")
        }
    }

    LaunchedEffect(userId) {
        if (userId != -1) {
            userId.let { userId ->
                Log.d("MapScreen", "Fetching geocaches captured by user with ID: $userId")
                geocachecapturedViewModel.getGeocachesByUserId(userId)
                    .observeForever { fetchedGeocachesCaptured ->
                        geocachescaptured = fetchedGeocachesCaptured
                        Log.d("MapScreen", "Fetched geocaches captured: $geocachescaptured")
                    }
            }
        }
    }

    LaunchedEffect(geocachescaptured, geocachesretrived) {
        val capturedGeocacheIds = geocachescaptured.map { it.geocacheId }.toSet()
        Log.d("MapScreen", "Captured geocache IDs: $capturedGeocacheIds")
        filteredGeocaches = geocachesretrived.filter { geocache ->
            val isNotCaptured = geocache.id !in capturedGeocacheIds
            Log.d("MapScreen", "Geocache ID: ${geocache.id}, isNotCaptured: $isNotCaptured")
            isNotCaptured
        }
        Log.d("MapScreen", "Filtered geocaches: $filteredGeocaches")
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted.value = true
        }
    }

    LaunchedEffect(locationPermissionGranted.value) {
        if (locationPermissionGranted.value) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    userLocation = location
                    isLoading = false
                    Log.d("MainScreen", "User location: Latitude ${location.latitude}, Longitude ${location.longitude}")
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 1f, locationListener)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500L, 1f, locationListener)
            } catch (e: SecurityException) {
                Log.e("MainScreen", "Location permission not granted", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CachyHunt",
                        color = Color.White
                    )
                },
                actions = {
                    val canCreateGeocache = filteredGeocaches.isEmpty() || filteredGeocaches.none { it.createdByUserId == userId }

                    if (userPoints > 300 || canCreateGeocache) {
                        IconButton(onClick = { onAddClick(userEmail) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), tint = Color.White)
                        }
                    }
                    IconButton(onClick = onAboutUsClick) {
                        Icon(Icons.Default.Help, contentDescription = stringResource(R.string.about_us), tint = Color.White)
                    }
                    IconButton(onClick = onLeaderboardClick) {
                        Icon(Icons.Default.Leaderboard, contentDescription = stringResource(R.string.leaderboard), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                val distance = userLocation?.let {
                    calculateDistance(it.latitude, it.longitude, geocache.latitude, geocache.longitude)
                } ?: 0.0
                GeocacheItem(geocache, distance, onClick)
            }
        }
    }
}

@Composable
fun GeocacheItem(geocache: Geocache, distance: Double, onClick: (Int) -> Unit) {
    val backgroundColor = when {
        distance < 500 -> Color(0xFFB7E4C7)
        distance < 1000 -> Color(0xFFE2E1B9)
        else -> Color(0xFFE1846A)
    }
    var locationName by remember { mutableStateOf("")}
    var weatherDescription by remember { mutableStateOf<String?>(null) }
    val distanceText = if (distance == 0.0) {
        "Calculating..."
    } else if (distance < 1000) {
        "${distance.toInt()} metros"
    } else {
        "${"%.2f".format(distance / 1000)} km"
    }

    LaunchedEffect(Unit) {
        val retrofit = RetrofitHelper.getOpenWeatherMapInstance()
        val weatherApi = retrofit.create(WeatherApi::class.java)
        val call = weatherApi.getWeatherByCoordinates(geocache.latitude, geocache.longitude, "42c9b4fb80d2fd2a988777436a3ccd89")
        call.enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        weatherDescription = it.weather[0].description
                        Log.d("WeatherDescription", "Description: $weatherDescription")
                    }
                } else {
                    Log.e("Weather", "Failed to get weather data: ${response.code()} - ${response.message()}")
                    Log.e("Weather", "Response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("Weather", "Error: ${t.message}")
            }
        })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = geocache.name,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                locationName = getLocationDetails(
                    LocalContext.current,
                    LatLng(geocache.latitude, geocache.longitude)
                )
                Text(
                    text = stringResource(R.string.location_format, locationName),
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.distance_format, distanceText),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                weatherDescription?.let {
                    Text(
                        text = "Weather conditions: $it",
                        fontSize = 12.sp
                    )
                }
            }
        }
        Column {
            Button(
                onClick = {
                    onClick(geocache.id)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
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

private fun getLocationDetails(context: Context, location: LatLng): String {
    return try {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return if (addresses != null && addresses.isNotEmpty()) {
        val address = addresses[0]
        val concelho = address.subAdminArea ?: "Unknown concelho"
        val distrito = address.adminArea ?: "Unknown distrito"
        val pais = address.countryName ?: "Unknown país"
        "$concelho, $distrito, $pais"
    } else {
        "Unknown location"
    }
} catch (e: IOException) {
        Log.e("MenuKt", "Geocoder service not available", e)
        "Service not available"
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371e3 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(
        userEmail = "1",
        onClick = {},
        onAddClick = {},
        onAboutUsClick = {},
        onLeaderboardClick = {}
    )
}
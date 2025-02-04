package pt.ipp.estg.cachyhunt.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import pt.ipp.estg.cachyhunt.R
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheCaptured
import pt.ipp.estg.cachyhunt.data.models.GeocacheStatus
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.models.User
import pt.ipp.estg.cachyhunt.data.repository.GeocacheCapturedRepository
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheCapturedViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.QuestionViewModel
import pt.ipp.estg.cachyhunt.viewmodel.QuestionViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import java.util.Date

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(userEmail: String) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context).userDao(), context), context)
    )
    val geocacheViewModel: GeocacheViewModel = viewModel(
        factory = GeocacheViewModelFactory(
            QuestionRepository(AppDatabase.getDatabase(context).questionDao()),
            GeocacheRepository(AppDatabase.getDatabase(context).geocacheDao()), context)
    )

    val geocachecapturedViewModel: GeocacheCapturedViewModel = viewModel(
        factory = GeocacheCapturedViewModelFactory(
            GeocacheCapturedRepository(AppDatabase.getDatabase(context).geocacheCapturedDao()), context)
    )

    val questionViewModel: QuestionViewModel = viewModel(
        factory = QuestionViewModelFactory(QuestionRepository(AppDatabase.getDatabase(context).questionDao()), context)
    )

    var user by remember { mutableStateOf<User?>(null) }
    var geocaches by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var geocachescaptured by remember { mutableStateOf<List<GeocacheCaptured>>(emptyList()) }
    var filteredGeocaches by remember { mutableStateOf<List<Geocache>>(emptyList()) }
    var question by remember { mutableStateOf<Question?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedGeocache by remember { mutableStateOf<Geocache?>(null) }
    var markerSymbols by remember { mutableStateOf<List<Symbol>>(emptyList()) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    val markedGeocacheIds = remember { mutableStateOf<MutableSet<Int>>(mutableSetOf()) }
    val markerMap = remember { mutableStateMapOf<Int, Symbol>() }

    LaunchedEffect(userEmail) {
        userViewModel.getUser(userEmail, onSuccess = { fetchedUser ->
            user = fetchedUser
        }, onError = {
            // Handle error
        })
    }

    LaunchedEffect(Unit) {
        geocacheViewModel.getAllGeocaches().observeForever { fetchedGeocaches ->
            geocaches = fetchedGeocaches.filter { geocache ->
                geocache.status == GeocacheStatus.ACTIVE && geocache.createdByUserId != user?.id
            }
        }
    }

    LaunchedEffect(user?.id) {
        user?.id?.let { userId ->
            Log.d("MapScreen", "Fetching geocaches captured by user with ID: $userId")
            geocachecapturedViewModel.getGeocachesByUserId(userId).observeForever { fetchedGeocachesCaptured ->
                geocachescaptured = fetchedGeocachesCaptured
                Log.d("MapScreen", "Fetched geocaches captured: $geocachescaptured")
            }
        }
    }

    LaunchedEffect(geocachescaptured, geocaches) {
        val capturedGeocacheIds = geocachescaptured.map { it.geocacheId }.toSet()
        Log.d("MapScreen", "Captured geocache IDs: $capturedGeocacheIds")
        filteredGeocaches = geocaches.filter { geocache ->
            val isNotCaptured = geocache.id !in capturedGeocacheIds
            Log.d("MapScreen", "Geocache ID: ${geocache.id}, isNotCaptured: $isNotCaptured")
            isNotCaptured
        }
        Log.d("MapScreen", "Filtered geocaches: $filteredGeocaches")

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    Mapbox.getInstance(context, context.getString(R.string.mapbox_access_token))

    val locationPermissionGranted = remember { mutableStateOf(false) }
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted.value = isGranted
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
            mapboxMap?.let { map ->
                map.getStyle { style ->
                    enableLocationComponent(context, style, map)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {
            MapView(it).apply {
                onCreate(null)
                getMapAsync { map ->
                    mapboxMap = map
                    map.setStyle(Style.OUTDOORS) { style ->
                        style.addImage("custom-marker", context.resources.getDrawable(R.drawable.mapbox_marker_icon_default, null))
                        if (locationPermissionGranted.value) {
                            enableLocationComponent(context, style, map)
                        }
                        symbolManager = SymbolManager(this, map, style).apply {
                            iconAllowOverlap = true
                            iconIgnorePlacement = true
                        }

                        map.addOnCameraIdleListener {
                            userLocation = map.locationComponent.lastKnownLocation?.let {
                                LatLng(it.latitude, it.longitude)
                            }

                            userLocation?.let { userLoc ->
                                val newMarkers = filteredGeocaches.map { geocache ->
                                    val distance = calculateDistance(
                                        userLoc.latitude, userLoc.longitude,
                                        geocache.latitude, geocache.longitude
                                    )

                                    if (distance <= 8000 && !markedGeocacheIds.value.contains(geocache.id)) {
                                        Log.d("MapScreen", "Creating marker for geocache: $geocache at distance: $distance")

                                        // Add to the list of marked geocache IDs to prevent re-adding the same marker
                                        markedGeocacheIds.value.add(geocache.id)

                                        // Create a new marker for the geocache
                                        val symbol = symbolManager!!.create(
                                            SymbolOptions()
                                                .withLatLng(LatLng(geocache.latitude, geocache.longitude))
                                                .withIconImage("custom-marker") // Ensure the image exists in your assets
                                                .withIconSize(1f)
                                        )

                                        Log.d("MapScreen", "Marker created at coordinates: ${geocache.latitude}, ${geocache.longitude}")

                                        // Set the click listener for the marker
                                        symbolManager!!.addClickListener { clickedSymbol ->
                                            if (clickedSymbol == symbol) {
                                                Log.d("MapScreen", "Geocache selected: $geocache")
                                                selectedGeocache = geocache
                                                questionViewModel.getQuestionById(geocache.questionId).observeForever { fetchedQuestion ->
                                                    if (fetchedQuestion != null) {
                                                        Log.d("MapScreen", "Question fetched: $fetchedQuestion")
                                                        question = fetchedQuestion
                                                        showDialog = true
                                                    } else {
                                                        Log.e("MapScreen", "Error fetching question for geocache: $geocache")
                                                        // Handle error
                                                    }
                                                }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        markerMap[geocache.id] = symbol
                                        Log.d("MapScreen", "Marker added to markerMap with geocache id: ${markerMap[geocache.id]}")
                                        Log.d("MapScreen", "MarkerMap updated: ${markerMap.size}")
                                        symbol // Return the symbol to be added to the markers list
                                    } else {
                                        Log.d("MapScreen", "Geocache: $geocache is too far at distance: $distance")
                                        null // Return null if the geocache is too far
                                    }
                                }.filterNotNull() // Remove null values

// Update the list of marker symbols
                                markerSymbols = newMarkers
                            }
                        }
                    }
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(41.366831552845525, -8.19477176031321))
                        .zoom(13.0)
                        .build()
                }
            }
        }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .align(Alignment.TopCenter)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                },
                placeholder = { Text(stringResource(R.string.search_location)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(3.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.LightGray
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        searchLocation(searchQuery, context, mapboxMap)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_icon),
                            tint = androidx.compose.ui.graphics.Color.Gray
                        )
                    }

                }
            )
        }
    }
    var feedbackState by remember { mutableStateOf<Boolean?>(null) }

    if (showDialog && question != null) {
        GeocacheQuestionDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            question = question!!,
            onAnswerSubmit = { isCorrect ->
                feedbackState = isCorrect
                showDialog = false
                if (isCorrect) {
                    Log.d("MapScreen", "isCorrect is true, attempting to remove marker for geocache with coordinates [${selectedGeocache?.longitude}, ${selectedGeocache?.latitude}]")

                    if (markerMap.isEmpty()) {
                        Log.d("MapScreen", "Tamanho do homem: ${markerMap.size}")
                    } else {
                        markerMap.forEach { (key, value) ->
                            Log.d("MapScreen", "markerMap key: $key, value: $value")
                        }
                    }

                    selectedGeocache?.let { geocache ->
                        markerMap[geocache.id]?.let { symbol ->
                            val coordinates = symbol.geometry.coordinates()
                            val isMatch = coordinates[0] == geocache.longitude && coordinates[1] == geocache.latitude
                            if (isMatch) {
                                Log.d("MapScreen", "Marker coordinates: ${coordinates[0]}, ${coordinates[1]}")
                                Log.d("MapScreen", "Geocache coordinates: ${geocache.longitude}, ${geocache.latitude}")
                                Log.d("MapScreen", "Marker found for geocache with coordinates [${geocache.longitude}, ${geocache.latitude}]")
                                symbolManager?.delete(symbol)
                                markerMap.remove(geocache.id)
                                markerSymbols = markerSymbols.filter { it != symbol }
                                Log.d("MapScreen", "Marker for geocache with coordinates [${geocache.longitude}, ${geocache.latitude}] removed")
                            } else {
                                Log.d("MapScreen", "Marker coordinates do not match geocache coordinates")
                            }
                        } ?: Log.d("MapScreen", "Marker for geocache with id ${geocache.id} not found")
                    }

                    // Show rating dialog after correct answer
                    showRatingDialog = true
                }
            }
        )
    }

    if (showRatingDialog) {
        GeocacheRatingDialog(
            showDialog = showRatingDialog,
            onDismiss = { showRatingDialog = false },
            onRatingSubmit = { rating ->
                selectedGeocache?.let { geocache ->
                    val newNumberOfRatings = geocache.numberofratings + 1
                    val newRating = ((geocache.rating * geocache.numberofratings) + rating) / newNumberOfRatings
                    val newStatus = if (newNumberOfRatings > 10 && newRating < 2.0) GeocacheStatus.INACTIVE else geocache.status
                    val currentDate = Date()

                    geocacheViewModel.updateGeocache(geocache.copy(
                        rating = newRating,
                        numberofratings = newNumberOfRatings,
                        status = newStatus,
                        lastdiscovered = currentDate
                    ), onSuccess = {
                        Log.d("MapScreen", "Geocache rating updated: $newRating")
                    }, onError = {
                        Log.e("MapScreen", "Error updating geocache rating")
                    })
                    val geocacheCaptured = user?.let {
                        GeocacheCaptured(
                            id = 0,
                            geocacheId = geocache.id,
                            userId = it.id,
                            capturedAt = Date().toString()
                        )
                    }

                    geocacheCaptured?.let {
                        geocachecapturedViewModel.insertGeocacheCaptured(it, onSuccess = {
                            Log.d("MapScreen", "Geocache captured successfully")
                        }, onError = {
                            Log.e("MapScreen", "Error capturing geocache")
                        })
                    }
                }
                showRatingDialog = false
            }
        )
    }

    feedbackState?.let { isCorrect ->
        ShowFeedback(isCorrect)
        feedbackState = null
    }
}


@Composable
fun ShowFeedback(isCorrect: Boolean) {
    val context = LocalContext.current
    if (isCorrect) {
        Toast.makeText(context, stringResource(R.string.correct_answer), Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, stringResource(R.string.incorrect_answer), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun GeocacheQuestionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    question: Question,
    onAnswerSubmit: (Boolean) -> Unit,
)  {
    var answer by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = question.question) },
            text = {
                Column {
                    TextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Your Answer") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val isCorrect = answer == question.correctAnswer
                    onAnswerSubmit(isCorrect)
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GeocacheRatingDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRatingSubmit: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(0) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rate the Geocache")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { rating = i }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onRatingSubmit(rating)
                    onDismiss()
                }) {
                    Text("Submit Rating")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}


private fun enableLocationComponent(context: Context, style: Style, mapboxMap: MapboxMap) {
    val locationComponent = mapboxMap.locationComponent
    locationComponent.activateLocationComponent(
        LocationComponentActivationOptions.builder(context, style).build()
    )

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    locationComponent.isLocationComponentEnabled = true
    locationComponent.cameraMode = CameraMode.TRACKING
    locationComponent.renderMode = RenderMode.COMPASS
}

private fun searchLocation(query: String, context: Context, mapboxMap: MapboxMap?) {
    if (query.isEmpty()) return

    val accessToken = context.getString(R.string.mapbox_access_token)

    val mapboxGeocoding = com.mapbox.api.geocoding.v5.MapboxGeocoding.builder()
        .accessToken(accessToken)
        .query(query)
        .build()

    mapboxGeocoding.enqueueCall(object : retrofit2.Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
        override fun onResponse(
            call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
            response: retrofit2.Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
        ) {
            val features = response.body()?.features()
            if (!features.isNullOrEmpty()) {
                val feature = features[0]
                val latLng = LatLng(feature.center()!!.latitude(), feature.center()!!.longitude())

                mapboxMap?.cameraPosition = CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15.0)
                    .build()
            } else {
                println("Nenhum resultado encontrado para a query: $query")
            }
        }

        override fun onFailure(
            call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
            t: Throwable
        ) {
            t.printStackTrace()
        }
    })
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
fun MapScreenPreview() {
    MapScreen(userEmail = "1")
}
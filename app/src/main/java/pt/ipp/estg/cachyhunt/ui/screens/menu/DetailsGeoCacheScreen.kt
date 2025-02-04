package pt.ipp.estg.cachyhunt.ui.screens.menu

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.util.Log
import pt.ipp.estg.cachyhunt.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import pt.ipp.estg.cachyhunt.data.models.Geocache
import java.util.Date
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import okhttp3.ResponseBody
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.GeocacheStatus
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.data.retrofit.MapConfig
import pt.ipp.estg.cachyhunt.data.retrofit.PlaceDetailsApi
import pt.ipp.estg.cachyhunt.data.retrofit.RetrofitHelper
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.MediumGreen
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.random.Random
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsGeocacheScreen(geocacheId: Int) {

    val detailsGeocacheViewModel: GeocacheViewModel = viewModel(
        factory = GeocacheViewModelFactory(
            QuestionRepository(AppDatabase.getDatabase(context = LocalContext.current).questionDao()),GeocacheRepository(AppDatabase.getDatabase(context = LocalContext.current).geocacheDao()), LocalContext.current)
        )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context = LocalContext.current).userDao(), context = LocalContext.current), context = LocalContext.current)
    )

    var nickname by remember { mutableStateOf("") }

    var geocache by remember { mutableStateOf<Geocache?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isImageLoaded by remember { mutableStateOf(false) }
    var currentClueIndex by remember { mutableStateOf(0) }
    var revealClue by remember { mutableStateOf(false) }
    var locationName by remember { mutableStateOf("")}

    LaunchedEffect(geocacheId) {
        detailsGeocacheViewModel.getGeocacheById(geocacheId).observeForever { fetchedGeocache ->
            if (fetchedGeocache != null) {
                geocache = fetchedGeocache
            } else {
                Log.e("DetailsGeocacheScreen", "Error fetching geocache")
            }
            isLoading = false
        }
    }

    LaunchedEffect(geocache) {
        val api = RetrofitHelper.getGeoapifyInstance().create(PlaceDetailsApi::class.java)
        val randomOffset = { Random.nextDouble(-0.002, 0.002) }
        val offsetLatitude = geocache?.latitude?.plus(randomOffset())
        val offsetLongitude = geocache?.longitude?.plus(randomOffset())

        val call = api.getStaticMap(
            center = "lonlat:$offsetLongitude,$offsetLatitude",
            apiKey = MapConfig.API_KEY
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val inputStream = responseBody.byteStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageBitmap = bitmap.asImageBitmap()
                        isImageLoaded = true
                        isLoading = false
                        Log.d("RemoteImage", "Image loaded successfully")
                        Log.d("RemoteImage", "Image size: ${response}")
                    }
                } else {
                    Log.e("RemoteImage", "Failed to get image: ${response.errorBody()?.string()}")
                    isLoading = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("PlaceDetailsApi", "Error fetching static map", t)
                isLoading = false
            }
        })
    }

    LaunchedEffect(geocache) {
        geocache?.createdByUserId?.let { createdById ->
            userViewModel.getAllUsers(onSuccess = { users ->
                users.find { it.id == createdById }?.let { user ->
                    nickname = user.nickName
                }
            }, onError = {
                Log.e("DetailsGeocacheScreen", "Error fetching users")
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Beige)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

            geocache?.let { geocache ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = geocache.name,
                        color = NatureGreen,
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    locationName = getLocationDetails(LocalContext.current, LatLng(geocache.latitude, geocache.longitude))
                    Text(
                        text = stringResource(R.string.location, locationName),
                        color = NatureGreen,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.created_by, nickname),
                        color = NatureGreen,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val lastDiscoveryPrefix = stringResource(R.string.last_discovery, "")

                    OutlinedTextField(
                        value = geocache.lastdiscovered?.let { stringResource(R.string.last_discovery, it.toString()) } ?: stringResource(R.string.not_found_yet),
                        onValueChange = { lastDiscovered ->
                            geocache.lastdiscovered =
                                lastDiscovered.removePrefix(lastDiscoveryPrefix).toLongOrNull()
                                    ?.let { Date(it) }
                        },
                        readOnly = true,
                        label = { Text(stringResource(R.string.activity), color = NatureGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ritmocardiaco),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NatureGreen,
                            unfocusedLabelColor = NatureGreen,
                            focusedBorderColor = NatureGreen,
                            unfocusedBorderColor = NatureGreen,
                            cursorColor = NatureGreen,
                            containerColor = MediumGreen
                        )
                    )

                    OutlinedTextField(
                        value = geocache.dificuldade.toString(),
                        onValueChange = { newDifficulty ->
                            geocache.dificuldade =
                                newDifficulty.toIntOrNull() ?: geocache.dificuldade
                        },
                        readOnly = true,
                        label = { Text(stringResource(R.string.difficulty), color = NatureGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NatureGreen,
                            unfocusedLabelColor = NatureGreen,
                            focusedBorderColor = NatureGreen,
                            unfocusedBorderColor = NatureGreen,
                            cursorColor = NatureGreen,
                            containerColor = MediumGreen
                        )
                    )

                    OutlinedTextField(
                        value = geocache.description,
                        onValueChange = { newDescription ->
                            geocache.description = newDescription
                        },
                        readOnly = true,
                        label = { Text(stringResource(R.string.description), color = NatureGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NatureGreen,
                            unfocusedLabelColor = NatureGreen,
                            focusedBorderColor = NatureGreen,
                            unfocusedBorderColor = NatureGreen,
                            cursorColor = NatureGreen,
                            containerColor = MediumGreen
                        )
                    )

                    OutlinedTextField(
                        value = geocache.points.toString(),
                        onValueChange = { newPoints ->
                            geocache.points = newPoints.toIntOrNull() ?: geocache.points
                        },
                        readOnly = true,
                        label = { Text(stringResource(R.string.points), color = NatureGreen) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = NatureGreen,
                            unfocusedLabelColor = NatureGreen,
                            focusedBorderColor = NatureGreen,
                            unfocusedBorderColor = NatureGreen,
                            cursorColor = NatureGreen,
                            containerColor = MediumGreen
                        )
                    )

                    val userDistanceFromGeocache =
                        300 // Calculate the user's distance from the geocache

                    currentClueIndex = when {
                        userDistanceFromGeocache < 100 -> geocache?.clues?.size ?: 0
                        userDistanceFromGeocache < 500 -> 3
                        userDistanceFromGeocache < 1000 -> 2
                        userDistanceFromGeocache < 3000 -> 1
                        else -> 0
                    }

                    Column {
                        for (index in 0 until currentClueIndex.coerceAtMost(geocache?.clues?.size ?: 0)) {
                            OutlinedTextField(
                                value = geocache?.clues?.get(index) ?: "",
                                onValueChange = { newClue: String ->
                                    geocache?.clues = geocache?.clues?.toMutableList()?.apply { set(index, newClue) } ?: listOf()
                                },
                                readOnly = true,
                                label = { Text(stringResource(R.string.clue_with_index, index + 1), color = NatureGreen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.White
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NatureGreen,
                                    unfocusedLabelColor = NatureGreen,
                                    focusedBorderColor = NatureGreen,
                                    unfocusedBorderColor = NatureGreen,
                                    cursorColor = NatureGreen,
                                    containerColor = MediumGreen
                                )
                            )
                        }
                    }
                }
            }

    }
}

private fun getLocationDetails(context: Context, location: LatLng): String {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClueTextField(index: Int, clue: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = clue,
        onValueChange = onValueChange,
        readOnly = true,
        label = { Text(stringResource(R.string.clue_with_index, index + 1), color = NatureGreen) },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewGeocacheDetailsScreen() {
    val sampleGeocache = Geocache(
        id = 1,
        name = "Sample Geocache",
        description = "This is a sample geocache description.",
        latitude = 41.36680104151704,
        longitude = -8.19507966568623,
        location = "New York",
        points = 100,
        clues = listOf("Clue 1", "Clue 2", "Clue 3", "Clue 4"),
        createdByUserId = 1,
        dificuldade = 3,
        lastdiscovered = Date(),
        questionId = 0,
        createdAt = Date(),
        numberofratings = 0,
        rating = 0.0,
        status = GeocacheStatus.ACTIVE
    )
    DetailsGeocacheScreen(geocacheId = 1)
}
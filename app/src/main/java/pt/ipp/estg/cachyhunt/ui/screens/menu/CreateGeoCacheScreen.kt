package pt.ipp.estg.cachyhunt.ui.screens.menu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import pt.ipp.estg.cachyhunt.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import pt.ipp.estg.cachyhunt.data.local.AppDatabase
import pt.ipp.estg.cachyhunt.data.models.Geocache
import pt.ipp.estg.cachyhunt.data.models.GeocacheStatus
import pt.ipp.estg.cachyhunt.data.models.Question
import pt.ipp.estg.cachyhunt.data.repository.GeocacheRepository
import pt.ipp.estg.cachyhunt.data.repository.QuestionRepository
import pt.ipp.estg.cachyhunt.data.repository.UserRepository
import pt.ipp.estg.cachyhunt.ui.theme.Beige
import pt.ipp.estg.cachyhunt.ui.theme.NatureGreen
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModel
import pt.ipp.estg.cachyhunt.viewmodel.GeocacheViewModelFactory
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModel
import pt.ipp.estg.cachyhunt.viewmodel.UserViewModelFactory
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGeocacheScreen(userEmail : String, onSuccess: (String) -> Unit) {

    val context = LocalContext.current
    val createGeocacheViewModel: GeocacheViewModel = viewModel(
        factory = GeocacheViewModelFactory(QuestionRepository(AppDatabase.getDatabase(context).questionDao()) , GeocacheRepository(AppDatabase.getDatabase(context).geocacheDao()), context)
    )
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(UserRepository(AppDatabase.getDatabase(context).userDao(), context), context)
    )
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val points = remember { mutableIntStateOf(0) }
    val clues = remember { mutableStateListOf<String>() }
    val questionText = remember { mutableStateOf("") }
    val correctAnswer = remember { mutableStateOf("") }
    val difficulty = remember { mutableIntStateOf(0) }
    var showMapDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("")}
    var validationError by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf(0) }

    LaunchedEffect(userEmail) {
        userEmail.let {
            Log.d("CreateGeocacheScreen", "Fetching user data for email: $it")
            viewModel.getUser(it, onSuccess = { user ->
                userId = user.id
            }, onError = {
                Log.e("CreateGeocacheScreen", "Error fetching user data")
            })
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.create_geocache_title),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = NatureGreen,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            AccordionItem(stringResource(id = R.string.name_label), name.value) { newValue -> name.value = newValue }
            AccordionItem(stringResource(id = R.string.description_label), description.value) { newValue ->
                description.value = newValue
            }
            AccordionItem(stringResource(id = R.string.location_label), questionText.value) { newValue ->
                questionText.value = newValue
            }
            AccordionItem(stringResource(id = R.string.correct_answer_label), correctAnswer.value) { newValue ->
                correctAnswer.value = newValue
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(id = R.string.difficulty_label))
                        NumberInputField(
                            value = difficulty.intValue,
                            onValueChange = { newValue ->
                                difficulty.intValue = newValue.coerceIn(0, 5)
                            }
                        )
                        Text(stringResource(id = R.string.difficulty_max), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(id = R.string.points_label))
                        NumberInputField(
                            value = points.intValue,
                            onValueChange = { newValue ->
                                points.intValue = newValue.coerceIn(0, 100)
                            }
                        )
                        Text(stringResource(id = R.string.points_max), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            CluesAccordionItem(clues)

            Button(
                onClick = { showMapDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text(stringResource(id = R.string.select_location))
            }

            if (showMapDialog) {
                MapDialog(
                    onDismissRequest = { showMapDialog = false },
                    onLocationSelected = { location ->
                        selectedLocation = location
                        locationName = getLocationName(context, location)
                        Log.d("CreateGeocacheScreen", "Selected location name: $locationName")
                        showMapDialog = false
                    }
                )
            }

            Button(
                onClick = {
                    if (name.value.isEmpty() || description.value.isEmpty() || questionText.value.isEmpty() || correctAnswer.value.isEmpty()) {
                        validationError = "All fields must be filled"
                    }else if(selectedLocation == null){
                        validationError = "Location must be selected."
                    } else {
                        validationError = ""
                        // Create the Question first
                        val question = Question(
                            question = questionText.value,
                            correctAnswer = correctAnswer.value
                        )
                        val geocache = Geocache(
                            id = 0,
                            name = name.value,
                            description = description.value,
                            latitude = selectedLocation?.latitude ?: 0.0,
                            longitude = selectedLocation?.longitude ?: 0.0,
                            location = locationName,
                            points = points.intValue,
                            clues = clues.toList(),
                            createdByUserId = userId, // Set the appropriate user ID
                            createdAt = Date(),
                            questionId = question.id,
                            dificuldade = difficulty.intValue,
                            lastdiscovered = null,
                            rating = 0.0,
                            numberofratings = 0,
                            status = GeocacheStatus.ACTIVE
                        )
                        createGeocacheViewModel.insertQuestionAndGeocache(
                            question = question,
                            geocache = geocache,
                            onSuccess = {
                                onSuccess("Geocache created successfully")
                            },
                            onError = {
                                Log.e("CreateGeocacheScreen", "Error creating geocache")
                            }
                        )
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text(stringResource(id = R.string.create_geocache_button))
            }
            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputField(value: Int, onValueChange: (Int) -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NatureGreen)
            .padding(8.dp)
            .width(IntrinsicSize.Min),
    ) {
        IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0)) }) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = stringResource(R.string.decrease), tint = Color.White)
        }
        Text(
            text = value.toString(),
            color = Color.White,
            modifier = Modifier.width(IntrinsicSize.Min)
        )
        IconButton(onClick = { onValueChange((value + 1).coerceAtMost(100)) }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.increase), tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CluesAccordionItem(clues: MutableList<String>) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = stringResource(R.string.clues), modifier = Modifier.weight(1f))
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                clues.forEachIndexed { index, clue ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = clue,
                            onValueChange = { newValue -> clues[index] = newValue },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.clue, index + 1)) }
                        )
                        IconButton(
                            onClick = { clues.removeAt(index) },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.remove_clue))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (clues.size < 5) {
                    Button(
                        onClick = { clues.add("") },
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.add_clue))
                    }
                }
            }
        }
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccordionItem(label: String, value: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = label, modifier = Modifier.weight(1f))
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = NatureGreen,
                        cursorColor = NatureGreen
                    )
                )
            }
        }
        Divider(color = NatureGreen, thickness = 1.dp, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp))
    }
}

@Composable
fun MapScreen(onLocationSelected: (LatLng) -> Unit) {
    val context = LocalContext.current
    Mapbox.getInstance(context, context.getString(R.string.mapbox_access_token))

    val locationPermissionGranted = remember { mutableStateOf(false) }
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var symbolManager by remember { mutableStateOf<SymbolManager?>(null) }
    var symbol by remember { mutableStateOf<Symbol?>(null) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            MapView(context).apply {
                onCreate(null)
                getMapAsync { map ->
                    mapboxMap = map
                    map.setStyle(Style.OUTDOORS) { style ->
                        enableLocationComponent(context, style, map)
                        symbolManager = SymbolManager(this, map, style) // 'this' refers to the MapView instance
                        style.addImage(
                            "marker-icon-id",
                            BitmapFactory.decodeResource(context.resources, R.drawable.mapbox_marker_icon_default)
                        )
                    }
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(41.36680104151704, -8.19507966568623))
                        .zoom(13.0)
                        .build()

                    map.addOnMapClickListener { point ->
                        selectedLocation = point
                        symbol?.let { symbolManager?.delete(it) }
                        symbol = symbolManager?.create(
                            SymbolOptions()
                                .withLatLng(point)
                                .withIconImage("marker-icon-id")
                        )
                        true
                    }
                }
            }
        }, modifier = Modifier.fillMaxSize())

        selectedLocation?.let {
            Button(
                onClick = { onLocationSelected(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NatureGreen)
            ) {
                Text(stringResource(R.string.confirm_location))
            }
        }
    }
}

@Composable
fun MapDialog(onDismissRequest: () -> Unit, onLocationSelected: (LatLng) -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .size(400.dp) // Define o tamanho do quadrado
                .background(Color.White)
        ) {
            MapScreen(onLocationSelected = onLocationSelected)
        }
    }
}

private fun getLocationName(context: Context, location: LatLng): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return if (addresses != null && addresses.isNotEmpty()) {
        addresses[0].getAddressLine(0) ?: "Unknown location"
    } else {
        "Unknown location"
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

@Preview(showBackground = true)
@Composable
fun PreviewCreateGeocacheScreen() {
    CreateGeocacheScreen(userEmail = "1", onSuccess = {"1"})
}
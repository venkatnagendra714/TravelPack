@file:OptIn(ExperimentalMaterial3Api::class)

package uk.ac.tees.mad.s3623092

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

import uk.ac.tees.mad.s3623092.data.TripEntity
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.viewmodel.TripViewModel

private object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val ADD_TRIP = "add_trip"
    const val MANAGE_TRIPS = "manage_trips"
}

data class Trip(
    val id: Long,
    val name: String,
    val fromLocation: String,
    val toLocation: String,
    val startDate: String,
    val endDate: String,
    val notes: String
)

private fun TripEntity.toUiTrip(): Trip {
    return Trip(
        id = id,
        name = name,
        fromLocation = fromLocation,
        toLocation = toLocation,
        startDate = startDate,
        endDate = endDate,
        notes = notes
    )
}

private fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        name = name,
        fromLocation = fromLocation,
        toLocation = toLocation,
        startDate = startDate,
        endDate = endDate,
        notes = notes
    )
}

private fun Trip.routeText(): String {
    return if (fromLocation.isBlank()) {
        toLocation
    } else {
        "$fromLocation → $toLocation"
    }
}

private fun getCurrentDeviceLocation(
    context: Context,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!fineLocationGranted && !coarseLocationGranted) {
        onFailure()
        return
    }

    val locationClient =
        LocationServices.getFusedLocationProviderClient(context)

    val cancellationToken =
        CancellationTokenSource().token

    try {
        locationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken
            )
            .addOnSuccessListener { location ->

                if (location == null) {
                    onFailure()
                    return@addOnSuccessListener
                }

                reverseGeocodeLocation(
                    context = context,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
            .addOnFailureListener {
                onFailure()
            }
    } catch (_: SecurityException) {
        onFailure()
    }
}

@Suppress("DEPRECATION")
private fun reverseGeocodeLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    if (!Geocoder.isPresent()) {
        onFailure()
        return
    }

    val executor = Executors.newSingleThreadExecutor()

    executor.execute {
        val placeName = try {
            val geocoder = Geocoder(
                context,
                Locale.getDefault()
            )

            val address = geocoder
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()

            address?.getAddressLine(0)
                ?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(
                    address?.featureName,
                    address?.locality,
                    address?.subAdminArea,
                    address?.adminArea,
                    address?.postalCode,
                    address?.countryName
                )
                    .distinct()
                    .joinToString(", ")
        } catch (_: Exception) {
            ""
        }

        executor.shutdown()

        Handler(Looper.getMainLooper()).post {
            if (placeName.isBlank()) {
                onFailure()
            } else {
                onSuccess(placeName)
            }
        }
    }
}
class MainActivity : FragmentActivity() {

    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialisePlacesSdk()

        enableEdgeToEdge()

        setContent {
            TravelPackTheme {
                TravelPackNavigation(
                    tripViewModel = tripViewModel,
                    authenticate = { onSuccess, onError ->
                        showBiometricPrompt(onSuccess, onError)
                    }
                )
            }
        }
    }

    private fun initialisePlacesSdk() {
        val applicationInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )

        val placesApiKey = applicationInfo.metaData
            ?.getString("com.google.android.geo.API_KEY")
            .orEmpty()

        if (placesApiKey.isNotBlank() && !Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                placesApiKey
            )
        } else if (placesApiKey.isBlank()) {
            Log.e(
                "TravelPack",
                "Google Places API key is missing"
            )
        }
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(this)

        val authenticators =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            }

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock TravelPack")
                    .setSubtitle(
                        "Use fingerprint, face or your device PIN"
                    )
                    .setAllowedAuthenticators(authenticators)
                    .build()

                val prompt = BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            onSuccess()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            onError(errString.toString())
                        }

                        override fun onAuthenticationFailed() {
                            onError(
                                "Authentication failed. Please try again."
                            )
                        }
                    }
                )

                prompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onError(
                    "Set a device PIN or enrol a fingerprint."
                )
            }

            else -> {
                onError(
                    "Authentication is not available on this device."
                )
            }
        }
    }
}

@Composable
private fun TravelPackNavigation(
    tripViewModel: TripViewModel,
    authenticate: (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()

    val savedTrips by tripViewModel.trips.collectAsState()

    val trips = savedTrips.map { entity ->
        entity.toUiTrip()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashAndAuthenticationScreen(
                authenticate = authenticate,
                onAuthenticated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            TripDashboardScreen(
                trips = trips,
                onAddTrip = {
                    navController.navigate(Routes.ADD_TRIP)
                },
                onManageTrips = {
                    navController.navigate(Routes.MANAGE_TRIPS)
                }
            )
        }

        composable(Routes.ADD_TRIP) {
            AddNewTripScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaveTrip = { trip ->
                    tripViewModel.saveTrip(trip.toEntity())
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MANAGE_TRIPS) {
            ManageTripsScreen(
                trips = trips,
                onBack = {
                    navController.popBackStack()
                },
                onUpdateTrip = { updatedTrip ->
                    tripViewModel.updateTrip(updatedTrip.toEntity())
                },
                onDeleteTrip = { trip ->
                    tripViewModel.deleteTrip(trip.toEntity())
                }
            )
        }
    }
}

@Composable
private fun SplashAndAuthenticationScreen(
    authenticate: (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onAuthenticated: () -> Unit
) {
    var showSplash by rememberSaveable {
        mutableStateOf(true)
    }

    var errorMessage by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        delay(1.5.seconds)
        showSplash = false
    }

    if (showSplash) {
        SplashContent()
    } else {
        AuthenticationContent(
            errorMessage = errorMessage,
            onAuthenticate = {
                authenticate(
                    {
                        errorMessage = ""
                        onAuthenticated()
                    },
                    { message ->
                        errorMessage = message
                    }
                )
            }
        )
    }
}

@Composable
private fun SplashContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TravelPack",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Plan. Pack. Travel.",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun AuthenticationContent(
    errorMessage: String,
    onAuthenticate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔒",
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TravelPack Authentication",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Authenticate to access your trips.")

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAuthenticate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Authenticate")
            }
        }
    }
}

@Composable
private fun TripDashboardScreen(
    trips: List<Trip>,
    onAddTrip: () -> Unit,
    onManageTrips: () -> Unit
) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    val tabs = listOf(
        "Overview",
        "Checklist",
        "Documents",
        "Weather"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Trip Dashboard")
                },
                actions = {
                    TextButton(onClick = onManageTrips) {
                        Text("Manage Trips")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        text = {
                            Text(title)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> DashboardOverview(
                    trips = trips,
                    onAddTrip = onAddTrip
                )

                1 -> TravelChecklistContent()

                2 -> DocumentsContent()

                3 -> DestinationWeatherContent(
                    trip = trips.firstOrNull()
                )
            }
        }
    }
}

@Composable
private fun DashboardOverview(
    trips: List<Trip>,
    onAddTrip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TravelPack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (trips.isEmpty()) {
            Text(
                text = "No trips added yet.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Add your first trip to begin planning.")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddTrip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Your First Trip")
            }
        } else {
            Text(
                text = "${trips.size} trip(s) saved.",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = trips,
                    key = { it.id }
                ) { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = trip.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(trip.routeText())

                            Text(
                                "${trip.startDate} to ${trip.endDate}"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAddTrip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Another Trip")
            }
        }
    }
}

@Composable
private fun TravelChecklistContent() {
    var passportChecked by rememberSaveable {
        mutableStateOf(false)
    }

    var ticketsChecked by rememberSaveable {
        mutableStateOf(false)
    }

    var accommodationChecked by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Travel Checklist",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ChecklistItem(
            label = "Passport and travel documents",
            checked = passportChecked,
            onCheckedChange = {
                passportChecked = it
            }
        )

        ChecklistItem(
            label = "Tickets and transport",
            checked = ticketsChecked,
            onCheckedChange = {
                ticketsChecked = it
            }
        )

        ChecklistItem(
            label = "Accommodation confirmed",
            checked = accommodationChecked,
            onCheckedChange = {
                accommodationChecked = it
            }
        )
    }
}

@Composable
private fun ChecklistItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(label)
    }
}

@Composable
private fun DocumentsContent() {
    var selectedDocument by rememberSaveable {
        mutableStateOf("")
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedDocument = uri?.toString().orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Photos and Documents",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedDocument.isBlank()) {
                "No travel documents added."
            } else {
                "Selected document:\n$selectedDocument"
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                documentPicker.launch(
                    arrayOf("image/*", "application/pdf")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Photo or Document")
        }
    }
}

@Composable
private fun DestinationWeatherContent(
    trip: Trip?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Destination Weather",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (trip == null) {
            Text("Add a trip to view destination weather.")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = trip.toLocation,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Weather information for this destination."
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNewTripScreen(
    onBack: () -> Unit,
    onSaveTrip: (Trip) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Add New Trip")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        TripForm(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp),
            title = "Trip Details",
            initialTrip = null,
            onCancel = onBack,
            onSave = onSaveTrip
        )
    }
}

@Composable
private fun ManageTripsScreen(
    trips: List<Trip>,
    onBack: () -> Unit,
    onUpdateTrip: (Trip) -> Unit,
    onDeleteTrip: (Trip) -> Unit
) {
    var editingTrip by remember {
        mutableStateOf<Trip?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Trips")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No trips are available to manage.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = trips,
                    key = { it.id }
                ) { trip ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = trip.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(trip.routeText())

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                Button(
                                    onClick = {
                                        editingTrip = trip
                                    }
                                ) {
                                    Text("Edit")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        onDeleteTrip(trip)
                                    }
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = {
                editingTrip = null
            },
            title = {
                Text("Edit Trip")
            },
            text = {
                TripForm(
                    modifier = Modifier.heightIn(max = 500.dp),
                    title = "",
                    initialTrip = trip,
                    onCancel = {
                        editingTrip = null
                    },
                    onSave = { updatedTrip ->
                        onUpdateTrip(updatedTrip)
                        editingTrip = null
                    }
                )
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun TripForm(
    modifier: Modifier = Modifier,
    title: String,
    initialTrip: Trip?,
    onCancel: () -> Unit,
    onSave: (Trip) -> Unit
) {
    val context = LocalContext.current

    val placesClient: PlacesClient = remember(context) {
        Places.createClient(context)
    }

    val formKey = initialTrip?.id ?: 0L

    var tripName by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.name.orEmpty())
    }

    var fromLocation by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.fromLocation.orEmpty())
    }

    var toLocation by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.toLocation.orEmpty())
    }

    var startDate by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.startDate.orEmpty())
    }

    var endDate by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.endDate.orEmpty())
    }

    var notes by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.notes.orEmpty())
    }

    var errorMessage by rememberSaveable(formKey) {
        mutableStateOf("")
    }

    var locationError by rememberSaveable(formKey) {
        mutableStateOf("")
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                getCurrentDeviceLocation(
                    context = context,
                    onSuccess = {
                        fromLocation = it
                        locationError = ""
                    },
                    onFailure = {
                        locationError =
                            "Unable to find your current location."
                    }
                )
            } else {
                locationError =
                    "Location permission was denied."
            }
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = tripName,
            onValueChange = {
                tripName = it
            },
            label = {
                Text("Trip name")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LocationAutocompleteField(
            label = "From location",
            value = fromLocation,
            placesClient = placesClient,
            onValueChange = {
                fromLocation = it
                locationError = ""
            }
        )

        OutlinedButton(
            onClick = {
                val hasPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    getCurrentDeviceLocation(
                        context = context,
                        onSuccess = {
                            fromLocation = it
                            locationError = ""
                        },
                        onFailure = {
                            locationError =
                                "Unable to find your current location."
                        }
                    )
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Use current location")
        }

        if (locationError.isNotBlank()) {
            Text(
                text = locationError,
                color = MaterialTheme.colorScheme.error
            )
        }

        LocationAutocompleteField(
            label = "To location",
            value = toLocation,
            placesClient = placesClient,
            onValueChange = {
                toLocation = it
            }
        )

        OutlinedTextField(
            value = startDate,
            onValueChange = {
                startDate = it
            },
            label = {
                Text("Start date")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = {
                endDate = it
            },
            label = {
                Text("End date")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
            },
            label = {
                Text("Notes")
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (
                        tripName.isBlank() ||
                        fromLocation.isBlank() ||
                        toLocation.isBlank() ||
                        startDate.isBlank() ||
                        endDate.isBlank()
                    ) {
                        errorMessage =
                            "Please complete all required fields."
                    } else {
                        onSave(
                            Trip(
                                id = initialTrip?.id ?: 0L,
                                name = tripName.trim(),
                                fromLocation = fromLocation.trim(),
                                toLocation = toLocation.trim(),
                                startDate = startDate.trim(),
                                endDate = endDate.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Save Trip")
            }
        }
    }
}
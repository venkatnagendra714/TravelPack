@file:OptIn(ExperimentalMaterial3Api::class)

package uk.ac.tees.mad.s3623092

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

private object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val ADD_TRIP = "add_trip"
    const val MANAGE_TRIPS = "manage_trips"
}

data class Trip(
    val id: Long,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val notes: String
)

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TravelPackTheme {
                TravelPackNavigation(
                    authenticate = { onSuccess, onError ->
                        showBiometricPrompt(onSuccess, onError)
                    }
                )
            }
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
                    .setSubtitle("Use fingerprint, face or your device PIN")
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
                            onError("Authentication failed. Please try again.")
                        }
                    }
                )

                prompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onError("Set a device PIN or enrol a fingerprint.")
            }

            else -> {
                onError("Authentication is not available on this device.")
            }
        }
    }
}

@Composable
private fun TravelPackNavigation(
    authenticate: (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()
    val trips = remember { mutableStateListOf<Trip>() }

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
                    trips.add(trip)
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
                    val index = trips.indexOfFirst {
                        it.id == updatedTrip.id
                    }

                    if (index >= 0) {
                        trips[index] = updatedTrip
                    }
                },
                onDeleteTrip = { trip ->
                    trips.remove(trip)
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

                            Text(trip.destination)
                            Text("${trip.startDate} to ${trip.endDate}")
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
                        text = trip.destination,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Weather information for this destination.")
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

                            Text(trip.destination)

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
    val formKey = initialTrip?.id ?: 0L

    var tripName by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.name.orEmpty())
    }

    var destination by rememberSaveable(formKey) {
        mutableStateOf(initialTrip?.destination.orEmpty())
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
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = destination,
            onValueChange = {
                destination = it
            },
            label = {
                Text("Destination")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = startDate,
            onValueChange = {
                startDate = it
            },
            label = {
                Text("Start date")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = {
                endDate = it
            },
            label = {
                Text("End date")
            },
            modifier = Modifier.fillMaxWidth()
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
                        destination.isBlank() ||
                        startDate.isBlank() ||
                        endDate.isBlank()
                    ) {
                        errorMessage = "Please complete all required fields."
                    } else {
                        onSave(
                            Trip(
                                id = initialTrip?.id
                                    ?: System.currentTimeMillis(),
                                name = tripName.trim(),
                                destination = destination.trim(),
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
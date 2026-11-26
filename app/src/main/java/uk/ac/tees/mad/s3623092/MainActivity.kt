package uk.ac.tees.mad.s3623092

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import uk.ac.tees.mad.s3623092.data.ChecklistItemEntity
import uk.ac.tees.mad.s3623092.data.TripAttachmentEntity
import uk.ac.tees.mad.s3623092.data.TripEntity
import uk.ac.tees.mad.s3623092.ui.TravelPackApp
import uk.ac.tees.mad.s3623092.ui.model.AttachmentUiModel
import uk.ac.tees.mad.s3623092.ui.model.ChecklistUiModel
import uk.ac.tees.mad.s3623092.ui.model.TripListTab
import uk.ac.tees.mad.s3623092.ui.model.TripSortOption
import uk.ac.tees.mad.s3623092.ui.model.TripUiModel
import uk.ac.tees.mad.s3623092.ui.screens.AddTripScreen
import uk.ac.tees.mad.s3623092.ui.screens.DashboardScreen
import uk.ac.tees.mad.s3623092.ui.screens.DashboardTab
import uk.ac.tees.mad.s3623092.ui.screens.ManageTripsScreen
import uk.ac.tees.mad.s3623092.ui.screens.SecureAccessScreen
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.util.DocumentOpener
import uk.ac.tees.mad.s3623092.util.LocationUtils
import uk.ac.tees.mad.s3623092.util.TripDateUtils
import uk.ac.tees.mad.s3623092.util.TripFileUtils
import uk.ac.tees.mad.s3623092.viewmodel.TripViewModel
import uk.ac.tees.mad.s3623092.weather.WeatherViewModel

class MainActivity : FragmentActivity() {

    private val tripViewModel: TripViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var placesClient: PlacesClient

    private var isAuthenticating by mutableStateOf(false)
    private var authenticationError by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // Permission result is handled silently.
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        initialisePlaces()

        setContent {
            TravelPackTheme {
                TravelPackContent()
            }
        }
    }

    private fun initialisePlaces() {
        val applicationInfo =
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )

        val apiKey =
            applicationInfo.metaData
                ?.getString(
                    "com.google.android.geo.API_KEY"
                )
                .orEmpty()

        if (apiKey.isBlank()) {
            throw IllegalStateException(
                "MAPS_API_KEY is missing from local.properties."
            )
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                apiKey
            )
        }

        placesClient = Places.createClient(this)
    }
    @Composable
    private fun TravelPackContent() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val tripEntities by tripViewModel.trips.collectAsState()

        val orderedTripEntities = remember(
            tripEntities
        ) {
            tripEntities.sortedWith { first, second ->
                TripDateUtils.compareDates(
                    second.startDate,
                    first.startDate
                )
            }
        }

        val trips = remember(
            orderedTripEntities
        ) {
            orderedTripEntities.map {
                it.toUiModel()
            }
        }

        var selectedTripId by rememberSaveable {
            mutableStateOf<Long?>(null)
        }

        var selectedDashboardTabName by rememberSaveable {
            mutableStateOf(
                DashboardTab.OVERVIEW.name
            )
        }

        val selectedDashboardTab =
            runCatching {
                DashboardTab.valueOf(
                    selectedDashboardTabName
                )
            }.getOrDefault(
                DashboardTab.OVERVIEW
            )

        LaunchedEffect(
            orderedTripEntities
        ) {
            val selectedStillExists =
                selectedTripId != null &&
                        orderedTripEntities.any {
                            it.id == selectedTripId
                        }

            if (!selectedStillExists) {
                selectedTripId =
                    orderedTripEntities
                        .firstOrNull()
                        ?.id
            }
        }

        val selectedTripEntity =
            orderedTripEntities.firstOrNull {
                it.id == selectedTripId
            }

        val selectedTripUiModel =
            selectedTripEntity?.toUiModel()

        val attachmentFlow =
            remember(selectedTripId) {
                tripViewModel.attachmentsForTrip(
                    selectedTripId ?: 0L
                )
            }

        val checklistFlow =
            remember(selectedTripId) {
                tripViewModel.checklistForTrip(
                    selectedTripId ?: 0L
                )
            }

        val selectedAttachments by
        attachmentFlow.collectAsState(
            initial = emptyList()
        )

        val selectedChecklist by
        checklistFlow.collectAsState(
            initial = emptyList()
        )

        val weatherState by
        weatherViewModel.uiState.collectAsState()

        LaunchedEffect(
            selectedTripUiModel?.id,
            selectedTripUiModel?.toLocation
        ) {
            val destination =
                selectedTripUiModel
                    ?.toLocation
                    ?.trim()
                    .orEmpty()

            if (destination.isBlank()) {
                weatherViewModel.clearWeather()
            } else {
                weatherViewModel.loadWeather(
                    destination
                )
            }
        }

        var formTripName by rememberSaveable {
            mutableStateOf("")
        }

        var formFromLocation by rememberSaveable {
            mutableStateOf("")
        }

        var formToLocation by rememberSaveable {
            mutableStateOf("")
        }

        var formStartDate by rememberSaveable {
            mutableStateOf("")
        }

        var formEndDate by rememberSaveable {
            mutableStateOf("")
        }

        var formNotes by rememberSaveable {
            mutableStateOf("")
        }

        var newChecklistTitle by rememberSaveable {
            mutableStateOf("")
        }

        var formChecklistItems by remember {
            mutableStateOf<List<String>>(
                emptyList()
            )
        }

        var formAttachments by remember {
            mutableStateOf<List<AttachmentUiModel>>(
                emptyList()
            )
        }

        var formErrorMessage by remember {
            mutableStateOf<String?>(null)
        }

        var formIsSaving by remember {
            mutableStateOf(false)
        }

        var manageTabName by rememberSaveable {
            mutableStateOf(
                TripListTab.UPCOMING.name
            )
        }

        val manageTab =
            runCatching {
                TripListTab.valueOf(
                    manageTabName
                )
            }.getOrDefault(
                TripListTab.UPCOMING
            )

        var manageSearchQuery by rememberSaveable {
            mutableStateOf("")
        }

        var manageSortName by rememberSaveable {
            mutableStateOf(
                TripSortOption.START_DATE.name
            )
        }

        val manageSortOption =
            runCatching {
                TripSortOption.valueOf(
                    manageSortName
                )
            }.getOrDefault(
                TripSortOption.START_DATE
            )

        var pendingCameraUri by remember {
            mutableStateOf<Uri?>(null)
        }

        var locationTarget by remember {
            mutableStateOf<LocationTarget?>(null)
        }

        val cameraLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.TakePicture()
            ) { successful ->
                val capturedUri =
                    pendingCameraUri

                if (
                    successful &&
                    capturedUri != null
                ) {
                    val attachment =
                        AttachmentUiModel(
                            id = 0L,
                            uri = capturedUri.toString(),
                            fileName =
                                TripFileUtils.getFileName(
                                    context,
                                    capturedUri
                                ),
                            mimeType = "image/jpeg"
                        )

                    formAttachments =
                        (
                                formAttachments + attachment
                                ).distinctBy {
                                it.uri
                            }
                }

                pendingCameraUri = null
            }

        var launchCamera: (() -> Unit)? = null

        val cameraPermissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    launchCamera?.invoke()
                } else {
                    formErrorMessage =
                        "Camera permission is required."
                }
            }

        launchCamera = {
            try {
                val imageFile =
                    TripFileUtils.createCameraImageFile(
                        context
                    )

                val imageUri =
                    TripFileUtils.getFileProviderUri(
                        context,
                        imageFile
                    )

                pendingCameraUri = imageUri
                cameraLauncher.launch(imageUri)
            } catch (_: Exception) {
                formErrorMessage =
                    "Unable to open the camera."
            }
        }

        val locationClient =
            remember {
                LocationServices
                    .getFusedLocationProviderClient(
                        context
                    )
            }

        var fetchCurrentLocation: (() -> Unit)? =
            null

        val locationPermissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts
                    .RequestMultiplePermissions()
            ) { permissions ->
                if (
                    permissions.values.any {
                        it
                    }
                ) {
                    fetchCurrentLocation?.invoke()
                } else {
                    formErrorMessage =
                        "Location permission is required."
                }
            }

        fetchCurrentLocation = {
            LocationUtils.getCurrentLocation(
                context = context,
                locationClient = locationClient,
                onSuccess = { location ->
                    coroutineScope.launch {
                        val address =
                            LocationUtils
                                .getAddressFromLocation(
                                    context,
                                    location
                                )
                                ?: LocationUtils
                                    .formatCoordinates(
                                        location
                                    )

                        when (locationTarget) {
                            LocationTarget.FROM -> {
                                formFromLocation = address
                            }

                            LocationTarget.TO -> {
                                formToLocation = address
                            }

                            null -> Unit
                        }

                        locationTarget = null
                        formErrorMessage = null
                    }
                },
                onError = { message ->
                    formErrorMessage = message
                }
            )
        }

        fun requestCurrentLocation(
            target: LocationTarget
        ) {
            locationTarget = target

            if (
                LocationUtils.hasLocationPermission(
                    context
                )
            ) {
                fetchCurrentLocation?.invoke()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        fun addSelectedDocuments(
            uris: List<Uri>
        ) {
            uris.forEach { uri ->
                try {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                } catch (_: Exception) {
                    // Some providers do not support persistence.
                }
            }

            val newAttachments =
                uris.map { uri ->
                    AttachmentUiModel(
                        id = 0L,
                        uri = uri.toString(),
                        fileName =
                            TripFileUtils.getFileName(
                                context,
                                uri
                            ),
                        mimeType =
                            TripFileUtils.getMimeType(
                                context,
                                uri
                            )
                    )
                }

            formAttachments =
                (
                        formAttachments + newAttachments
                        ).distinctBy {
                        it.uri
                    }
        }

        fun saveTrip(
            editingTripId: Long?,
            onFinished: () -> Unit
        ) {
            val storedStartDate =
                TripDateUtils
                    .convertUserDateToStorage(
                        formStartDate
                    )

            val storedEndDate =
                TripDateUtils
                    .convertUserDateToStorage(
                        formEndDate
                    )

            if (
                storedStartDate == null ||
                storedEndDate == null
            ) {
                formErrorMessage =
                    "Enter valid dates in DD/MM/YYYY format."
                return
            }

            if (
                !TripDateUtils.isValidDateRange(
                    formStartDate,
                    formEndDate
                )
            ) {
                formErrorMessage =
                    "The end date must be on or after the start date."
                return
            }

            formIsSaving = true
            formErrorMessage = null

            val trip =
                TripEntity(
                    id = editingTripId ?: 0L,
                    name = formTripName.trim(),
                    fromLocation =
                        formFromLocation.trim(),
                    toLocation =
                        formToLocation.trim(),
                    startDate = storedStartDate,
                    endDate = storedEndDate,
                    notes = formNotes.trim(),
                    photoUri =
                        formAttachments
                            .firstOrNull {
                                it.isImage
                            }
                            ?.uri
                )

            val attachments =
                formAttachments.map {
                    TripAttachmentEntity(
                        id = 0L,
                        tripId = 0L,
                        uri = it.uri,
                        fileName = it.fileName,
                        mimeType = it.mimeType
                    )
                }

            if (editingTripId == null) {
                val checklistItems =
                    formChecklistItems.mapNotNull {
                            title ->
                        val cleanTitle =
                            title.trim()

                        if (
                            cleanTitle.isBlank()
                        ) {
                            null
                        } else {
                            ChecklistItemEntity(
                                id = 0L,
                                tripId = 0L,
                                title = cleanTitle,
                                isCompleted = false
                            )
                        }
                    }

                tripViewModel
                    .saveTripWithAttachmentsAndChecklist(
                        trip = trip,
                        attachments = attachments,
                        checklistItems = checklistItems
                    )
            } else {
                tripViewModel.updateTrip(trip)

                val newAttachments =
                    attachments.filter {
                        it.uri !in selectedAttachments
                            .map { saved ->
                                saved.uri
                            }
                    }

                tripViewModel.addAttachments(
                    tripId = editingTripId,
                    attachments = newAttachments
                )

                formChecklistItems.forEach { title ->
                    tripViewModel.addChecklistItem(
                        tripId = editingTripId,
                        title = title
                    )
                }
            }

            formIsSaving = false
            onFinished()
        }

        TravelPackApp(
            secureAccessContent = { onAuthenticated ->
                SecureAccessScreen(
                    isAuthenticating = isAuthenticating,
                    errorMessage = authenticationError,
                    onAuthenticate = {
                        authenticateUser(
                            onSuccess = {
                                requestNotificationPermission()
                                onAuthenticated()
                            }
                        )
                    }
                )
            },
            dashboardContent = { onAddTrip, onManageTrips ->
                DashboardScreen(
                    trips = trips,
                    selectedTripId = selectedTripId,
                    selectedTab = selectedDashboardTab,
                    onTripSelected = { trip ->
                        selectedTripId = trip.id
                    },
                    onTabSelected = { tab ->
                        selectedDashboardTabName =
                            tab.name
                    },
                    onAddTrip = {
                        formTripName = ""
                        formFromLocation = ""
                        formToLocation = ""
                        formStartDate = ""
                        formEndDate = ""
                        formNotes = ""
                        formChecklistItems = emptyList()
                        formAttachments = emptyList()
                        formErrorMessage = null

                        onAddTrip()
                    },
                    onManageTrips = onManageTrips,
                    checklistItems =
                        selectedChecklist.map {
                            ChecklistUiModel(
                                id = it.id,
                                tripId = it.tripId,
                                title = it.title,
                                isCompleted =
                                    it.isCompleted
                            )
                        },
                    newChecklistItemTitle =
                        newChecklistTitle,
                    onNewChecklistItemTitleChange = {
                        newChecklistTitle = it
                    },
                    onAddChecklistItem = {
                        val cleanTitle =
                            newChecklistTitle.trim()

                        if (
                            selectedTripId != null &&
                            cleanTitle.isNotBlank()
                        ) {
                            tripViewModel.addChecklistItem(
                                tripId = selectedTripId!!,
                                title = cleanTitle
                            )

                            newChecklistTitle = ""
                        }
                    },
                    onToggleChecklistItem = { item ->
                        tripViewModel.updateChecklistItem(
                            ChecklistItemEntity(
                                id = item.id,
                                tripId = item.tripId,
                                title = item.title,
                                isCompleted =
                                    !item.isCompleted
                            )
                        )
                    },
                    onDeleteChecklistItem = { item ->
                        tripViewModel.deleteChecklistItem(
                            ChecklistItemEntity(
                                id = item.id,
                                tripId = item.tripId,
                                title = item.title,
                                isCompleted =
                                    item.isCompleted
                            )
                        )
                    },
                    attachments =
                        selectedAttachments.map {
                            AttachmentUiModel(
                                id = it.id,
                                uri = it.uri,
                                fileName = it.fileName,
                                mimeType = it.mimeType
                            )
                        },
                    onAttachmentsSelected = { uris ->
                        val tripId = selectedTripId

                        if (tripId != null) {
                            uris.forEach { uri ->
                                try {
                                    context.contentResolver
                                        .takePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                } catch (_: Exception) {
                                    // Provider may not support persistence.
                                }
                            }

                            val attachments =
                                uris.map { uri ->
                                    TripAttachmentEntity(
                                        id = 0L,
                                        tripId = tripId,
                                        uri = uri.toString(),
                                        fileName =
                                            TripFileUtils
                                                .getFileName(
                                                    context,
                                                    uri
                                                ),
                                        mimeType =
                                            TripFileUtils
                                                .getMimeType(
                                                    context,
                                                    uri
                                                )
                                    )
                                }

                            tripViewModel.addAttachments(
                                tripId = tripId,
                                attachments = attachments
                            )
                        }
                    },
                    onOpenAttachment = { attachment ->
                        DocumentOpener.openAttachment(
                            context = context,
                            uriString = attachment.uri,
                            mimeType = attachment.mimeType
                        )
                    },
                    onDeleteAttachment = { attachment ->
                        val databaseAttachment =
                            selectedAttachments.firstOrNull {
                                it.id == attachment.id
                            }

                        if (databaseAttachment != null) {
                            tripViewModel.deleteAttachment(
                                databaseAttachment
                            )
                        }
                    },
                    weatherState = weatherState,
                    onRetryWeather = {
                        weatherViewModel.retry()
                    }
                )
            },
            addTripContent = { onBack, editingTripId ->
                val editingEntity =
                    orderedTripEntities.firstOrNull {
                        it.id == editingTripId
                    }

                val editAttachmentFlow =
                    remember(editingTripId) {
                        tripViewModel.attachmentsForTrip(
                            editingTripId ?: 0L
                        )
                    }

                val editAttachments by
                editAttachmentFlow.collectAsState(
                    initial = emptyList()
                )

                LaunchedEffect(
                    editingTripId,
                    editingEntity?.id,
                    editAttachments
                ) {
                    if (editingEntity == null) {
                        formTripName = ""
                        formFromLocation = ""
                        formToLocation = ""
                        formStartDate = ""
                        formEndDate = ""
                        formNotes = ""
                        formChecklistItems = emptyList()
                        formAttachments = emptyList()
                    } else {
                        formTripName =
                            editingEntity.name
                        formFromLocation =
                            editingEntity.fromLocation
                        formToLocation =
                            editingEntity.toLocation
                        formStartDate =
                            TripDateUtils
                                .convertStorageDateToUser(
                                    editingEntity.startDate
                                )
                        formEndDate =
                            TripDateUtils
                                .convertStorageDateToUser(
                                    editingEntity.endDate
                                )
                        formNotes =
                            editingEntity.notes

                        formAttachments =
                            editAttachments.map {
                                AttachmentUiModel(
                                    id = it.id,
                                    uri = it.uri,
                                    fileName = it.fileName,
                                    mimeType = it.mimeType
                                )
                            }
                    }
                }

                AddTripScreen(
                    placesClient = placesClient,
                    tripName = formTripName,
                    fromLocation = formFromLocation,
                    toLocation = formToLocation,
                    startDate = formStartDate,
                    endDate = formEndDate,
                    notes = formNotes,
                    attachments = formAttachments,
                    checklistItems = formChecklistItems,
                    newChecklistItem = newChecklistTitle,
                    isSaving = formIsSaving,
                    errorMessage = formErrorMessage,
                    onTripNameChange = {
                        formTripName = it
                    },
                    onFromLocationChange = {
                        formFromLocation = it
                    },
                    onToLocationChange = {
                        formToLocation = it
                    },
                    onStartDateChange = {
                        formStartDate = it
                    },
                    onEndDateChange = {
                        formEndDate = it
                    },
                    onNotesChange = {
                        formNotes = it
                    },
                    onNewChecklistItemChange = {
                        newChecklistTitle = it
                    },
                    onAddChecklistItem = {
                        val cleanTitle =
                            newChecklistTitle.trim()

                        if (
                            cleanTitle.isNotBlank()
                        ) {
                            formChecklistItems =
                                formChecklistItems +
                                        cleanTitle

                            newChecklistTitle = ""
                        }
                    },
                    onRemoveChecklistItem = { item ->
                        formChecklistItems =
                            formChecklistItems.filterNot {
                                it == item
                            }
                    },
                    onUseCurrentLocationForFrom = {
                        requestCurrentLocation(
                            LocationTarget.FROM
                        )
                    },
                    onUseCurrentLocationForTo = {
                        requestCurrentLocation(
                            LocationTarget.TO
                        )
                    },
                    onCapturePhoto = {
                        val hasCameraPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            launchCamera?.invoke()
                        } else {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    },
                    onDocumentsSelected = {
                        addSelectedDocuments(it)
                    },
                    onRemoveAttachment = { attachment ->
                        formAttachments =
                            formAttachments.filterNot {
                                it.uri == attachment.uri
                            }
                    },
                    onSaveTrip = {
                        saveTrip(
                            editingTripId = editingTripId,
                            onFinished = onBack
                        )
                    },
                    onCancel = onBack
                )
            },
            manageTripsContent = { onBack, onEditTrip ->
                ManageTripsScreen(
                    trips = trips,
                    selectedTab = manageTab,
                    searchQuery = manageSearchQuery,
                    sortOption = manageSortOption,
                    onTabSelected = { tab ->
                        manageTabName = tab.name
                    },
                    onSearchQueryChange = {
                        manageSearchQuery = it
                    },
                    onSortOptionChange = {
                        manageSortName = it.name
                    },
                    onEditTrip = { trip ->
                        onEditTrip(trip.id)
                    },
                    onDeleteTrip = { trip ->
                        tripViewModel.deleteTrip(
                            TripEntity(
                                id = trip.id,
                                name = trip.name,
                                fromLocation =
                                    trip.fromLocation,
                                toLocation =
                                    trip.toLocation,
                                startDate =
                                    trip.startDate,
                                endDate =
                                    trip.endDate,
                                notes = trip.notes,
                                photoUri = trip.photoUri
                            )
                        )
                    },
                    onShareTrip = {
                        shareTrip(it)
                    },
                    onBack = onBack
                )
            }
        )
    }

    private fun authenticateUser(
        onSuccess: () -> Unit
    ) {
        isAuthenticating = true
        authenticationError = null

        val biometricManager =
            BiometricManager.from(this)

        val result =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators
                    .BIOMETRIC_STRONG
            )

        if (
            result !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            isAuthenticating = false
            authenticationError =
                "Biometric authentication is not available."
            return
        }

        val executor =
            ContextCompat.getMainExecutor(this)

        val prompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        isAuthenticating = false
                        authenticationError = null
                        onSuccess()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        isAuthenticating = false
                        authenticationError =
                            errString.toString()
                    }

                    override fun onAuthenticationFailed() {
                        authenticationError =
                            "Authentication failed. Try again."
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock TravelPack")
                .setSubtitle(
                    "Authenticate to view your trips"
                )
                .setNegativeButtonText("Cancel")
                .build()

        prompt.authenticate(promptInfo)
    }

    private fun requestNotificationPermission() {
        if (
            android.os.Build.VERSION.SDK_INT >= 33
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun shareTrip(
        trip: TripUiModel
    ) {
        val shareText =
            """
            Trip: ${trip.name}
            From: ${trip.fromLocation}
            To: ${trip.toLocation}
            Start date: ${
                TripDateUtils.convertStorageDateToUser(
                    trip.startDate
                )
            }
            End date: ${
                TripDateUtils.convertStorageDateToUser(
                    trip.endDate
                )
            }
            """.trimIndent()

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                "Share trip information"
            )
        )
    }

    private fun TripEntity.toUiModel():
            TripUiModel {
        return TripUiModel(
            id = id,
            name = name,
            fromLocation = fromLocation,
            toLocation = toLocation,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            photoUri = photoUri
        )
    }

    private enum class LocationTarget {
        FROM,
        TO
    }
}
package uk.ac.tees.mad.s3623092

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                var showSplash by rememberSaveable {
                    mutableStateOf(true)
                }

                var isAuthenticated by rememberSaveable {
                    mutableStateOf(false)
                }

                var authenticationMessage by rememberSaveable {
                    mutableStateOf("")
                }

                LaunchedEffect(Unit) {
                    delay(1500L)
                    showSplash = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        showSplash -> {
                            SplashScreen()
                        }

                        !isAuthenticated -> {
                            AuthenticationScreen(
                                message = authenticationMessage,
                                onAuthenticate = {
                                    showBiometricAuthentication(
                                        onSuccess = {
                                            authenticationMessage = ""
                                            isAuthenticated = true
                                        },
                                        onMessage = { message ->
                                            authenticationMessage = message
                                        }
                                    )
                                }
                            )
                        }

                        else -> {
                            TravelPackNavigation(
                                onLockApplication = {
                                    isAuthenticated = false
                                    authenticationMessage = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricAuthentication(
        onSuccess: () -> Unit,
        onMessage: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(this)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // The device can authenticate.
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                onMessage("This device does not have biometric hardware.")
                return
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                onMessage("Biometric authentication is currently unavailable.")
                return
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                onMessage(
                    "No fingerprint or face is enrolled. " +
                            "Add one in the device settings."
                )
                return
            }

            else -> {
                onMessage("Biometric authentication is unavailable.")
                return
            }
        }

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onMessage(
                        "Biometric authentication was not recognised. Try again."
                    )
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onMessage(errString.toString())
                }
            }
        )

        val promptInformation = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock TravelPack")
            .setSubtitle("Confirm your identity to continue")
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInformation)
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✈",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TravelPack",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Plan. Pack. Travel.",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun AuthenticationScreen(
    message: String,
    onAuthenticate: () -> Unit
) {
    LaunchedEffect(Unit) {
        onAuthenticate()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔒",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TravelPack Authentication",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Use your fingerprint or face to access your trips.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
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

private object TravelPackRoutes {
    const val DASHBOARD = "dashboard"
    const val ADD_TRIP = "add_trip"
    const val CHECKLIST = "checklist"
    const val DOCUMENTS = "documents"
    const val WEATHER = "weather"
}

@Composable
fun TravelPackNavigation(
    onLockApplication: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TravelPackRoutes.DASHBOARD,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(TravelPackRoutes.DASHBOARD) {
            DashboardScreen(
                onAddTrip = {
                    navController.navigate(TravelPackRoutes.ADD_TRIP)
                },
                onChecklist = {
                    navController.navigate(TravelPackRoutes.CHECKLIST)
                },
                onDocuments = {
                    navController.navigate(TravelPackRoutes.DOCUMENTS)
                },
                onWeather = {
                    navController.navigate(TravelPackRoutes.WEATHER)
                },
                onLockApplication = onLockApplication
            )
        }

        composable(TravelPackRoutes.ADD_TRIP) {
            PlaceholderScreen(
                title = "Add Trip",
                description = "Trip creation will be developed in Sprint 2.",
                navController = navController
            )
        }

        composable(TravelPackRoutes.CHECKLIST) {
            PlaceholderScreen(
                title = "Travel Checklist",
                description = "Checklist management will be developed later.",
                navController = navController
            )
        }

        composable(TravelPackRoutes.DOCUMENTS) {
            PlaceholderScreen(
                title = "Photos and Documents",
                description = "Secure document storage will be developed later.",
                navController = navController
            )
        }

        composable(TravelPackRoutes.WEATHER) {
            PlaceholderScreen(
                title = "Destination Weather",
                description = "Weather information will be connected later.",
                navController = navController
            )
        }
    }
}

@Composable
fun DashboardScreen(
    onAddTrip: () -> Unit,
    onChecklist: () -> Unit,
    onDocuments: () -> Unit,
    onWeather: () -> Unit,
    onLockApplication: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                top = 48.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TravelPack",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your travel dashboard",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No trips added yet.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first trip to begin planning.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        DashboardButton(
            text = "Add Your First Trip",
            onClick = onAddTrip
        )

        DashboardButton(
            text = "Travel Checklist",
            onClick = onChecklist
        )

        DashboardButton(
            text = "Photos and Documents",
            onClick = onDocuments
        )

        DashboardButton(
            text = "Destination Weather",
            onClick = onWeather
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLockApplication,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lock Application")
        }
    }
}

@Composable
fun DashboardButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text)
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        DashboardScreen(
            onAddTrip = {},
            onChecklist = {},
            onDocuments = {},
            onWeather = {},
            onLockApplication = {}
        )
    }
}
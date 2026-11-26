package uk.ac.tees.mad.s3623092.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun SecureAccessScreen(
    isAuthenticating: Boolean,
    errorMessage: String?,
    onAuthenticate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TravelPack",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Your trips, organised simply",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = 8.dp
                )
            )

            Text(
                text = "Secure access",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    top = 40.dp
                )
            )

            Text(
                text = "Use your fingerprint or face authentication to open your travel information.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp,
                        bottom = 24.dp
                    )
            )

            if (isAuthenticating) {
                CircularProgressIndicator()

                Text(
                    text = "Waiting for authentication...",
                    modifier = Modifier.padding(
                        top = 12.dp
                    )
                )
            } else {
                Button(
                    onClick = onAuthenticate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Authenticate"
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp
                        )
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun SecureAccessScreenPreview() {
    TravelPackTheme {
        SecureAccessScreen(
            isAuthenticating = false,
            errorMessage = null,
            onAuthenticate = {}
        )
    }
}
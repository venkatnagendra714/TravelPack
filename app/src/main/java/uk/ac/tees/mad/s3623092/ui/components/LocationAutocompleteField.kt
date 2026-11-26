package uk.ac.tees.mad.s3623092.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun LocationAutocompleteField(
    label: String,
    value: String,
    placesClient: PlacesClient?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var predictions by remember {
        mutableStateOf<List<AutocompletePrediction>>(
            emptyList()
        )
    }

    var hasFocus by remember {
        mutableStateOf(false)
    }

    var serviceError by remember {
        mutableStateOf<String?>(null)
    }

    var skipNextSearch by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        value,
        placesClient,
        hasFocus
    ) {
        if (
            placesClient == null ||
            !hasFocus
        ) {
            predictions = emptyList()
            return@LaunchedEffect
        }

        if (skipNextSearch) {
            skipNextSearch = false
            predictions = emptyList()
            return@LaunchedEffect
        }

        val query = value.trim()

        if (query.length < 2) {
            predictions = emptyList()
            serviceError = null
            return@LaunchedEffect
        }

        delay(350L)

        val request =
            FindAutocompletePredictionsRequest
                .builder()
                .setQuery(query)
                .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions =
                    response.autocompletePredictions.take(5)

                serviceError = null
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "LocationAutocomplete",
                    "Places autocomplete failed",
                    exception
                )

                predictions = emptyList()
                serviceError =
                    "Location suggestions are unavailable."
            }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                serviceError = null
                onValueChange(it)
            },
            label = {
                Text(label)
            },
            placeholder = {
                Text("Type at least two letters")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.hasFocus

                    if (!focusState.hasFocus) {
                        predictions = emptyList()
                    }
                }
        )

        if (
            hasFocus &&
            predictions.isNotEmpty()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(top = 4.dp)
            ) {
                Column {
                    predictions.forEach { prediction ->
                        val placeText =
                            prediction
                                .getFullText(null)
                                .toString()

                        TextButton(
                            onClick = {
                                skipNextSearch = true
                                predictions = emptyList()
                                serviceError = null
                                onValueChange(placeText)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = placeText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        if (!serviceError.isNullOrBlank()) {
            Text(
                text = serviceError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 380
)
@Composable
private fun LocationAutocompleteFieldPreview() {
    TravelPackTheme {
        LocationAutocompleteField(
            label = "Destination",
            value = "Edinburgh",
            placesClient = null,
            onValueChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
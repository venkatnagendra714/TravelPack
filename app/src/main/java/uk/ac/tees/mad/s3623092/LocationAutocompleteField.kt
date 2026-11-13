package uk.ac.tees.mad.s3623092

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay

@Composable
fun LocationAutocompleteField(
    label: String,
    value: String,
    placesClient: PlacesClient,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var predictions by remember {
        mutableStateOf<List<AutocompletePrediction>>(emptyList())
    }

    var skipNextSearch by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(value) {
        if (skipNextSearch) {
            skipNextSearch = false
            predictions = emptyList()
            return@LaunchedEffect
        }

        val query = value.trim()

        if (query.length < 2) {
            predictions = emptyList()
            return@LaunchedEffect
        }

        delay(350)

        val request = FindAutocompletePredictionsRequest
            .builder()
            .setQuery(query)
            .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response
                    .autocompletePredictions
                    .take(5)
            }
            .addOnFailureListener {
                predictions = emptyList()
            }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            label = {
                Text(label)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (predictions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
            ) {
                Column {
                    predictions.forEach { prediction ->
                        val placeText = prediction
                            .getFullText(null)
                            .toString()

                        TextButton(
                            onClick = {
                                skipNextSearch = true
                                predictions = emptyList()
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
    }
}
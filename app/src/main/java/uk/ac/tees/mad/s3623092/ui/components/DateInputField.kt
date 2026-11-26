package uk.ac.tees.mad.s3623092.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme
import uk.ac.tees.mad.s3623092.util.TripDateUtils

@Composable
fun DateInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isComplete =
        value.length == 10

    val hasError =
        isComplete &&
                !TripDateUtils.isValidUserDate(value)

    val supportingMessage =
        if (hasError) {
            "Enter a valid date in DD/MM/YYYY format."
        } else {
            "Format: DD/MM/YYYY"
        }

    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(
                formatDateInput(input)
            )
        },
        label = {
            Text(label)
        },
        placeholder = {
            Text(TripDateUtils.DATE_HINT)
        },
        supportingText = {
            Text(supportingMessage)
        },
        isError = hasError,
        enabled = enabled,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}

private fun formatDateInput(
    input: String
): String {
    val digitsOnly = input
        .filter { it.isDigit() }
        .take(8)

    return buildString {
        digitsOnly.forEachIndexed { index, character ->
            if (
                index == 2 ||
                index == 4
            ) {
                append("/")
            }

            append(character)
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 380
)
@Composable
private fun DateInputFieldPreview() {
    TravelPackTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DateInputField(
                label = "Start date",
                value = "10/12/2026",
                onValueChange = {}
            )

            DateInputField(
                label = "End date",
                value = "31/15/2026",
                onValueChange = {}
            )
        }
    }
}
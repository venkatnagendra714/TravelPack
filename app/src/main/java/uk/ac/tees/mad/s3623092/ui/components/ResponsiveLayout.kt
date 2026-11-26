package uk.ac.tees.mad.s3623092.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.s3623092.ui.theme.TravelPackTheme

@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        if (maxWidth >= 600.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ResponsiveTwoPane(
    firstContent: @Composable () -> Unit,
    secondContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        if (maxWidth >= 600.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    firstContent()
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    secondContent()
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                firstContent()
                secondContent()
            }
        }
    }
}

@Composable
fun ResponsiveHorizontalSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.width(16.dp)
    )
}

@Composable
fun ResponsiveHorizontalPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Preview(
    showBackground = true,
    widthDp = 380
)
@Composable
private fun ResponsiveLayoutPhonePreview() {
    TravelPackTheme {
        ResponsiveLayout(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Responsive phone layout",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 700
)
@Composable
private fun ResponsiveLayoutTabletPreview() {
    TravelPackTheme {
        ResponsiveTwoPane(
            firstContent = {
                Text(
                    text = "First pane",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            secondContent = {
                Text(
                    text = "Second pane",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}
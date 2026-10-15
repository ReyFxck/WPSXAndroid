package io.github.reyfxck.wpsxandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Feature(
    val title: String,
    val description: String,
    val status: String,
)

private val bootstrapFeatures = listOf(
    Feature("Wi-Fi / WPS Scanner", "Passive discovery and AP capability inspection.", "Foundation"),
    Feature("WPS PIN Engine", "Deterministic, test-vector-driven PIN derivation algorithms.", "Planned"),
    Feature("Pixie Dust", "Authorized audit backend isolated from the Android UI.", "Planned"),
    Feature("Reaver Backend", "Capability-gated native/root integration for supported devices.", "Planned"),
)

@Composable
fun WpsxApp() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "WPSXAndroid",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Authorized Wi-Fi/WPS security research toolkit",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                items(bootstrapFeatures) { feature ->
                    FeatureCard(feature)
                }

                item {
                    Text(
                        text = "Use only on networks you own or have explicit permission to test.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(feature.title, fontWeight = FontWeight.SemiBold)
            Text(feature.description, style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${feature.status}", style = MaterialTheme.typography.labelMedium)
        }
    }
}

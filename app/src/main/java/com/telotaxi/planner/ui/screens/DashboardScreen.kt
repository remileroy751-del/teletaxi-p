package com.telotaxi.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.data.RideStatus
import com.telotaxi.planner.ui.PlannerViewModel
import com.telotaxi.planner.ui.WeatherUiState
import com.telotaxi.planner.ui.theme.*
import com.telotaxi.planner.weather.WeatherCodeMapper

@Composable
fun DashboardScreen(
    viewModel: PlannerViewModel,
    onAddRide: () -> Unit,
    onOpenRide: (Long) -> Unit,
    onOpenWeather: () -> Unit,
    onOpenReports: () -> Unit
) {
    val allRides by viewModel.allRides.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()

    val today = viewModel.todayRides()
    val tomorrow = viewModel.tomorrowRides()
    val todayDone = today.count { it.status == RideStatus.TERMINEE }
    val todayPlanned = today.count { it.status == RideStatus.PLANIFIEE }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRide,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvelle course") },
                containerColor = TaxiYellow,
                contentColor = androidx.compose.ui.graphics.Color.Black
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Tableau de bord", style = MaterialTheme.typography.headlineMedium)
                Text("Bonne route aujourd'hui 🚕", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            // Accès rapide aux rapports de chiffre d'affaires
            item {
                OutlinedButton(
                    onClick = onOpenReports,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voir les rapports de chiffre d'affaires")
                }
            }

            // Statistiques rapides
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(Modifier.weight(1f), "Aujourd'hui", "${today.size}", Icons.Default.Today, TaxiBlue)
                    StatCard(Modifier.weight(1f), "Demain", "${tomorrow.size}", Icons.Default.EventUpcoming, WarningOrange)
                    StatCard(Modifier.weight(1f), "Terminées", "$todayDone", Icons.Default.CheckCircle, SuccessGreen)
                }
            }

            // Mini widget météo cliquable
            item {
                WeatherPreviewCard(weatherState, onClick = onOpenWeather)
            }

            // Courses du jour
            item {
                SectionHeader("Courses d'aujourd'hui", today.size)
            }
            if (today.isEmpty()) {
                item { EmptyStateRow("Aucune course programmée aujourd'hui") }
            } else {
                items(today, key = { "today_${it.id}" }) { ride ->
                    RideCard(
                        ride = ride,
                        onClick = { onOpenRide(ride.id) },
                        onComplete = { viewModel.markCompleted(ride) },
                        onCancel = { viewModel.markCancelled(ride) }
                    )
                }
            }

            // Courses de demain
            item {
                SectionHeader("Courses de demain", tomorrow.size)
            }
            if (tomorrow.isEmpty()) {
                item { EmptyStateRow("Aucune course programmée demain") }
            } else {
                items(tomorrow, key = { "tomorrow_${it.id}" }) { ride ->
                    RideCard(
                        ride = ride,
                        onClick = { onOpenRide(ride.id) },
                        onComplete = { viewModel.markCompleted(ride) },
                        onCancel = { viewModel.markCancelled(ride) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text("$count", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun EmptyStateRow(text: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BackgroundLight)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text, color = TextSecondary)
        }
    }
}

@Composable
private fun WeatherPreviewCard(state: WeatherUiState, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TaxiBlue)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (state) {
                is WeatherUiState.Success -> {
                    val current = state.data.current
                    val (label, emoji) = WeatherCodeMapper.describe(current.weather_code)
                    Column {
                        Text("Météo actuelle", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                        Text("${current.temperature_2m.toInt()}°C · $label", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                }
                is WeatherUiState.Loading -> {
                    Text("Chargement de la météo…", color = androidx.compose.ui.graphics.Color.White)
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                is WeatherUiState.Error -> {
                    Text("Météo indisponible — appuyer pour réessayer", color = androidx.compose.ui.graphics.Color.White)
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
                is WeatherUiState.PermissionNeeded -> {
                    Text("Activer la localisation pour voir la météo", color = androidx.compose.ui.graphics.Color.White)
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}

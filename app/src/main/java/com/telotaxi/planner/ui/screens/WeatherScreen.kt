package com.telotaxi.planner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telotaxi.planner.ui.PlannerViewModel
import com.telotaxi.planner.ui.WeatherUiState
import com.telotaxi.planner.ui.theme.TaxiBlue
import com.telotaxi.planner.ui.theme.TaxiBlueDark
import com.telotaxi.planner.ui.theme.TextSecondary
import com.telotaxi.planner.ui.theme.WarningOrange
import com.telotaxi.planner.weather.WeatherCodeMapper
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WeatherScreen(viewModel: PlannerViewModel) {
    val context = LocalContext.current
    val state by viewModel.weatherState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.loadWeather() else viewModel.notifyPermissionNeeded() }

    fun checkAndLoad() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.loadWeather() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(Unit) { checkAndLoad() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Météo", style = MaterialTheme.typography.headlineMedium)
        Text("Prévisions selon votre position actuelle", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is WeatherUiState.Loading -> {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WeatherUiState.PermissionNeeded -> {
                PermissionCard { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            }
            is WeatherUiState.Error -> {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Impossible de charger la météo", fontWeight = FontWeight.Bold)
                        Text(s.message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { checkAndLoad() }) { Text("Réessayer") }
                    }
                }
            }
            is WeatherUiState.Success -> {
                val current = s.data.current
                val (label, emoji) = WeatherCodeMapper.describe(current.weather_code)

                // Carte des conditions actuelles
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TaxiBlue)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Actuellement", color = Color.White.copy(alpha = 0.85f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${current.temperature_2m.toInt()}°C", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 44.sp))
                            Spacer(Modifier.width(12.dp))
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                        }
                        Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("Ressenti ${current.apparent_temperature.toInt()}°C", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            InfoPill(Icons.Default.Air, "${current.wind_speed_10m.toInt()} km/h vent")
                            InfoPill(Icons.Default.WaterDrop, "${current.relative_humidity_2m}% humidité")
                        }

                        if (WeatherCodeMapper.isRiskyForDriving(current.weather_code)) {
                            Spacer(Modifier.height(12.dp))
                            Surface(color = WarningOrange, shape = RoundedCornerShape(10.dp)) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Prudence : conditions de route difficiles", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Aujourd'hui & demain", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))

                val daily = s.data.daily
                val dayFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)

                daily.time.forEachIndexed { index, dateStr ->
                    if (index > 1) return@forEachIndexed // uniquement aujourd'hui + demain
                    val (dayLabel, dayEmoji) = WeatherCodeMapper.describe(daily.weather_code[index])
                    val dateLabel = try {
                        val d = isoFormat.parse(dateStr)
                        if (index == 0) "Aujourd'hui — ${dayFormat.format(d!!)}" else "Demain — ${dayFormat.format(d!!)}"
                    } catch (e: Exception) { dateStr }

                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dateLabel.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                                Text(dayLabel, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text("Pluie : ${daily.precipitation_probability_max[index]}%  ·  Vent : ${daily.wind_speed_10m_max[index].toInt()} km/h",
                                    color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(dayEmoji, style = MaterialTheme.typography.headlineMedium)
                                Text("${daily.temperature_2m_max[index].toInt()}° / ${daily.temperature_2m_min[index].toInt()}°",
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Prévisions horaires", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                val hourly = s.data.hourly
                val hourFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
                val hourlyIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.FRANCE)
                val now = System.currentTimeMillis()

                val nextHours = hourly.time.mapIndexedNotNull { i, t ->
                    try {
                        val millis = hourlyIso.parse(t)?.time ?: return@mapIndexedNotNull null
                        if (millis >= now) Triple(i, millis, t) else null
                    } catch (e: Exception) { null }
                }.take(12)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(nextHours) { hourEntry ->
                        val index = hourEntry.first
                        val millis = hourEntry.second
                        val (_, hEmoji) = WeatherCodeMapper.describe(hourly.weather_code[index])
                        Card(shape = RoundedCornerShape(14.dp)) {
                            Column(
                                Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(hourFormat.format(millis), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(hEmoji)
                                Text("${hourly.temperature_2m[index].toInt()}°", fontWeight = FontWeight.SemiBold)
                                Text("${hourly.precipitation_probability[index]}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PermissionCard(onRequest: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("Localisation requise", fontWeight = FontWeight.Bold)
            Text(
                "Pour afficher la météo à votre position actuelle, autorisez l'accès à la localisation.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRequest) { Text("Autoriser la localisation") }
        }
    }
}

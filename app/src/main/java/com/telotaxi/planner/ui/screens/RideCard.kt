package com.telotaxi.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.data.RideStatus
import com.telotaxi.planner.maps.MapsLauncher
import com.telotaxi.planner.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RideCard(
    ride: Ride,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }
    val statusColor = when (ride.status) {
        RideStatus.PLANIFIEE -> TaxiBlue
        RideStatus.EN_COURS -> WarningOrange
        RideStatus.TERMINEE -> SuccessGreen
        RideStatus.ANNULEE -> DangerRed
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heure de la course en évidence
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeFormat.format(ride.dateTimeMillis),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(ride.clientName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        ride.pickupAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                if (ride.destinationAddress.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            ride.destinationAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = {},
                    label = { Text("⏰ Rappel ${ride.reminderMinutesBefore} min avant", style = MaterialTheme.typography.labelSmall) }
                )
            }

            if (ride.status == RideStatus.PLANIFIEE) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onComplete) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Terminée", tint = SuccessGreen)
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Annuler", tint = DangerRed)
                    }
                }
            } else {
                StatusBadge(ride.status)
            }
        }

        // Itinéraire GPS : le chauffeur choisit son appli de navigation (Google Maps, Waze, etc.)
        if (ride.status == RideStatus.PLANIFIEE || ride.status == RideStatus.EN_COURS) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ride.pickupAddress.isNotBlank()) {
                    OutlinedButton(onClick = { MapsLauncher.openNavigation(context, ride.pickupAddress) }) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vers le client", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (ride.destinationAddress.isNotBlank()) {
                    OutlinedButton(onClick = { MapsLauncher.openNavigation(context, ride.destinationAddress) }) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vers destination", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
      }
    }
}

@Composable
fun StatusBadge(status: RideStatus) {
    val (label, color) = when (status) {
        RideStatus.PLANIFIEE -> "Planifiée" to TaxiBlue
        RideStatus.EN_COURS -> "En cours" to WarningOrange
        RideStatus.TERMINEE -> "Terminée" to SuccessGreen
        RideStatus.ANNULEE -> "Annulée" to DangerRed
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

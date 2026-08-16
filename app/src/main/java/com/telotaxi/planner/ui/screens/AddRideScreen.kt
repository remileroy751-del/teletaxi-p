package com.telotaxi.planner.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.maps.MapsLauncher
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRideScreen(
    existingRide: Ride?,
    onSave: (Ride) -> Unit,
    onDelete: ((Ride) -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var clientName by remember { mutableStateOf(existingRide?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(existingRide?.clientPhone ?: "") }
    var pickupAddress by remember { mutableStateOf(existingRide?.pickupAddress ?: "") }
    var destinationAddress by remember { mutableStateOf(existingRide?.destinationAddress ?: "") }
    var notes by remember { mutableStateOf(existingRide?.notes ?: "") }
    var priceText by remember { mutableStateOf(existingRide?.price?.toString() ?: "") }
    var reminderMinutes by remember { mutableIntStateOf(existingRide?.reminderMinutesBefore ?: 15) }

    val calendar = remember {
        Calendar.getInstance().apply {
            existingRide?.let { timeInMillis = it.dateTimeMillis } ?: add(Calendar.HOUR_OF_DAY, 1)
        }
    }
    var selectedMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    val dateFormat = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                cal.set(year, month, day)
                selectedMillis = cal.timeInMillis
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        TimePickerDialog(
            context,
            { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                selectedMillis = cal.timeInMillis
            },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingRide == null) "Nouvelle course" else "Modifier la course") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = clientName, onValueChange = { clientName = it },
                label = { Text("Nom du client *") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = clientPhone, onValueChange = { clientPhone = it },
                label = { Text("Téléphone du client") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = pickupAddress, onValueChange = { pickupAddress = it },
                label = { Text("Adresse de prise en charge *") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destinationAddress, onValueChange = { destinationAddress = it },
                label = { Text("Destination") }, modifier = Modifier.fillMaxWidth()
            )

            if (existingRide != null && (pickupAddress.isNotBlank() || destinationAddress.isNotBlank())) {
                Text("Itinéraire GPS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (pickupAddress.isNotBlank()) {
                        OutlinedButton(
                            onClick = { MapsLauncher.openNavigation(context, pickupAddress) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Vers le client")
                        }
                    }
                    if (destinationAddress.isNotBlank()) {
                        OutlinedButton(
                            onClick = { MapsLauncher.openNavigation(context, destinationAddress) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Vers destination")
                        }
                    }
                }
            }

            Text("Date et heure de la course", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { openDatePicker() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(dateFormat.format(selectedMillis))
                }
                OutlinedButton(onClick = { openTimePicker() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(timeFormat.format(selectedMillis))
                }
            }

            Text("Alerte de rappel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    FilterChip(
                        selected = reminderMinutes == minutes,
                        onClick = { reminderMinutes = minutes },
                        label = { Text("$minutes min") }
                    )
                }
            }

            OutlinedTextField(
                value = priceText, onValueChange = { priceText = it },
                label = { Text("Prix estimé (optionnel)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes (bagages, accès, préférences…)") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val ride = (existingRide ?: Ride(
                        clientName = "", clientPhone = "", pickupAddress = "",
                        destinationAddress = "", dateTimeMillis = 0
                    )).copy(
                        clientName = clientName.trim(),
                        clientPhone = clientPhone.trim(),
                        pickupAddress = pickupAddress.trim(),
                        destinationAddress = destinationAddress.trim(),
                        dateTimeMillis = selectedMillis,
                        reminderMinutesBefore = reminderMinutes,
                        notes = notes.trim(),
                        price = priceText.toDoubleOrNull()
                    )
                    onSave(ride)
                },
                enabled = clientName.isNotBlank() && pickupAddress.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (existingRide == null) "Programmer la course" else "Enregistrer les modifications")
            }

            if (existingRide != null && onDelete != null) {
                OutlinedButton(
                    onClick = { onDelete(existingRide) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer la course")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

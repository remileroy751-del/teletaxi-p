package com.telotaxi.planner.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telotaxi.planner.data.RideStatus
import com.telotaxi.planner.reports.ReportPdfExporter
import com.telotaxi.planner.ui.PlannerViewModel
import com.telotaxi.planner.ui.theme.SuccessGreen
import com.telotaxi.planner.ui.theme.TaxiBlue
import com.telotaxi.planner.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class PeriodMode { SEMAINE, PERSONNALISE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: PlannerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val allRides by viewModel.allRides.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

    var mode by remember { mutableStateOf(PeriodMode.SEMAINE) }

    // Bornes de la semaine en cours (lundi -> dimanche)
    fun startOfWeekMillis(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    var startMillis by remember { mutableLongStateOf(startOfWeekMillis()) }
    var endMillis by remember {
        mutableLongStateOf(
            Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }.timeInMillis
        )
    }

    fun openDatePicker(isStart: Boolean) {
        val cal = Calendar.getInstance().apply { timeInMillis = if (isStart) startMillis else endMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                cal.set(year, month, day)
                if (isStart) {
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                    startMillis = cal.timeInMillis
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                    endMillis = cal.timeInMillis
                }
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val effectiveStart = if (mode == PeriodMode.SEMAINE) startOfWeekMillis() else startMillis
    val effectiveEnd = if (mode == PeriodMode.SEMAINE) System.currentTimeMillis() else endMillis

    val ridesInPeriod = remember(allRides, effectiveStart, effectiveEnd, mode) {
        allRides.filter {
            it.status == RideStatus.TERMINEE &&
                it.dateTimeMillis in effectiveStart..effectiveEnd
        }
    }
    val totalRevenue = ridesInPeriod.sumOf { it.price ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rapports de chiffre d'affaires") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Choix du mode de période
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = mode == PeriodMode.SEMAINE,
                    onClick = { mode = PeriodMode.SEMAINE },
                    label = { Text("Cette semaine") }
                )
                FilterChip(
                    selected = mode == PeriodMode.PERSONNALISE,
                    onClick = { mode = PeriodMode.PERSONNALISE },
                    label = { Text("Période personnalisée") }
                )
            }

            if (mode == PeriodMode.PERSONNALISE) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { openDatePicker(true) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Du ${dateFormat.format(startMillis)}")
                    }
                    OutlinedButton(onClick = { openDatePicker(false) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Au ${dateFormat.format(endMillis)}")
                    }
                }
            } else {
                Text(
                    "Du ${dateFormat.format(startOfWeekMillis())} à aujourd'hui",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Carte de synthèse
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = TaxiBlue)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Chiffre d'affaires total", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
                    Text(
                        "${"%.0f".format(totalRevenue)} FCFA",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${ridesInPeriod.size} course(s) terminée(s) sur la période",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    val label = if (mode == PeriodMode.SEMAINE)
                        "Semaine du ${dateFormat.format(startOfWeekMillis())} à aujourd'hui"
                    else
                        "Période du ${dateFormat.format(startMillis)} au ${dateFormat.format(endMillis)}"

                    val path = ReportPdfExporter.export(context, ridesInPeriod, label)
                    if (path != null) {
                        Toast.makeText(context, "Rapport PDF enregistré dans Téléchargements", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Erreur lors de la génération du PDF", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exporter en PDF")
            }

            Text("Détail des courses", style = MaterialTheme.typography.titleLarge)
            if (ridesInPeriod.isEmpty()) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Aucune course terminée sur cette période", color = TextSecondary)
                    }
                }
            } else {
                ridesInPeriod.sortedByDescending { it.dateTimeMillis }.forEach { ride ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(ride.clientName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(ride.dateTimeMillis),
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                ride.price?.let { "${"%.0f".format(it)} FCFA" } ?: "—",
                                fontWeight = FontWeight.Bold,
                                color = TaxiBlue
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

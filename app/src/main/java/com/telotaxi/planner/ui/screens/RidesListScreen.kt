package com.telotaxi.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telotaxi.planner.ui.PlannerViewModel

@Composable
fun RidesListScreen(viewModel: PlannerViewModel, onOpenRide: (Long) -> Unit) {
    viewModel.allRides.collectAsStateWithLifecycle().value // force recomposition sur changement
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aujourd'hui", "Demain", "À venir")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Mes courses",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
            }
        }

        val rides = when (tabIndex) {
            0 -> viewModel.todayRides()
            1 -> viewModel.tomorrowRides()
            else -> viewModel.upcomingAfterTomorrow()
        }

        if (rides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Aucune course dans cette catégorie")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rides, key = { it.id }) { ride ->
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

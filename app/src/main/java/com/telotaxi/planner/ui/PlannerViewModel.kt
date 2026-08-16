package com.telotaxi.planner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.data.RideRepository
import com.telotaxi.planner.data.RideStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val rideRepository = RideRepository(application)

    private val _allRides = MutableStateFlow<List<Ride>>(emptyList())
    val allRides: StateFlow<List<Ride>> = _allRides.asStateFlow()

    init {
        viewModelScope.launch {
            rideRepository.getAllRides().collect { rides ->
                _allRides.value = rides
            }
        }
    }

    fun todayRides(): List<Ride> {
        val (start, end) = dayBounds(0)
        return _allRides.value.filter { it.dateTimeMillis in start until end && it.status != RideStatus.ANNULEE }
    }

    fun tomorrowRides(): List<Ride> {
        val (start, end) = dayBounds(1)
        return _allRides.value.filter { it.dateTimeMillis in start until end && it.status != RideStatus.ANNULEE }
    }

    fun upcomingAfterTomorrow(): List<Ride> {
        val (_, endTomorrow) = dayBounds(1)
        return _allRides.value.filter { it.dateTimeMillis >= endTomorrow && it.status != RideStatus.ANNULEE }
    }

    private fun dayBounds(offsetDays: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }

    fun addRide(ride: Ride) = viewModelScope.launch { rideRepository.addRide(ride) }
    fun updateRide(ride: Ride) = viewModelScope.launch { rideRepository.updateRide(ride) }
    fun deleteRide(ride: Ride) = viewModelScope.launch { rideRepository.deleteRide(ride) }
    fun markCompleted(ride: Ride) = viewModelScope.launch { rideRepository.updateRide(ride.copy(status = RideStatus.TERMINEE)) }
    fun markCancelled(ride: Ride) = viewModelScope.launch { rideRepository.updateRide(ride.copy(status = RideStatus.ANNULEE)) }
}

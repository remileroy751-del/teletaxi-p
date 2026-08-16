package com.telotaxi.planner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.telotaxi.planner.data.Ride
import com.telotaxi.planner.data.RideRepository
import com.telotaxi.planner.data.RideStatus
import com.telotaxi.planner.weather.WeatherRepository
import com.telotaxi.planner.weather.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherResponse, val locationLabel: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
    object PermissionNeeded : WeatherUiState()
}

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val rideRepository = RideRepository(application)
    private val weatherRepository = WeatherRepository(application)

    private val _allRides = MutableStateFlow<List<Ride>>(emptyList())
    val allRides: StateFlow<List<Ride>> = _allRides.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

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

    fun loadWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            try {
                val location = weatherRepository.getCurrentLocation()
                if (location == null) {
                    _weatherState.value = WeatherUiState.Error("Position GPS indisponible. Vérifiez que la localisation est activée.")
                    return@launch
                }
                val forecast = weatherRepository.getForecast(location.first, location.second)
                _weatherState.value = WeatherUiState.Success(
                    forecast,
                    "Lat ${"%.2f".format(location.first)}, Lon ${"%.2f".format(location.second)}"
                )
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(e.message ?: "Erreur de chargement de la météo")
            }
        }
    }

    fun notifyPermissionNeeded() {
        _weatherState.value = WeatherUiState.PermissionNeeded
    }
}

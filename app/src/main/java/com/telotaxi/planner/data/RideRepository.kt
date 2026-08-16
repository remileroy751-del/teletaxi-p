package com.telotaxi.planner.data

import android.content.Context
import com.telotaxi.planner.alarm.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class RideRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).rideDao()
    private val alarmScheduler = AlarmScheduler(context)

    fun getAllRides(): Flow<List<Ride>> = dao.getAllRides()

    fun getRidesBetween(start: Long, end: Long): Flow<List<Ride>> = dao.getRidesBetween(start, end)

    suspend fun addRide(ride: Ride): Long {
        val id = dao.insert(ride)
        val saved = ride.copy(id = id)
        alarmScheduler.scheduleRideReminder(saved)
        dao.update(saved.copy(alarmScheduled = true))
        return id
    }

    suspend fun updateRide(ride: Ride) {
        // On annule l'ancienne alarme puis on reprogramme avec les nouvelles infos
        alarmScheduler.cancelRideReminder(ride)
        dao.update(ride.copy(alarmScheduled = false))
        if (ride.status == RideStatus.PLANIFIEE) {
            alarmScheduler.scheduleRideReminder(ride)
            dao.update(ride.copy(alarmScheduled = true))
        }
    }

    suspend fun deleteRide(ride: Ride) {
        alarmScheduler.cancelRideReminder(ride)
        dao.delete(ride)
    }

    suspend fun getRideById(id: Long) = dao.getRideById(id)

    suspend fun rescheduleAllPendingAlarms(now: Long) {
        val rides = dao.getRidesNeedingAlarm(now)
        rides.forEach { ride ->
            alarmScheduler.scheduleRideReminder(ride)
            dao.update(ride.copy(alarmScheduled = true))
        }
    }
}

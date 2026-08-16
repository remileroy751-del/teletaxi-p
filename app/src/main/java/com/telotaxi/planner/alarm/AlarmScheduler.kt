package com.telotaxi.planner.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.telotaxi.planner.data.Ride

/**
 * Programme et annule les alarmes de rappel de course.
 * L'alarme se déclenche X minutes avant l'heure de la course (reminderMinutesBefore),
 * réveillant le téléphone même s'il est en veille (setExactAndAllowWhileIdle).
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRideReminder(ride: Ride) {
        val triggerAtMillis = ride.dateTimeMillis - (ride.reminderMinutesBefore * 60_000L)
        // Si l'heure de rappel est déjà passée, on ne programme rien
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, RideAlarmReceiver::class.java).apply {
            putExtra(RideAlarmReceiver.EXTRA_RIDE_ID, ride.id)
            putExtra(RideAlarmReceiver.EXTRA_CLIENT_NAME, ride.clientName)
            putExtra(RideAlarmReceiver.EXTRA_PICKUP, ride.pickupAddress)
            putExtra(RideAlarmReceiver.EXTRA_DESTINATION, ride.destinationAddress)
            putExtra(RideAlarmReceiver.EXTRA_RIDE_TIME, ride.dateTimeMillis)
            putExtra(RideAlarmReceiver.EXTRA_MINUTES_BEFORE, ride.reminderMinutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ride.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                // Repli si la permission d'alarme exacte n'a pas été accordée
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelRideReminder(ride: Ride) {
        val intent = Intent(context, RideAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ride.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

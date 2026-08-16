package com.telotaxi.planner.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.telotaxi.planner.MainActivity
import com.telotaxi.planner.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Reçu lorsqu'une alarme de rappel de course se déclenche.
 * Affiche une notification prioritaire avec son d'alarme, vibration,
 * et les informations essentielles pour que le chauffeur agisse immédiatement.
 */
class RideAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_RIDE_ID = "extra_ride_id"
        const val EXTRA_CLIENT_NAME = "extra_client_name"
        const val EXTRA_PICKUP = "extra_pickup"
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_RIDE_TIME = "extra_ride_time"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val CHANNEL_ID = "ride_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val rideId = intent.getLongExtra(EXTRA_RIDE_ID, -1)
        val clientName = intent.getStringExtra(EXTRA_CLIENT_NAME) ?: "Client"
        val pickup = intent.getStringExtra(EXTRA_PICKUP) ?: ""
        val destination = intent.getStringExtra(EXTRA_DESTINATION) ?: ""
        val rideTime = intent.getLongExtra(EXTRA_RIDE_TIME, System.currentTimeMillis())
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 15)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val heureCourse = timeFormat.format(rideTime)

        createChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_ride_id", rideId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, rideId.toInt(), openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("🚕 Course dans $minutesBefore min — $clientName")
            .setContentText("$heureCourse • Départ : $pickup")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Client : $clientName\nHeure de prise en charge : $heureCourse\nDépart : $pickup\nDestination : $destination"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setAutoCancel(true)
            .setFullScreenIntent(contentPendingIntent, true)
            .setContentIntent(contentPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(rideId.toInt(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.alarm_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.alarm_channel_desc)
                    enableVibration(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        audioAttributes
                    )
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}

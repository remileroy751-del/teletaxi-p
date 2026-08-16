package com.telotaxi.planner.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.telotaxi.planner.data.RideRepository
import kotlinx.coroutines.runBlocking

/**
 * Après un redémarrage du téléphone, toutes les alarmes AlarmManager sont perdues.
 * Ce receiver relance une tâche qui reprogramme les rappels des courses à venir.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val request = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

class RescheduleAlarmsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val repository = RideRepository(applicationContext)
        runBlocking {
            repository.rescheduleAllPendingAlarms(System.currentTimeMillis())
        }
        return Result.success()
    }
}

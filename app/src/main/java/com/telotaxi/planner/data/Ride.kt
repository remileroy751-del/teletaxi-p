package com.telotaxi.planner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Représente une course programmée avec un client.
 * dateTimeMillis = date et heure exactes de prise en charge du client.
 * reminderMinutesBefore = délai d'alerte avant la course (15 ou 30 minutes en général).
 */
@Entity(tableName = "rides")
data class Ride(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val clientPhone: String,
    val pickupAddress: String,
    val destinationAddress: String,
    val dateTimeMillis: Long,
    val reminderMinutesBefore: Int = 15,
    val notes: String = "",
    val price: Double? = null,
    val status: RideStatus = RideStatus.PLANIFIEE,
    val alarmScheduled: Boolean = false
)

enum class RideStatus {
    PLANIFIEE,
    EN_COURS,
    TERMINEE,
    ANNULEE
}

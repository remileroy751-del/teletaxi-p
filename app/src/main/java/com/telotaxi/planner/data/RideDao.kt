package com.telotaxi.planner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {

    @Query("SELECT * FROM rides ORDER BY dateTimeMillis ASC")
    fun getAllRides(): Flow<List<Ride>>

    @Query("SELECT * FROM rides WHERE dateTimeMillis BETWEEN :startMillis AND :endMillis AND status != 'ANNULEE' ORDER BY dateTimeMillis ASC")
    fun getRidesBetween(startMillis: Long, endMillis: Long): Flow<List<Ride>>

    @Query("SELECT * FROM rides WHERE dateTimeMillis >= :fromMillis AND status = 'PLANIFIEE' ORDER BY dateTimeMillis ASC")
    fun getUpcomingRides(fromMillis: Long): Flow<List<Ride>>

    @Query("SELECT * FROM rides WHERE id = :id")
    suspend fun getRideById(id: Long): Ride?

    @Insert
    suspend fun insert(ride: Ride): Long

    @Update
    suspend fun update(ride: Ride)

    @Delete
    suspend fun delete(ride: Ride)

    @Query("SELECT * FROM rides WHERE alarmScheduled = 0 AND status = 'PLANIFIEE' AND dateTimeMillis > :now")
    suspend fun getRidesNeedingAlarm(now: Long): List<Ride>
}

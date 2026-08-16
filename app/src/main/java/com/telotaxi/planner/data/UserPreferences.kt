package com.telotaxi.planner.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stocke localement (SharedPreferences) le nom du chauffeur et la date
 * du dernier message de bienvenue affiché, pour ne le montrer qu'une fois par jour.
 */
object UserPreferences {
    private const val PREFS_NAME = "teletaxi_prefs"
    private const val KEY_DRIVER_NAME = "driver_name"
    private const val KEY_LAST_GREETING_DATE = "last_greeting_date"

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDriverName(context: Context): String? =
        prefs(context).getString(KEY_DRIVER_NAME, null)

    fun setDriverName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_DRIVER_NAME, name.trim()).apply()
    }

    fun hasDriverName(context: Context): Boolean = !getDriverName(context).isNullOrBlank()

    /** Vrai si le message de salutation n'a pas encore été montré aujourd'hui. */
    fun shouldShowGreetingToday(context: Context): Boolean {
        val today = dayFormat.format(Date())
        val lastShown = prefs(context).getString(KEY_LAST_GREETING_DATE, null)
        return lastShown != today
    }

    fun markGreetingShownToday(context: Context) {
        val today = dayFormat.format(Date())
        prefs(context).edit().putString(KEY_LAST_GREETING_DATE, today).apply()
    }
}

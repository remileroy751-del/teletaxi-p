package com.telotaxi.planner.maps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Lance l'itinéraire GPS vers une adresse en laissant le chauffeur choisir
 * son application de navigation préférée (Google Maps, Waze, etc.),
 * grâce à un intent générique "geo:" reconnu par toutes les applis de cartes.
 * Si aucune application de navigation n'est installée, on ouvre l'itinéraire dans le navigateur.
 */
object MapsLauncher {

    fun openNavigation(context: Context, address: String) {
        if (address.isBlank()) return
        try {
            val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            val chooser = Intent.createChooser(mapIntent, "Naviguer avec…").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            openInBrowser(context, address)
        }
    }

    private fun openInBrowser(context: Context, address: String) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(address)}&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, webUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }

    /** Ouvre une conversation WhatsApp avec le numéro de support (format international sans '+' ni espaces). */
    fun openWhatsApp(context: Context, phoneInternational: String) {
        try {
            val uri = Uri.parse("https://wa.me/$phoneInternational")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Aucune application ne peut ouvrir le lien, on ignore silencieusement
        }
    }
}

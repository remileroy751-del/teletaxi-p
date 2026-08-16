package com.telotaxi.planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telotaxi.planner.maps.MapsLauncher
import com.telotaxi.planner.ui.theme.TaxiBlue
import com.telotaxi.planner.ui.theme.TextSecondary

private const val SUPPORT_WHATSAPP_NUMBER = "22899373635" // format international sans '+'

/**
 * Pied de page affiché tout en bas de l'application, sur tous les écrans principaux.
 * Le numéro WhatsApp est cliquable et ouvre directement une conversation.
 */
@Composable
fun AppFooter() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "App créée par Ecom Academy. Support WhatsApp ",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = "+228 99 37 36 35",
            style = MaterialTheme.typography.labelSmall,
            color = TaxiBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                MapsLauncher.openWhatsApp(context, SUPPORT_WHATSAPP_NUMBER)
            }
        )
    }
}

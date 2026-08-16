package com.telotaxi.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.telotaxi.planner.ui.theme.TaxiBlue
import com.telotaxi.planner.ui.theme.TextSecondary

/**
 * Affiché uniquement lors du tout premier lancement de l'application.
 * Le champ nom et prénom est obligatoire : impossible de continuer sans le renseigner.
 */
@Composable
fun WelcomeScreen(onNameConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val isValid = name.trim().length >= 2

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LocalTaxi,
                contentDescription = null,
                tint = TaxiBlue,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Bienvenue sur TéléTaxi Planner",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Pour personnaliser votre expérience, indiquez votre nom et prénom.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom et prénom *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (!isValid && name.isNotEmpty()) {
                        Text("Merci de saisir au moins 2 caractères")
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onNameConfirmed(name.trim()) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Commencer")
            }
        }
    }
}

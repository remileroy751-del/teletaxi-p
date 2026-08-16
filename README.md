# TéléTaxi Planner 🚕

Application Android native (Kotlin + Jetpack Compose) pour chauffeurs de télé-taxi / VTC : planification des courses, rappels d'alarme automatiques, et météo géolocalisée.

## Fonctionnalités

- **Tableau de bord** : vue d'ensemble avec statistiques du jour (courses du jour, de demain, terminées), aperçu météo, et accès rapide.
- **Planification des courses** : nom du client, téléphone, adresse de départ/destination, date/heure, prix estimé, notes.
- **Alarmes de rappel automatiques** : choix du délai (15, 30, 45 ou 60 min avant la course). Utilise `AlarmManager.setExactAndAllowWhileIdle` pour se déclencher même téléphone en veille, avec notification sonore + vibration + écran de rappel prioritaire.
- **Reprogrammation après redémarrage** : un `BroadcastReceiver` + `WorkManager` reprogramment automatiquement toutes les alarmes en attente si le téléphone redémarre.
- **Liste des courses** : onglets Aujourd'hui / Demain / À venir, avec actions rapides (terminer, annuler).
- **Rapports de chiffre d'affaires** : filtrage par semaine en cours ou période personnalisée (date à date), avec total du chiffre d'affaires, nombre de courses terminées, détail course par course, et **export PDF** en un clic (enregistré dans le dossier Téléchargements du téléphone).
- **Pied de page** : "App créée par Ecom Academy. Support WhatsApp +228 99 37 36 35" visible en bas de l'application, avec lien WhatsApp cliquable.

> Note : la météo géolocalisée a été retirée temporairement pour simplifier la compilation. Elle pourra être réintégrée plus tard si besoin.

## Architecture technique

```
com.telotaxi.planner
├── data/              → Room (Ride, RideDao, AppDatabase, RideRepository) + UserPreferences (nom du chauffeur, salutation du jour)
├── alarm/              → AlarmScheduler, RideAlarmReceiver, BootReceiver (rappels programmés)
├── maps/               → MapsLauncher (itinéraire GPS, ouverture WhatsApp)
├── reports/            → ReportPdfExporter (génération PDF native, sans dépendance)
└── ui/                 → PlannerViewModel + écrans Compose (Bienvenue, Dashboard, Courses, Ajout, Rapports)
```

- **Stockage local** : Room (SQLite), aucune donnée envoyée en ligne — tout reste sur le téléphone du chauffeur.
- **Design** : Material 3, palette bleu/jaune inspirée du secteur taxi, cartes arrondies, gros boutons lisibles au volant.
- **Minimum SDK** : Android 8.0 (API 26) — compatible avec la quasi-totalité des téléphones en circulation.

## Installation / compilation

1. Ouvrez le dossier `TeleTaxiPlanner` dans **Android Studio** (version Koala ou plus récente recommandée).
2. Laissez Android Studio synchroniser Gradle (les dépendances se téléchargent automatiquement).
3. Branchez un téléphone Android (ou utilisez un émulateur) et cliquez sur **Run ▶**.
4. Au premier lancement, autorisez :
   - la **localisation** (pour la météo),
   - les **notifications** (pour les rappels, Android 13+),
   - les **alarmes exactes** (l'app vous redirige automatiquement vers le réglage système si besoin).

> Ce projet est fourni en code source complet (pas de clé API à configurer — Open-Meteo est gratuit et sans authentification). Pour publier l'app sur le Play Store, il faudra générer un APK/AAB signé depuis Android Studio (Build > Generate Signed Bundle).

## Pistes d'évolution possibles

- Synchronisation cloud multi-appareils (Firebase) pour les groupements de plusieurs chauffeurs.
- Intégration d'un dispatcher central (réception de courses en temps réel, comme évoqué dans les solutions type AppSolu/Anolla).
- Suivi des horaires de vols/trains pour anticiper les courses aéroport/gare.
- Export comptable (courses terminées → facturation).
- Mode carte avec itinéraire intégré (Google Maps / Waze deep link).

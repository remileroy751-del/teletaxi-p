package com.telotaxi.planner.ui.components

/**
 * Génère un message de salutation aléatoire et personnalisé, affiché une seule fois par jour
 * à l'ouverture de l'application. Le premier prénom saisi par le chauffeur est utilisé.
 */
object GreetingMessages {

    private val templates = listOf(
        { name: String -> "Bonjour $name, comment allez-vous ?" },
        { name: String -> "Bonjour $name, comment vous sentez-vous ce matin ?" },
        { name: String -> "Bonjour $name, bien réveillé ce matin ?" },
        { name: String -> "Salut $name ! Prêt pour une belle journée de courses ?" },
        { name: String -> "Bonjour $name, on prend la route en pleine forme ?" },
        { name: String -> "Hello $name, j'espère que vous avez bien dormi !" },
        { name: String -> "Bonjour $name, que la route soit belle aujourd'hui !" },
        { name: String -> "$name, bonjour ! Une nouvelle journée, de nouvelles courses." },
        { name: String -> "Bonjour $name, prudence et bonne route aujourd'hui !" },
        { name: String -> "Bien le bonjour $name, on y va en douceur ce matin ?" }
    )

    fun randomGreeting(driverName: String): String {
        val firstName = driverName.trim().split(" ").firstOrNull()?.takeIf { it.isNotBlank() } ?: driverName
        val template = templates.random()
        return template(firstName)
    }
}

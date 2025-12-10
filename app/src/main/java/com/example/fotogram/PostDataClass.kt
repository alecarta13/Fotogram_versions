package com.example.fotogram.navigator

data class PostDataClass(
    val userName: String,
    val description: String
) {
    // Usa companion object per i dati statici/di esempio
    companion object {
        val Utente1 = PostDataClass("MarioRossi", "Che bella giornata al mare!")
        val Utente2 = PostDataClass("LucaBianchi", "Amo la montagna.")
        val Utente3 = PostDataClass("GiuliaVerdi", "Cena con amici.")

        // Consiglio: Mettili in una lista per poterli scorrere facilmente
        val tuttiIPost = listOf(Utente1, Utente2, Utente3)
    }
}

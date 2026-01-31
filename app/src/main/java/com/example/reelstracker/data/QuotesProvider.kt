// app/src/main/java/com/example/reelstracker/data/QuotesProvider.kt
package com.example.reelstracker.data

object QuotesProvider {

    private val normalQuotes = listOf(
        "You could’ve read 10 pages by now 📚",
        "Discipline beats dopamine.",
        "Your future self is watching 👀",
        "Scrolling is easy. Progress is hard."
    )

    private val stopQuotes = listOf(
        "Enough scrolling. Close Instagram now.",
        "You don’t need this reel.",
        "This reel won’t change your life.",
        "Stop. Breathe. Get back to work.",
        "Your goals are more important than this."
    )

    fun randomQuote(): String = normalQuotes.random()

    fun stopQuotes(): String = stopQuotes.random()
}

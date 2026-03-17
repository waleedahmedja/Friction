package com.waleedahmedja.friction.messages

object CompletionMessages {
    private val messages = listOf(
        "You stayed.",
        "Focused. Finished.",
        "That's what commitment looks like.",
        "The work is done.",
        "Quiet achievement.",
        "You did not break.",
        "Time well kept.",
        "One session. Fully present.",
        "This is how it's built.",
        "No shortcuts taken."
    )
    fun durationLabel(minutes: Int): String = when {
        minutes < 60      -> "$minutes min"
        minutes % 60 == 0 -> "${minutes / 60} hr"
        else              -> "${minutes / 60} hr ${minutes % 60} min"
    }
    fun getForIndex(idx: Int, durationLabel: String): String =
        "${messages[idx.coerceIn(0, messages.size - 1)]}\n$durationLabel of uninterrupted focus."
    fun nextIndex(current: Int): Int = (current + 1) % messages.size
}
package com.waleedahmedja.friction.messages

object ReflectiveMessages {
    private val messages = listOf(
        "Discipline is a decision.",
        "Every interruption has a cost.",
        "The session was yours to keep.",
        "Comfort is the enemy of progress.",
        "Distraction is always a choice.",
        "You set this time aside for a reason.",
        "The urge passes. The work remains.",
        "What you resist, you strengthen.",
        "Consistency is built in moments like this.",
        "The phone will still be there in an hour."
    )
    fun getForIndex(idx: Int): String = messages[idx.coerceIn(0, messages.size - 1)]
    fun nextIndex(current: Int): Int  = (current + 1) % messages.size
}
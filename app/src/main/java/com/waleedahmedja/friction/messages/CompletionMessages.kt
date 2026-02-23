package com.waleedahmedja.friction.messages

object CompletionMessages {

    private val templates = listOf(
        "You stayed away for {duration}.\nThat was intentional.",
        "{duration} of your time, returned to you.",
        "You chose {duration} of silence.\nGood.",
        "The screen waited.\nYou didn't.",
        "{duration} without the pull.\nNotice how that feels.",
        "You locked it for {duration}.\nYou meant it.",
        "The habit expected you.\nYou surprised it. {duration}.",
        "{duration} passed.\nYou were elsewhere. That's the point.",
        "Friction worked.\n{duration} reclaimed.",
        "You set a boundary.\nYou held it for {duration}.",
        "Distance creates clarity.\n{duration} of distance.",
        "Not every moment needs to be filled.\nYou proved that for {duration}.",
        "You resisted for {duration}.\nIt gets easier.",
        "Presence is a practice.\n{duration} of practice.",
        "{duration} belonged to you."
    )

    private val shuffled = mutableListOf<Int>()

    fun getForIndex(index: Int, durationLabel: String): String {
        if (templates.isEmpty()) return "Well done."
        if (shuffled.isEmpty()) reshuffle()
        val safe = index.coerceIn(0, shuffled.size - 1)
        val raw  = templates[shuffled[safe].coerceIn(0, templates.lastIndex)]
        return raw.replace("{duration}", durationLabel)
    }

    fun nextIndex(current: Int): Int {
        if (shuffled.isEmpty()) reshuffle()
        val next = current + 1
        return if (next >= shuffled.size) { reshuffle(); 0 } else next
    }

    private fun reshuffle() {
        shuffled.clear()
        shuffled.addAll(templates.indices.toMutableList().also { it.shuffle() })
    }

    fun durationLabel(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0           -> "${h}h"
            else            -> "${m}m"
        }
    }
}
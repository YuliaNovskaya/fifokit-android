package com.fifokit.app.domain.roster

import java.time.LocalDate

object RosterPhrases {

    val workDayPhrases = listOf(
        "One shift at a time.",
        "Stay focused. Finish strong.",
        "Small progress still counts.",
        "You are building something bigger.",
        "Keep moving forward.",
        "Consistency creates results.",
        "Today’s effort matters.",
        "Do the work. Trust the process.",
        "Strong days build strong futures.",
        "Keep your goal in sight.",
        "Progress is made one day at a time.",
        "Stay steady.",
        "Another day closer to your goal.",
        "Your effort has a purpose.",
        "Focus on what you can control.",
        "Keep showing up.",
        "Finish today well.",
        "Discipline creates freedom.",
        "Your future is being built today.",
        "Make this shift count.",
        "Keep going. You are getting there.",
        "Stay sharp. Stay safe.",
        "One good decision at a time.",
        "The hard days still move you forward.",
        "Work today for the life you want.",
        "Keep the bigger picture in mind.",
        "Every completed shift is progress.",
        "You are closer than yesterday."
    )

    val offDayPhrases = listOf(
        "Rest is part of the plan.",
        "Make today yours.",
        "Recharge properly.",
        "Slow down and enjoy the day.",
        "Your time matters too.",
        "Rest today. Return stronger.",
        "Spend time on what matters.",
        "Enjoy the life you work for.",
        "Take care of yourself today.",
        "A good break makes a better return.",
        "Be present today.",
        "Make space for what matters.",
        "Recovery is productive.",
        "Enjoy the freedom you earned.",
        "Today belongs to you.",
        "Reconnect with life outside work.",
        "Rest without guilt.",
        "Use today well.",
        "Do something that makes you feel alive.",
        "Take time for yourself.",
        "Life is happening now.",
        "Reset your mind.",
        "Enjoy the people around you.",
        "Make a good memory today.",
        "Protect your time off.",
        "Rest, reset, continue.",
        "Give yourself room to breathe.",
        "Enjoy where you are today."
    )

    fun phraseFor(
        date: LocalDate,
        isWorkDay: Boolean
    ): String {
        val phrases = if (isWorkDay) {
            workDayPhrases
        } else {
            offDayPhrases
        }

        val index = Math.floorMod(
            date.toEpochDay(),
            phrases.size.toLong()
        ).toInt()

        return phrases[index]
    }

}
package com.fifokit.app.domain.roster

enum class AustralianState(
    val code: String,
    val displayName: String
) {
    WA("WA", "Western Australia"),
    NSW("NSW", "New South Wales"),
    VIC("VIC", "Victoria"),
    QLD("QLD", "Queensland"),
    SA("SA", "South Australia"),
    TAS("TAS", "Tasmania"),
    ACT("ACT", "Australian Capital Territory"),
    NT("NT", "Northern Territory")
}
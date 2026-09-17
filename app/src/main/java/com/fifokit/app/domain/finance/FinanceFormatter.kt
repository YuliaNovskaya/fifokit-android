package com.fifokit.app.domain.finance

import java.text.NumberFormat
import java.util.Locale

object FinanceFormatter {

    private val moneyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "AU")
        )

    fun money(value: Double): String {
        return moneyFormatter.format(value)
    }
}
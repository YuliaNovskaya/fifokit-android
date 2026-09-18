package com.fifokit.app.domain.pro

data class ProEntitlement(
    val isPro: Boolean = false,
    val productId: String? = null,
    val basePlanId: String? = null,
    val autoRenewing: Boolean = false
)
package com.fifokit.app.domain.pro

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProEntitlementManager {

    private val _entitlement = MutableStateFlow(ProEntitlement())

    val entitlement: StateFlow<ProEntitlement> = _entitlement.asStateFlow()

    val isPro: Boolean
        get() = _entitlement.value.isPro

    fun updateEntitlement(entitlement: ProEntitlement) {
        _entitlement.value = entitlement
    }

    fun hasAccess(feature: ProFeature): Boolean {
        return _entitlement.value.isPro
    }
    fun setDebugPro(enabled: Boolean) {
        _entitlement.value = ProEntitlement(
            isPro = enabled,
            productId = if (enabled) ProProductIds.SUBSCRIPTION_ID else null
        )
    }
}
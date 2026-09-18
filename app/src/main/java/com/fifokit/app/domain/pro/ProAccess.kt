package com.fifokit.app.domain.pro

object ProAccess {

    fun canUse(feature: ProFeature): Boolean {
        return ProEntitlementManager.hasAccess(feature)
    }
}
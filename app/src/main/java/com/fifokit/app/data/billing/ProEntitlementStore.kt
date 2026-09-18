package com.fifokit.app.data.billing

import android.content.Context
import com.fifokit.app.domain.pro.ProEntitlement

class ProEntitlementStore(
    context: Context
) {
    private val preferences =
        context.getSharedPreferences(
            "pro_entitlement",
            Context.MODE_PRIVATE
        )

    fun save(entitlement: ProEntitlement) {
        preferences.edit()
            .putBoolean("is_pro", entitlement.isPro)
            .putString("product_id", entitlement.productId)
            .putString("base_plan_id", entitlement.basePlanId)
            .putBoolean("auto_renewing", entitlement.autoRenewing)
            .apply()
    }

    fun load(): ProEntitlement {
        return ProEntitlement(
            isPro = preferences.getBoolean("is_pro", false),
            productId = preferences.getString("product_id", null),
            basePlanId = preferences.getString("base_plan_id", null),
            autoRenewing = preferences.getBoolean(
                "auto_renewing",
                false
            )
        )
    }
}
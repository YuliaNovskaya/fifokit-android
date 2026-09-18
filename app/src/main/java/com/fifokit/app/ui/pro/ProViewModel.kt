package com.fifokit.app.ui.pro

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fifokit.app.FifokitApplication
import com.fifokit.app.domain.pro.ProEntitlementManager

class ProViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val billingRepository =
        (application as FifokitApplication).billingRepository

    val proPlans = billingRepository.proPlans

    val entitlement = ProEntitlementManager.entitlement

    val purchaseState = billingRepository.purchaseState

    fun purchase(
        activity: Activity,
        basePlanId: String
    ) {
        billingRepository.launchPurchase(
            activity = activity,
            basePlanId = basePlanId
        )
    }
    fun restorePurchases() {
        billingRepository.restorePurchases()
    }

    fun resetPurchaseState() {
        billingRepository.resetPurchaseState()
    }

}
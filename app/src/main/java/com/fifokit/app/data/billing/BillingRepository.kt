package com.fifokit.app.data.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.fifokit.app.domain.pro.ProProductIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.QueryPurchasesParams
import com.fifokit.app.domain.pro.ProEntitlement
import com.fifokit.app.domain.pro.ProEntitlementManager
import android.app.Activity
import com.android.billingclient.api.BillingFlowParams
import com.fifokit.app.domain.pro.ProPlan
import com.fifokit.app.domain.pro.ProPurchaseState
import com.fifokit.app.widgets.RosterWidgetUpdater


class BillingRepository(
    context: Context
) : PurchasesUpdatedListener {

    private val applicationContext =
        context.applicationContext

    private val _proProductDetails = MutableStateFlow<ProductDetails?>(null)

    val proProductDetails: StateFlow<ProductDetails?> =
        _proProductDetails.asStateFlow()

    private val billingClient: BillingClient =
        BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(
            object : BillingClientStateListener {

                override fun onBillingSetupFinished(
                    billingResult: BillingResult
                ) {
                    if (
                        billingResult.responseCode ==
                        BillingClient.BillingResponseCode.OK
                    ) {
                        queryProProduct()
                        queryExistingPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Automatic reconnection is enabled
                }
            }
        )
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        when (billingResult.responseCode) {

            BillingClient.BillingResponseCode.OK -> {

                val purchaseList = purchases.orEmpty()

                if (
                    purchaseList.any {
                        it.purchaseState ==
                                Purchase.PurchaseState.PENDING
                    }
                ) {
                    _purchaseState.value =
                        ProPurchaseState.PENDING
                }

                val completedPurchases =
                    purchaseList.filter {
                        it.purchaseState ==
                                Purchase.PurchaseState.PURCHASED
                    }

                if (completedPurchases.isNotEmpty()) {
                    processPurchases(completedPurchases)

                    _purchaseState.value =
                        ProPurchaseState.PURCHASED
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value =
                    ProPurchaseState.CANCELLED
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryExistingPurchases()
            }

            else -> {
                _purchaseState.value =
                    ProPurchaseState.ERROR
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    private fun queryProProduct() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(ProProductIds.SUBSCRIPTION_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, result ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails =
                    result.productDetailsList.firstOrNull()

                _proProductDetails.value = productDetails

                _proPlans.value =
                    productDetails
                        ?.subscriptionOfferDetails
                        ?.filter { it.offerId == null }
                        ?.mapNotNull { offer ->

                            val price =
                                offer.pricingPhases
                                    .pricingPhaseList
                                    .firstOrNull()
                                    ?.formattedPrice
                                    ?: return@mapNotNull null

                            ProPlan(
                                basePlanId = offer.basePlanId,
                                formattedPrice = price
                            )
                        }
                        ?: emptyList()
            }
        }
    }
    private fun queryExistingPurchases() {

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->

            if (
                billingResult.responseCode ==
                BillingClient.BillingResponseCode.OK
            ) {

                val hasPendingPurchase = purchases.any { purchase ->
                    purchase.products.contains(
                        ProProductIds.SUBSCRIPTION_ID
                    ) &&
                            purchase.purchaseState ==
                            Purchase.PurchaseState.PENDING
                }

                if (hasPendingPurchase) {
                    _purchaseState.value =
                        ProPurchaseState.PENDING
                }

                processPurchases(purchases)
            }
        }
    }
    private fun processPurchases(purchases: List<Purchase>) {

        val proPurchase = purchases.firstOrNull { purchase ->

            purchase.products.contains(
                ProProductIds.SUBSCRIPTION_ID
            ) &&
                    purchase.purchaseState ==
                    Purchase.PurchaseState.PURCHASED
        }

        if (proPurchase != null) {

            updateEntitlement(
                ProEntitlement(
                    isPro = true,
                    productId = ProProductIds.SUBSCRIPTION_ID,
                    autoRenewing = proPurchase.isAutoRenewing
                )
            )

            if (!proPurchase.isAcknowledged) {
                acknowledgePurchase(proPurchase)
            }

        } else {

            updateEntitlement(
                ProEntitlement()
            )
        }
    }
    private fun acknowledgePurchase(purchase: Purchase) {

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) {
            // Purchase acknowledged
        }
    }
    fun launchPurchase(
        activity: Activity,
        basePlanId: String
    ) {
        val productDetails = _proProductDetails.value

        if (productDetails == null) {
            _purchaseState.value = ProPurchaseState.ERROR
        } else {

            val offerDetails = productDetails.subscriptionOfferDetails
                ?.firstOrNull {
                    it.basePlanId == basePlanId &&
                            it.offerId == null
                }

            if (offerDetails == null) {
                _purchaseState.value = ProPurchaseState.ERROR
            } else {

                val productDetailsParams =
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerDetails.offerToken)
                        .build()

                val billingFlowParams =
                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(productDetailsParams)
                        )
                        .build()

                val result = billingClient.launchBillingFlow(
                    activity,
                    billingFlowParams
                )

                if (
                    result.responseCode !=
                    BillingClient.BillingResponseCode.OK
                ) {
                    _purchaseState.value =
                        ProPurchaseState.ERROR
                }
            }
        }
    }
    private val _proPlans = MutableStateFlow<List<ProPlan>>(emptyList())

    val proPlans: StateFlow<List<ProPlan>> =
        _proPlans.asStateFlow()

    fun restorePurchases() {
        if (billingClient.isReady) {
            queryExistingPurchases()
        } else {
            startConnection()
        }
    }
    private val entitlementStore =
        ProEntitlementStore(applicationContext)

    init {
        ProEntitlementManager.updateEntitlement(
            entitlementStore.load()
        )
    }
    private fun updateEntitlement(
        entitlement: ProEntitlement
    ) {
        entitlementStore.save(entitlement)
        ProEntitlementManager.updateEntitlement(entitlement)

        RosterWidgetUpdater.updateAllAsync(
            applicationContext
        )
    }
    private val _purchaseState =
        MutableStateFlow(ProPurchaseState.IDLE)

    val purchaseState: StateFlow<ProPurchaseState> =
        _purchaseState.asStateFlow()

    fun resetPurchaseState() {
        _purchaseState.value = ProPurchaseState.IDLE
    }
}
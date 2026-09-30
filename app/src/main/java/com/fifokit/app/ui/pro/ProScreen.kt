package com.fifokit.app.ui.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fifokit.app.domain.pro.ProProductIds
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import com.fifokit.app.domain.pro.ProPurchaseState
import com.fifokit.app.analytics.AnalyticsEvents
import com.fifokit.app.analytics.AnalyticsParams
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProScreen(
    onBack: () -> Unit,
    viewModel: ProViewModel = viewModel()
) {
    val context = LocalContext.current

    val activity = context.findActivity()

    val analytics = FirebaseAnalytics.getInstance(context)

    LaunchedEffect(Unit) {
        analytics.logEvent(AnalyticsEvents.PRO_PAYWALL_VIEWED) {}
    }

    val plans by viewModel.proPlans.collectAsState()

    val entitlement by viewModel.entitlement.collectAsState()

    val purchaseState by viewModel.purchaseState.collectAsState()

    LaunchedEffect(purchaseState) {
        when (purchaseState) {

            ProPurchaseState.PURCHASED -> {
                analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_SUCCESS) {}
                delay(2500)
                viewModel.resetPurchaseState()
            }

            ProPurchaseState.CANCELLED -> {
                analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_CANCELLED) {}
                delay(2500)
                viewModel.resetPurchaseState()
            }

            ProPurchaseState.ERROR -> {
                analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_ERROR) {}
                delay(2500)
                viewModel.resetPurchaseState()
            }

            ProPurchaseState.PENDING -> {
                analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_PENDING) {}
            }

            ProPurchaseState.IDLE -> Unit
        }
    }

    val monthlyPlan = plans.firstOrNull {
        it.basePlanId == ProProductIds.MONTHLY_BASE_PLAN_ID
    }

    val annualPlan = plans.firstOrNull {
        it.basePlanId == ProProductIds.ANNUAL_BASE_PLAN_ID
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("FIFOKIT Pro")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Upgrade to FIFOKIT Pro")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Unlock Pro features:")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Multiple rosters")
            Text("Custom roster patterns")
            Text("Yearly roster view")
            Text("Advanced reminders")
            Text("Detailed financial tools")
            Text("Job and pay comparison")
            Text("Saved financial scenarios")

            Spacer(modifier = Modifier.height(24.dp))

            if (entitlement.isPro) {

                Text("FIFOKIT Pro is active")

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val url =
                            "https://play.google.com/store/account/subscriptions" +
                                    "?sku=${ProProductIds.SUBSCRIPTION_ID}" +
                                    "&package=${context.packageName}"

                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                            )
                        )
                    }
                ) {
                    Text("Manage subscription")
                }

                Spacer(modifier = Modifier.height(12.dp))

            } else {
                when (purchaseState) {

                    ProPurchaseState.PENDING -> {
                        Text("Purchase pending")
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ProPurchaseState.CANCELLED -> {
                        Text("Purchase cancelled")
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ProPurchaseState.ERROR -> {
                        Text("Unable to complete purchase. Please try again.")
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ProPurchaseState.PURCHASED -> {
                        Text("FIFOKIT Pro activated")
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ProPurchaseState.IDLE -> Unit
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = monthlyPlan != null && activity != null,
                    onClick = {
                        activity?.let {

                            analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_STARTED) {
                                param(AnalyticsParams.PLAN, "monthly")
                            }

                            viewModel.purchase(
                                activity = it,
                                basePlanId = ProProductIds.MONTHLY_BASE_PLAN_ID
                            )
                        }
                    }
                ) {
                    Text(
                        "Monthly - ${monthlyPlan?.formattedPrice ?: "A$4.99"}"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = annualPlan != null && activity != null,
                    onClick = {
                        activity?.let {

                            analytics.logEvent(AnalyticsEvents.PRO_PURCHASE_STARTED) {
                                param(AnalyticsParams.PLAN, "annual")
                            }

                            viewModel.purchase(
                                activity = it,
                                basePlanId = ProProductIds.ANNUAL_BASE_PLAN_ID
                            )
                        }
                    }
                ) {
                    Text(
                        "Annual - ${annualPlan?.formattedPrice ?: "A$39.99"}"
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        analytics.logEvent(AnalyticsEvents.PRO_RESTORE_TAPPED) {}
                        viewModel.restorePurchases()
                    }
                ) {
                    Text("Restore purchases")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Subscriptions renew automatically unless cancelled in Google Play."
            )

        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this

    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }

        context = context.baseContext
    }

    return null
}
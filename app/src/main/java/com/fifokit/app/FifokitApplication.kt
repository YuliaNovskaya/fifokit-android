package com.fifokit.app

import android.app.Application
import com.fifokit.app.data.billing.BillingRepository

class FifokitApplication : Application() {

    lateinit var billingRepository: BillingRepository
        private set

    override fun onCreate() {
        super.onCreate()

        billingRepository = BillingRepository(this)
        billingRepository.startConnection()
    }
}
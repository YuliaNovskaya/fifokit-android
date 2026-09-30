package com.fifokit.app

import android.app.Application
import com.fifokit.app.data.billing.BillingRepository
import com.fifokit.app.widgets.RosterWidgetObserver

class FifokitApplication : Application() {

    lateinit var billingRepository: BillingRepository
        private set

    private lateinit var rosterWidgetObserver:
            RosterWidgetObserver

    override fun onCreate() {
        super.onCreate()

        billingRepository = BillingRepository(this)

        rosterWidgetObserver =
            RosterWidgetObserver(this)
                .also { it.start() }

        billingRepository.startConnection()
        AppCheckInstaller.install()
    }
}

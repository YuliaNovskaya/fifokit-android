package com.fifokit.app.widgets

import android.content.Context
import com.fifokit.app.data.RosterPreferences
import com.fifokit.app.data.RosterRepository
import com.fifokit.app.data.local.RosterDatabase
import com.fifokit.app.domain.pro.ProEntitlementManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private data class WidgetRefreshKey(
    val rostersHash: Int,
    val activeRosterId: Long?,
    val selectedStates: Set<String>,
    val isPro: Boolean
)

class RosterWidgetObserver(
    context: Context
) {
    private val applicationContext =
        context.applicationContext

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.Default
        )

    private val rosterPreferences =
        RosterPreferences(applicationContext)

    private val rosterRepository =
        RosterRepository(
            RosterDatabase
                .getInstance(applicationContext)
                .rosterDao()
        )

    fun start() {
        scope.launch {
            combine(
                rosterRepository.observeAllRosters(),
                rosterPreferences.activeRosterId,
                rosterPreferences.selectedStates,
                ProEntitlementManager.entitlement
            ) { rosters, activeRosterId, selectedStates, entitlement ->
                WidgetRefreshKey(
                    rostersHash = rosters.hashCode(),
                    activeRosterId = activeRosterId,
                    selectedStates = selectedStates,
                    isPro = entitlement.isPro
                )
            }
                .distinctUntilChanged()
                .collect {
                    RosterWidgetUpdater.updateAll(
                        applicationContext
                    )
                }
        }
    }
}

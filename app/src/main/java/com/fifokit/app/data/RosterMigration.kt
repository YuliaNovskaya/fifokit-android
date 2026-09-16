package com.fifokit.app.data

import kotlinx.coroutines.flow.first

class RosterMigration(
    private val rosterRepository: RosterRepository,
    private val rosterPreferences: RosterPreferences
) {

    suspend fun migrateLegacyRosterIfNeeded() {

        if (rosterPreferences.legacyRosterMigrated.first()) {
            return
        }

        // Protect against creating a duplicate if a previous
        // migration was interrupted after inserting into Room.
        val existingRosters = rosterRepository.getAllRosters()

        if (existingRosters.isNotEmpty()) {
            val activeRosterId =
                rosterPreferences.activeRosterId.first()

            val activeRosterExists =
                activeRosterId != null &&
                        rosterRepository.getRosterById(activeRosterId) != null

            if (!activeRosterExists) {
                rosterPreferences.setActiveRosterId(
                    existingRosters.first().id
                )
            }

            rosterPreferences.markLegacyRosterMigrated()
            return
        }

        val legacyRoster =
            rosterPreferences.savedRoster.first()

        if (legacyRoster != null) {
            val rosterId = rosterRepository.createRoster(
                name = "My Roster",
                pattern = legacyRoster.pattern,
                startDate = legacyRoster.startDate,
                isCustomRoster = legacyRoster.isCustomRoster,
                customWorkDays = legacyRoster.customWorkDays,
                customOffDays = legacyRoster.customOffDays
            )

            rosterPreferences.setActiveRosterId(rosterId)
        }

        rosterPreferences.markLegacyRosterMigrated()
    }
}
package com.fifokit.shared.roster

import platform.Foundation.NSUserDefaults

internal data class IosRosterSnapshot(
    val id: Int,
    val name: String,
    val startDate: RosterStartDate,
    val pattern: RosterPattern,
    val holidayRegion: HolidayRegion
)

/**
 * Local iOS roster storage. On first use, imports the existing single-roster
 * preferences without deleting them.
 */
internal class IosRosterStore(private val preferences: NSUserDefaults) {
    private val idsKey = "com.fifokit.rosters.ids"
    private val activeKey = "com.fifokit.rosters.active"

    fun ensureMigrated(defaultStart: RosterStartDate) {
        if (preferences.stringForKey(idsKey) != null) return
        val snapshot = IosRosterSnapshot(
            id = 1,
            name = preferences.stringForKey("com.fifokit.roster.name") ?: "My Roster",
            startDate = RosterStartDate.parseIso(
                preferences.stringForKey("com.fifokit.roster.startDate") ?: ""
            ) ?: defaultStart,
            pattern = RosterPattern.entries.firstOrNull {
                it.name == preferences.stringForKey("com.fifokit.roster.pattern")
            } ?: RosterPattern.TWO_ONE,
            holidayRegion = HolidayRegion.entries.firstOrNull {
                it.name == preferences.stringForKey("com.fifokit.roster.holidayRegion")
            } ?: HolidayRegion.WA
        )
        preferences.setObject("1", forKey = idsKey)
        preferences.setObject("1", forKey = activeKey)
        save(snapshot)
    }

    fun ids(): List<Int> = preferences.stringForKey(idsKey)
        ?.split(",")?.mapNotNull { it.toIntOrNull() }?.distinct() ?: emptyList()

    fun activeId(): Int = preferences.stringForKey(activeKey)?.toIntOrNull()
        ?.takeIf { it in ids() } ?: ids().first()

    fun load(id: Int): IosRosterSnapshot? {
        if (id !in ids()) return null
        val prefix = "com.fifokit.rosters.$id."
        return IosRosterSnapshot(
            id = id,
            name = preferences.stringForKey(prefix + "name") ?: "My Roster",
            startDate = RosterStartDate.parseIso(preferences.stringForKey(prefix + "start") ?: "")
                ?: RosterStartDate(2026, 11, 1),
            pattern = RosterPattern.entries.firstOrNull {
                it.name == preferences.stringForKey(prefix + "pattern")
            } ?: RosterPattern.TWO_ONE,
            holidayRegion = HolidayRegion.entries.firstOrNull {
                it.name == preferences.stringForKey(prefix + "region")
            } ?: HolidayRegion.WA
        )
    }

    fun save(roster: IosRosterSnapshot) {
        require(roster.id in ids())
        val prefix = "com.fifokit.rosters.${roster.id}."
        preferences.setObject(roster.name, forKey = prefix + "name")
        preferences.setObject(roster.startDate.toIsoString(), forKey = prefix + "start")
        preferences.setObject(roster.pattern.name, forKey = prefix + "pattern")
        preferences.setObject(roster.holidayRegion.name, forKey = prefix + "region")
        if (roster.id == activeId()) mirrorLegacy(roster)
    }

    fun update(
        id: Int,
        name: String? = null,
        startDate: RosterStartDate? = null,
        pattern: RosterPattern? = null,
        holidayRegion: HolidayRegion? = null
    ) {
        val old = load(id) ?: return
        save(old.copy(
            name = name ?: old.name,
            startDate = startDate ?: old.startDate,
            pattern = pattern ?: old.pattern,
            holidayRegion = holidayRegion ?: old.holidayRegion
        ))
    }

    fun activate(id: Int) {
        require(id in ids())
        preferences.setObject(id.toString(), forKey = activeKey)
        load(id)?.let(::mirrorLegacy)
    }

    fun create(startDate: RosterStartDate): IosRosterSnapshot {
        val currentIds = ids()
        val newId = (currentIds.maxOrNull() ?: 0) + 1
        preferences.setObject((currentIds + newId).joinToString(","), forKey = idsKey)
        val roster = IosRosterSnapshot(
            id = newId,
            name = "New Roster",
            startDate = startDate,
            pattern = RosterPattern.TWO_ONE,
            holidayRegion = HolidayRegion.WA
        )
        save(roster)
        activate(newId)
        return roster
    }

    private fun mirrorLegacy(roster: IosRosterSnapshot) {
        preferences.setObject(roster.name, forKey = "com.fifokit.roster.name")
        preferences.setObject(roster.startDate.toIsoString(), forKey = "com.fifokit.roster.startDate")
        preferences.setObject(roster.pattern.name, forKey = "com.fifokit.roster.pattern")
        preferences.setObject(roster.holidayRegion.name, forKey = "com.fifokit.roster.holidayRegion")
    }
}

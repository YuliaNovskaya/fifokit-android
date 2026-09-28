package com.fifokit.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.longPreferencesKey

private val Context.dataStore by preferencesDataStore(
    name = "roster_preferences"
)

data class SavedRoster(
    val pattern: String,
    val startDate: String,
    val isCustomRoster: Boolean,
    val customWorkDays: Int,
    val customOffDays: Int
)

data class ReminderSettings(
    val enabled: Boolean = true,
    val workRemindersEnabled: Boolean = true,
    val offRemindersEnabled: Boolean = true,
    val sharedTimeRemindersEnabled: Boolean = true,
    val hour: Int = 19,
    val minute: Int = 0
)

class RosterPreferences(
    private val context: Context
) {

    private object Keys {
        val PATTERN = stringPreferencesKey("pattern")
        val START_DATE = stringPreferencesKey("start_date")
        val SELECTED_STATES = stringSetPreferencesKey("selected_states")

        val IS_CUSTOM_ROSTER = booleanPreferencesKey("is_custom_roster")
        val CUSTOM_WORK_DAYS = intPreferencesKey("custom_work_days")
        val CUSTOM_OFF_DAYS = intPreferencesKey("custom_off_days")
        val REMINDERS_ENABLED =
            booleanPreferencesKey("reminders_enabled")

        val WORK_REMINDERS_ENABLED =
            booleanPreferencesKey("work_reminders_enabled")

        val OFF_REMINDERS_ENABLED =
            booleanPreferencesKey("off_reminders_enabled")

        val REMINDER_HOUR =
            intPreferencesKey("reminder_hour")

        val REMINDER_MINUTE =
            intPreferencesKey("reminder_minute")

        val ACTIVE_ROSTER_ID =
            longPreferencesKey("active_roster_id")

        val LEGACY_ROSTER_MIGRATED =
            booleanPreferencesKey("legacy_roster_migrated")

        val SETTINGS_UPDATED_AT =
            longPreferencesKey("settings_updated_at")

        val SHARED_TIME_REMINDERS_ENABLED =
            booleanPreferencesKey("shared_time_reminders_enabled")

    }

    val savedRoster: Flow<SavedRoster?> =
        context.dataStore.data.map { preferences ->
            val pattern = preferences[Keys.PATTERN]
            val startDate = preferences[Keys.START_DATE]

            if (pattern != null && startDate != null) {
                SavedRoster(
                    pattern = pattern,
                    startDate = startDate,
                    isCustomRoster = preferences[Keys.IS_CUSTOM_ROSTER] ?: false,
                    customWorkDays = preferences[Keys.CUSTOM_WORK_DAYS] ?: 14,
                    customOffDays = preferences[Keys.CUSTOM_OFF_DAYS] ?: 7
                )
            } else {
                null
            }
        }

    val selectedStates: Flow<Set<String>> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.SELECTED_STATES] ?: setOf("WA")
        }

    val reminderSettings: Flow<ReminderSettings> =
        context.dataStore.data.map { preferences ->
            ReminderSettings(
                enabled = preferences[Keys.REMINDERS_ENABLED] ?: true,
                workRemindersEnabled =
                    preferences[Keys.WORK_REMINDERS_ENABLED] ?: true,
                offRemindersEnabled =
                    preferences[Keys.OFF_REMINDERS_ENABLED] ?: true,
                sharedTimeRemindersEnabled =
                    preferences[Keys.SHARED_TIME_REMINDERS_ENABLED] ?: true,
                hour = preferences[Keys.REMINDER_HOUR] ?: 19,
                minute = preferences[Keys.REMINDER_MINUTE] ?: 0
            )
        }
    val activeRosterId: Flow<Long?> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.ACTIVE_ROSTER_ID]
        }

    val settingsUpdatedAt: Flow<Long> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.SETTINGS_UPDATED_AT] ?: 0L
        }

    val legacyRosterMigrated: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.LEGACY_ROSTER_MIGRATED] ?: false
        }
    suspend fun saveRoster(
        pattern: String,
        startDate: String,
        isCustomRoster: Boolean = false,
        customWorkDays: Int = 14,
        customOffDays: Int = 7
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PATTERN] = pattern
            preferences[Keys.START_DATE] = startDate
            preferences[Keys.IS_CUSTOM_ROSTER] = isCustomRoster
            preferences[Keys.CUSTOM_WORK_DAYS] = customWorkDays
            preferences[Keys.CUSTOM_OFF_DAYS] = customOffDays
        }
    }

    suspend fun saveSelectedStates(
        states: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SELECTED_STATES] = states
            preferences[Keys.SETTINGS_UPDATED_AT] =
                System.currentTimeMillis()
        }
    }

    suspend fun saveReminderSettings(
        settings: ReminderSettings
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.REMINDERS_ENABLED] = settings.enabled
            preferences[Keys.WORK_REMINDERS_ENABLED] =
                settings.workRemindersEnabled
            preferences[Keys.OFF_REMINDERS_ENABLED] =
                settings.offRemindersEnabled
            preferences[Keys.REMINDER_HOUR] = settings.hour
            preferences[Keys.REMINDER_MINUTE] = settings.minute
            preferences[Keys.SETTINGS_UPDATED_AT] =
                System.currentTimeMillis()
            preferences[Keys.SHARED_TIME_REMINDERS_ENABLED] =
                settings.sharedTimeRemindersEnabled
        }
    }

    suspend fun setActiveRosterId(id: Long?) {
        context.dataStore.edit { preferences ->
            if (id == null) {
                preferences.remove(Keys.ACTIVE_ROSTER_ID)
            } else {
                preferences[Keys.ACTIVE_ROSTER_ID] = id
            }
            preferences[Keys.SETTINGS_UPDATED_AT] =
                System.currentTimeMillis()
        }
    }

    suspend fun markLegacyRosterMigrated() {
        context.dataStore.edit { preferences ->
            preferences[Keys.LEGACY_ROSTER_MIGRATED] = true
        }
    }
    suspend fun clearRoster() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.PATTERN)
            preferences.remove(Keys.START_DATE)
            preferences.remove(Keys.IS_CUSTOM_ROSTER)
            preferences.remove(Keys.CUSTOM_WORK_DAYS)
            preferences.remove(Keys.CUSTOM_OFF_DAYS)
        }
    }
    suspend fun applyCloudSettings(
        selectedStates: Set<String>,
        reminderSettings: ReminderSettings,
        activeRosterId: Long?,
        updatedAt: Long
    ) {
        context.dataStore.edit { preferences ->

            preferences[Keys.SELECTED_STATES] =
                selectedStates

            preferences[Keys.REMINDERS_ENABLED] =
                reminderSettings.enabled

            preferences[Keys.WORK_REMINDERS_ENABLED] =
                reminderSettings.workRemindersEnabled

            preferences[Keys.OFF_REMINDERS_ENABLED] =
                reminderSettings.offRemindersEnabled

            preferences[Keys.REMINDER_HOUR] =
                reminderSettings.hour

            preferences[Keys.REMINDER_MINUTE] =
                reminderSettings.minute

            if (activeRosterId == null) {
                preferences.remove(Keys.ACTIVE_ROSTER_ID)
            } else {
                preferences[Keys.ACTIVE_ROSTER_ID] =
                    activeRosterId
            }

            preferences[Keys.SETTINGS_UPDATED_AT] =
                updatedAt
        }
    }

}
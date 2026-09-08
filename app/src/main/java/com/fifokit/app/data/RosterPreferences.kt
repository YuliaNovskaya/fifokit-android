package com.fifokit.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringSetPreferencesKey

private val Context.dataStore by preferencesDataStore(
    name = "roster_preferences"
)

data class SavedRoster(
    val pattern: String,
    val startDate: String
)

class RosterPreferences(
    private val context: Context
) {

    private object Keys {
        val PATTERN = stringPreferencesKey("pattern")
        val START_DATE = stringPreferencesKey("start_date")
        val SELECTED_STATES = stringSetPreferencesKey("selected_states")
    }

    val savedRoster: Flow<SavedRoster?> =
        context.dataStore.data.map { preferences ->
            val pattern = preferences[Keys.PATTERN]
            val startDate = preferences[Keys.START_DATE]

            if (pattern != null && startDate != null) {
                SavedRoster(
                    pattern = pattern,
                    startDate = startDate
                )
            } else {
                null
            }
        }

    val selectedStates: Flow<Set<String>> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.SELECTED_STATES] ?: setOf("WA")
        }

    suspend fun saveRoster(
        pattern: String,
        startDate: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PATTERN] = pattern
            preferences[Keys.START_DATE] = startDate
        }
    }

    suspend fun saveSelectedStates(
        states: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SELECTED_STATES] = states
        }
    }

    suspend fun clearRoster() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.PATTERN)
            preferences.remove(Keys.START_DATE)
        }
    }
}
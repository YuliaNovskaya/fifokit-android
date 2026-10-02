package com.fifokit.app.domain.roster

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class ShutdownPeriod(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Shutdown",
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(!endDate.isBefore(startDate))
    }

    fun contains(
        date: LocalDate
    ): Boolean {
        return !date.isBefore(startDate) &&
                !date.isAfter(endDate)
    }
}

object ShutdownPeriodCodec {

    fun encode(
        shutdowns: List<ShutdownPeriod>
    ): String {
        val array = JSONArray()

        shutdowns.forEach { shutdown ->
            array.put(
                JSONObject()
                    .put("id", shutdown.id)
                    .put("name", shutdown.name)
                    .put(
                        "startDate",
                        shutdown.startDate.toString()
                    )
                    .put(
                        "endDate",
                        shutdown.endDate.toString()
                    )
            )
        }

        return array.toString()
    }

    fun decode(
        value: String?
    ): List<ShutdownPeriod> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }

        return runCatching {
            val array = JSONArray(value)

            buildList {
                for (
                    index in 0 until array.length()
                ) {
                    val item =
                        array.getJSONObject(index)

                    val startDate =
                        LocalDate.parse(
                            item.getString(
                                "startDate"
                            )
                        )

                    val endDate =
                        LocalDate.parse(
                            item.getString(
                                "endDate"
                            )
                        )

                    add(
                        ShutdownPeriod(
                            id =
                                item.optString(
                                    "id",
                                    UUID
                                        .randomUUID()
                                        .toString()
                                ),
                            name =
                                item.optString(
                                    "name",
                                    "Shutdown"
                                ),
                            startDate =
                                startDate,
                            endDate =
                                endDate
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )
    }
}

fun List<ShutdownPeriod>.shutdownOn(
    date: LocalDate
): ShutdownPeriod? {
    return firstOrNull {
        it.contains(date)
    }
}

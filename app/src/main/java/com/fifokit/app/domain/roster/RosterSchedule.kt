package com.fifokit.app.domain.roster

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

enum class RosterSegmentType {
    WORK,
    OFF,
    DAY,
    NIGHT
}

data class RosterScheduleSegment(
    val id: String =
        UUID.randomUUID().toString(),
    val type: RosterSegmentType =
        RosterSegmentType.WORK,
    val days: Int = 1
) {
    val isWork: Boolean
        get() =
            type != RosterSegmentType.OFF
}

data class RosterScheduleStatus(
    val segment: RosterScheduleSegment,
    val segmentIndex: Int,
    val dayInSegment: Int
)

object RosterScheduleCodec {

    fun encode(
        segments: List<RosterScheduleSegment>
    ): String {
        val array = JSONArray()

        segments.forEach { segment ->
            array.put(
                JSONObject()
                    .put("id", segment.id)
                    .put("type", segment.type.name)
                    .put("days", segment.days)
            )
        }

        return array.toString()
    }

    fun decode(
        encoded: String?
    ): List<RosterScheduleSegment> {
        if (encoded.isNullOrBlank()) {
            return emptyList()
        }

        return runCatching {
            val array = JSONArray(encoded)

            buildList {
                for (
                    index in 0 until array.length()
                ) {
                    val item =
                        array.getJSONObject(index)

                    val type =
                        runCatching {
                            RosterSegmentType.valueOf(
                                item.optString(
                                    "type",
                                    RosterSegmentType
                                        .WORK
                                        .name
                                )
                            )
                        }.getOrDefault(
                            RosterSegmentType.WORK
                        )

                    val days =
                        item.optInt(
                            "days",
                            1
                        ).coerceAtLeast(1)

                    add(
                        RosterScheduleSegment(
                            id =
                                item.optString(
                                    "id"
                                ).ifBlank {
                                    UUID
                                        .randomUUID()
                                        .toString()
                                },
                            type = type,
                            days = days
                        )
                    )
                }
            }
        }.getOrDefault(
            emptyList()
        )
    }
}

object RosterScheduleCalculator {

    fun statusOn(
        date: LocalDate,
        startDate: LocalDate,
        segments: List<RosterScheduleSegment>,
        repeat: Boolean
    ): RosterScheduleStatus? {

        if (
            date.isBefore(startDate) ||
            segments.isEmpty()
        ) {
            return null
        }

        val normalized =
            segments.filter {
                it.days > 0
            }

        if (normalized.isEmpty()) {
            return null
        }

        val totalDays =
            normalized.sumOf {
                it.days
            }

        if (totalDays <= 0) {
            return null
        }

        val elapsed =
            ChronoUnit.DAYS.between(
                startDate,
                date
            )

        if (
            !repeat &&
            elapsed >= totalDays
        ) {
            return null
        }

        val position =
            if (repeat) {
                Math.floorMod(
                    elapsed,
                    totalDays.toLong()
                ).toInt()
            } else {
                elapsed.toInt()
            }

        var cursor = 0

        normalized.forEachIndexed {
                index,
                segment ->

            val endExclusive =
                cursor + segment.days

            if (
                position <
                endExclusive
            ) {
                return RosterScheduleStatus(
                    segment = segment,
                    segmentIndex = index,
                    dayInSegment =
                        position -
                                cursor +
                                1
                )
            }

            cursor =
                endExclusive
        }

        return null
    }

    fun isWorkDay(
        date: LocalDate,
        startDate: LocalDate,
        segments: List<RosterScheduleSegment>,
        repeat: Boolean
    ): Boolean {
        return statusOn(
            date = date,
            startDate = startDate,
            segments = segments,
            repeat = repeat
        )?.segment?.isWork == true
    }

    fun endDate(
        startDate: LocalDate,
        segments: List<RosterScheduleSegment>
    ): LocalDate? {
        val totalDays =
            segments
                .filter {
                    it.days > 0
                }
                .sumOf {
                    it.days
                }

        if (totalDays <= 0) {
            return null
        }

        return startDate.plusDays(
            totalDays.toLong() - 1L
        )
    }

    fun legacyRepeatingSequence(
        workDays: Int,
        offDays: Int
    ): List<RosterScheduleSegment> {
        return listOf(
            RosterScheduleSegment(
                type =
                    RosterSegmentType.WORK,
                days =
                    workDays.coerceAtLeast(1)
            ),
            RosterScheduleSegment(
                type =
                    RosterSegmentType.OFF,
                days =
                    offDays.coerceAtLeast(1)
            )
        )
    }

    fun legacyFiniteSequence(
        startDate: LocalDate,
        endDate: LocalDate,
        workDays: Int,
        offDays: Int
    ): List<RosterScheduleSegment> {

        if (endDate.isBefore(startDate)) {
            return emptyList()
        }

        var remaining =
            ChronoUnit.DAYS.between(
                startDate,
                endDate
            ).toInt() + 1

        val result =
            mutableListOf<
                    RosterScheduleSegment
                    >()

        var work = true

        while (remaining > 0) {
            val requested =
                if (work) {
                    workDays
                        .coerceAtLeast(1)
                } else {
                    offDays
                        .coerceAtLeast(1)
                }

            val days =
                minOf(
                    requested,
                    remaining
                )

            result +=
                RosterScheduleSegment(
                    type =
                        if (work) {
                            RosterSegmentType
                                .WORK
                        } else {
                            RosterSegmentType
                                .OFF
                        },
                    days = days
                )

            remaining -= days
            work = !work
        }

        return result
    }
}

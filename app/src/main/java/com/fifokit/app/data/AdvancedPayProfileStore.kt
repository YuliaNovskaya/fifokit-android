package com.fifokit.app.data

import android.content.Context
import com.fifokit.app.domain.finance.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek

/** Local, versioned store. Cloud sync will be added when the new calculation flow is integrated. */
class AdvancedPayProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences("advanced_pay_v1", Context.MODE_PRIVATE)

    fun load(): PayProfile {
        val raw = preferences.getString("profile", null) ?: return PayProfile()
        return runCatching {
            val j = JSONObject(raw)
            val rates = j.optJSONArray("rates") ?: JSONArray()
            val overtime = j.optJSONArray("overtime") ?: JSONArray()
            val allowances = j.optJSONArray("allowances") ?: JSONArray()
            PayProfile(
                id = j.optString("id", "advanced"),
                name = j.optString("name", "My pay"),
                arrangement = WorkArrangement.valueOf(j.getString("arrangement")),
                employmentType = EmploymentType.valueOf(j.getString("employment")),
                basis = AdvancedPayBasis.valueOf(j.getString("basis")),
                baseRate = j.optDouble("baseRate"),
                shiftType = PayShiftType.valueOf(j.getString("shiftType")),
                casualLoadingPercent = j.optDouble("casualLoading"),
                rateRules = (0 until rates.length()).map { index ->
                    val r = rates.getJSONObject(index)
                    RateRule(
                        id = r.getString("id"),
                        category = r.optString("category").takeIf { it.isNotBlank() }?.let { DayCategory.valueOf(it) },
                        weekday = r.optString("weekday").takeIf { it.isNotBlank() }?.let { DayOfWeek.valueOf(it) },
                        shiftType = r.optString("shiftType").takeIf { it.isNotBlank() }?.let { PayShiftType.valueOf(it) },
                        value = r.getDouble("value"),
                        valueType = RateValueType.valueOf(r.getString("valueType")),
                        combination = RateCombination.valueOf(r.getString("combination")),
                        priority = r.optInt("priority")
                    )
                },
                overtimeRules = (0 until overtime.length()).map { index ->
                    val o = overtime.getJSONObject(index)
                    OvertimeRule(
                        id = o.getString("id"),
                        period = OvertimePeriod.valueOf(o.getString("period")),
                        afterHours = o.getDouble("afterHours"),
                        untilHours = if (o.isNull("untilHours")) null else o.getDouble("untilHours"),
                        value = o.getDouble("value"),
                        valueType = RateValueType.valueOf(o.getString("valueType")),
                        combination = RateCombination.valueOf(o.getString("combination"))
                    )
                },
                allowances = (0 until allowances.length()).map { index ->
                    val a = allowances.getJSONObject(index)
                    Allowance(a.getString("id"), a.getString("name"), a.getDouble("amount"), AllowanceFrequency.valueOf(a.getString("frequency")))
                }
            )
        }.getOrElse { PayProfile() }
    }

    fun save(profile: PayProfile) {
        val j = JSONObject()
            .put("schemaVersion", 1)
            .put("id", profile.id)
            .put("name", profile.name)
            .put("arrangement", profile.arrangement.name)
            .put("employment", profile.employmentType.name)
            .put("basis", profile.basis.name)
            .put("baseRate", profile.baseRate)
            .put("shiftType", profile.shiftType.name)
            .put("casualLoading", profile.casualLoadingPercent)
        j.put("rates", JSONArray().apply {
            profile.rateRules.forEach { r ->
                put(JSONObject().put("id", r.id).put("category", r.category?.name ?: "")
                    .put("weekday", r.weekday?.name ?: "").put("shiftType", r.shiftType?.name ?: "")
                    .put("value", r.value).put("valueType", r.valueType.name)
                    .put("combination", r.combination.name).put("priority", r.priority))
            }
        })
        j.put("overtime", JSONArray().apply {
            profile.overtimeRules.forEach { r ->
                put(JSONObject().put("id", r.id).put("period", r.period.name)
                    .put("afterHours", r.afterHours).put("untilHours", r.untilHours ?: JSONObject.NULL)
                    .put("value", r.value).put("valueType", r.valueType.name)
                    .put("combination", r.combination.name))
            }
        })
        j.put("allowances", JSONArray().apply {
            profile.allowances.forEach { a ->
                put(JSONObject().put("id", a.id).put("name", a.name)
                    .put("amount", a.amount).put("frequency", a.frequency.name))
            }
        })
        check(preferences.edit().putString("profile", j.toString()).commit()) { "Could not save advanced pay settings" }
    }
}

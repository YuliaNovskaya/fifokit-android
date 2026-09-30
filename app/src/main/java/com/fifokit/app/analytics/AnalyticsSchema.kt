package com.fifokit.app.analytics

object AnalyticsEvents {
    const val FIRST_OPEN = "first_open"
    const val INSTALL_ATTRIBUTION = "install_attribution"
    const val DEEP_LINK_OPENED = "deep_link_opened"

    const val ROSTER_CREATED = "roster_created"
    const val ROSTER_UPDATED = "roster_updated"
    const val ROSTER_DELETED = "roster_deleted"
    const val CALENDAR_VIEWED = "calendar_viewed"

    const val FINANCE_TOOLS_VIEWED = "finance_tools_viewed"
    const val FINANCE_TOOL_SELECTED = "finance_tool_selected"
    const val PAY_CALCULATION_COMPLETED = "pay_calculation_completed"
    const val ANNUAL_EARNINGS_CALCULATED = "annual_earnings_calculated"
    const val FINANCIAL_GOAL_CALCULATED = "financial_goal_calculated"

    const val PRO_PAYWALL_VIEWED = "pro_paywall_viewed"
    const val PRO_FEATURE_LOCKED = "pro_feature_locked"
    const val PRO_PURCHASE_STARTED = "pro_purchase_started"
    const val PRO_PURCHASE_SUCCESS = "pro_purchase_success"
    const val PRO_PURCHASE_CANCELLED = "pro_purchase_cancelled"
    const val PRO_PURCHASE_PENDING = "pro_purchase_pending"
    const val PRO_PURCHASE_ERROR = "pro_purchase_error"
    const val PRO_RESTORE_TAPPED = "pro_restore_tapped"

    const val ROSTER_INVITE_CREATED = "roster_invite_created"
    const val ROSTER_INVITE_SHARED = "roster_invite_shared"
    const val ROSTER_INVITE_ACCEPTED = "roster_invite_accepted"
    const val ROSTER_SHARE_REVOKED = "roster_share_revoked"
    const val SHARED_ROSTERS_VIEWED = "shared_rosters_viewed"
    const val SHARED_ROSTER_SELECTED = "shared_roster_selected"
    const val TOGETHER_CALENDAR_VIEWED = "together_calendar_viewed"
    const val FAMILY_PLANNING_SUMMARY_VIEWED = "family_planning_summary_viewed"
    const val SHARED_TIME_CARD_CLICKED = "shared_time_card_clicked"

    const val APP_SHARED = "app_shared"
    const val FEATURE_OPENED = "feature_opened"
    const val REVIEW_PROMPT_SHOWN = "review_prompt_shown"
    const val REVIEW_PROMPT_ACCEPTED = "review_prompt_accepted"
    const val REVIEW_PROMPT_DISMISSED = "review_prompt_dismissed"
    const val REVIEW_FLOW_TRIGGERED = "review_flow_triggered"

    const val ROSTER_EXPORT_VIEWED = "roster_export_viewed"
    const val ROSTER_EXPORT_CREATED = "roster_export_created"
    const val ROSTER_EXPORT_SAVED = "roster_export_saved"
    const val ROSTER_EXPORT_SHARED = "roster_export_shared"

    const val WIDGET_ADDED = "widget_added"
    const val WIDGET_OPENED = "widget_opened"
    const val WIDGET_PRO_LOCKED = "widget_pro_locked"

    const val NOTIFICATION_ENABLED = "notification_enabled"
    const val NOTIFICATION_DENIED = "notification_denied"
}

object AnalyticsParams {
    const val SOURCE = "source"
    const val MEDIUM = "medium"
    const val CAMPAIGN = "campaign"
    const val CONTENT = "content"
    const val DESTINATION = "destination"

    const val TOOL = "tool"
    const val PAY_TYPE = "pay_type"
    const val ROSTER_TYPE = "roster_type"
    const val PLAN = "plan"
    const val FEATURE = "feature"
    const val SURFACE = "surface"
    const val WIDGET_TYPE = "widget_type"

    const val FORMAT = "format"
    const val MONTH_COUNT = "month_count"
    const val PATTERN = "pattern"
    const val STATE_COUNT = "state_count"
    const val PAY_FREQUENCY_DAYS = "pay_frequency_days"

    const val REFERRER_CLICK_TS = "referrer_click_ts"
    const val INSTALL_BEGIN_TS = "install_begin_ts"
}

object AnalyticsUserProperties {
    const val ACQ_SOURCE = "acq_source"
    const val ACQ_MEDIUM = "acq_medium"
    const val ACQ_CAMPAIGN = "acq_campaign"
    const val ACQ_CONTENT = "acq_content"
}

data class AnalyticsFunnelStep(
    val label: String,
    val eventNames: Set<String>
)

data class AnalyticsFunnel(
    val name: String,
    val steps: List<AnalyticsFunnelStep>
)

object AnalyticsFunnels {
    val acquisition = AnalyticsFunnel(
        name = "Acquisition",
        steps = listOf(
            AnalyticsFunnelStep(
                label = "First open",
                eventNames = setOf(AnalyticsEvents.FIRST_OPEN)
            ),
            AnalyticsFunnelStep(
                label = "Activation",
                eventNames = setOf(
                    AnalyticsEvents.ROSTER_CREATED,
                    AnalyticsEvents.FINANCE_TOOLS_VIEWED
                )
            )
        )
    )

    val finance = AnalyticsFunnel(
        name = "Finance",
        steps = listOf(
            AnalyticsFunnelStep(
                label = "Finance opened",
                eventNames = setOf(
                    AnalyticsEvents.FINANCE_TOOLS_VIEWED
                )
            ),
            AnalyticsFunnelStep(
                label = "Tool selected",
                eventNames = setOf(
                    AnalyticsEvents.FINANCE_TOOL_SELECTED
                )
            ),
            AnalyticsFunnelStep(
                label = "Calculation completed",
                eventNames = setOf(
                    AnalyticsEvents.PAY_CALCULATION_COMPLETED,
                    AnalyticsEvents.ANNUAL_EARNINGS_CALCULATED,
                    AnalyticsEvents.FINANCIAL_GOAL_CALCULATED
                )
            )
        )
    )

    val pro = AnalyticsFunnel(
        name = "Pro",
        steps = listOf(
            AnalyticsFunnelStep(
                label = "Paywall viewed",
                eventNames = setOf(
                    AnalyticsEvents.PRO_PAYWALL_VIEWED
                )
            ),
            AnalyticsFunnelStep(
                label = "Purchase started",
                eventNames = setOf(
                    AnalyticsEvents.PRO_PURCHASE_STARTED
                )
            ),
            AnalyticsFunnelStep(
                label = "Purchase completed",
                eventNames = setOf(
                    AnalyticsEvents.PRO_PURCHASE_SUCCESS
                )
            )
        )
    )

    val partner = AnalyticsFunnel(
        name = "Partner",
        steps = listOf(
            AnalyticsFunnelStep(
                label = "Invite accepted",
                eventNames = setOf(
                    AnalyticsEvents.ROSTER_INVITE_ACCEPTED
                )
            ),
            AnalyticsFunnelStep(
                label = "Shared rosters opened",
                eventNames = setOf(
                    AnalyticsEvents.SHARED_ROSTERS_VIEWED
                )
            ),
            AnalyticsFunnelStep(
                label = "Together calendar opened",
                eventNames = setOf(
                    AnalyticsEvents.TOGETHER_CALENDAR_VIEWED
                )
            )
        )
    )

    val growth = AnalyticsFunnel(
        name = "Growth",
        steps = listOf(
            AnalyticsFunnelStep(
                label = "Review prompt shown",
                eventNames = setOf(
                    AnalyticsEvents.REVIEW_PROMPT_SHOWN
                )
            ),
            AnalyticsFunnelStep(
                label = "Review accepted",
                eventNames = setOf(
                    AnalyticsEvents.REVIEW_PROMPT_ACCEPTED
                )
            ),
            AnalyticsFunnelStep(
                label = "Play review flow requested",
                eventNames = setOf(
                    AnalyticsEvents.REVIEW_FLOW_TRIGGERED
                )
            )
        )
    )

    val all = listOf(
        acquisition,
        finance,
        pro,
        partner,
        growth
    )
}

object AnalyticsSchema {
    val customEventNames = setOf(
        AnalyticsEvents.INSTALL_ATTRIBUTION,
        AnalyticsEvents.DEEP_LINK_OPENED,
        AnalyticsEvents.ROSTER_CREATED,
        AnalyticsEvents.ROSTER_UPDATED,
        AnalyticsEvents.ROSTER_DELETED,
        AnalyticsEvents.CALENDAR_VIEWED,
        AnalyticsEvents.FINANCE_TOOLS_VIEWED,
        AnalyticsEvents.FINANCE_TOOL_SELECTED,
        AnalyticsEvents.PAY_CALCULATION_COMPLETED,
        AnalyticsEvents.ANNUAL_EARNINGS_CALCULATED,
        AnalyticsEvents.FINANCIAL_GOAL_CALCULATED,
        AnalyticsEvents.PRO_PAYWALL_VIEWED,
        AnalyticsEvents.PRO_FEATURE_LOCKED,
        AnalyticsEvents.PRO_PURCHASE_STARTED,
        AnalyticsEvents.PRO_PURCHASE_SUCCESS,
        AnalyticsEvents.PRO_PURCHASE_CANCELLED,
        AnalyticsEvents.PRO_PURCHASE_PENDING,
        AnalyticsEvents.PRO_PURCHASE_ERROR,
        AnalyticsEvents.PRO_RESTORE_TAPPED,
        AnalyticsEvents.ROSTER_INVITE_CREATED,
        AnalyticsEvents.ROSTER_INVITE_SHARED,
        AnalyticsEvents.ROSTER_INVITE_ACCEPTED,
        AnalyticsEvents.ROSTER_SHARE_REVOKED,
        AnalyticsEvents.SHARED_ROSTERS_VIEWED,
        AnalyticsEvents.SHARED_ROSTER_SELECTED,
        AnalyticsEvents.TOGETHER_CALENDAR_VIEWED,
        AnalyticsEvents.FAMILY_PLANNING_SUMMARY_VIEWED,
        AnalyticsEvents.SHARED_TIME_CARD_CLICKED,
        AnalyticsEvents.APP_SHARED,
        AnalyticsEvents.FEATURE_OPENED,
        AnalyticsEvents.REVIEW_PROMPT_SHOWN,
        AnalyticsEvents.REVIEW_PROMPT_ACCEPTED,
        AnalyticsEvents.REVIEW_PROMPT_DISMISSED,
        AnalyticsEvents.REVIEW_FLOW_TRIGGERED,
        AnalyticsEvents.ROSTER_EXPORT_VIEWED,
        AnalyticsEvents.ROSTER_EXPORT_CREATED,
        AnalyticsEvents.ROSTER_EXPORT_SAVED,
        AnalyticsEvents.ROSTER_EXPORT_SHARED,
        AnalyticsEvents.WIDGET_ADDED,
        AnalyticsEvents.WIDGET_OPENED,
        AnalyticsEvents.WIDGET_PRO_LOCKED,
        AnalyticsEvents.NOTIFICATION_ENABLED,
        AnalyticsEvents.NOTIFICATION_DENIED
    )

    val parameterNames = setOf(
        AnalyticsParams.SOURCE,
        AnalyticsParams.MEDIUM,
        AnalyticsParams.CAMPAIGN,
        AnalyticsParams.CONTENT,
        AnalyticsParams.DESTINATION,
        AnalyticsParams.TOOL,
        AnalyticsParams.PAY_TYPE,
        AnalyticsParams.ROSTER_TYPE,
        AnalyticsParams.PLAN,
        AnalyticsParams.FEATURE,
        AnalyticsParams.SURFACE,
        AnalyticsParams.WIDGET_TYPE,
        AnalyticsParams.FORMAT,
        AnalyticsParams.MONTH_COUNT,
        AnalyticsParams.PATTERN,
        AnalyticsParams.STATE_COUNT,
        AnalyticsParams.PAY_FREQUENCY_DAYS,
        AnalyticsParams.REFERRER_CLICK_TS,
        AnalyticsParams.INSTALL_BEGIN_TS
    )

    val userPropertyNames = setOf(
        AnalyticsUserProperties.ACQ_SOURCE,
        AnalyticsUserProperties.ACQ_MEDIUM,
        AnalyticsUserProperties.ACQ_CAMPAIGN,
        AnalyticsUserProperties.ACQ_CONTENT
    )
}

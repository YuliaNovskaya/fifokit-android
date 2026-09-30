# P7-W6 Growth analytics

This document defines the production analytics structure for FIFOKIT.

## Principles

- Keep event and parameter names low-cardinality and stable.
- Do not send roster IDs, invite IDs, partner roster IDs, email addresses, names, or other user-specific identifiers to Analytics.
- Acquisition first-touch is stored in `acq_source`, `acq_medium`, `acq_campaign`, and `acq_content`.
- `install_attribution` is a diagnostic acquisition event. Use the acquisition user properties to segment funnels because Install Referrer is resolved asynchronously.
- `deep_link_opened` measures website/app-link re-engagement and does not overwrite first-touch acquisition.
- `feature_opened` measures usage of existing feature entry points.

## Funnels

### Acquisition
1. `first_open`
2. Activation: `roster_created` OR `finance_tools_viewed`

Break down by:
- `acq_source`
- `acq_medium`
- `acq_campaign`
- `acq_content`

Track `deep_link_opened` separately for re-engagement.

### Finance
1. `finance_tools_viewed`
2. `finance_tool_selected`
3. Calculation completion:
   - `pay_calculation_completed`
   - `annual_earnings_calculated`
   - `financial_goal_calculated`

Use `tool` values:
- `pay_calculator`
- `annual_earnings`
- `financial_goal`

### Pro
1. `pro_paywall_viewed`
2. `pro_purchase_started`
3. `pro_purchase_success`

Use `plan` on purchase start. Monitor cancelled, pending and error events separately.

### Partner
Partner growth is two-sided, so do not build one owner-to-viewer closed funnel across users.

Viewer journey:
1. `roster_invite_accepted`
2. `shared_rosters_viewed`
3. `together_calendar_viewed`

Owner metrics:
- `roster_invite_created`
- `roster_invite_shared`
- `roster_share_revoked`

### Growth
Review funnel:
1. `review_prompt_shown`
2. `review_prompt_accepted`
3. `review_flow_triggered`

Supporting metrics:
- `app_shared`
- `feature_opened`
- `roster_export_shared`

Google Play controls whether the native review dialog is actually displayed, so `review_flow_triggered` does not prove that a review was submitted.

## Recommended GA4 configuration

Register user-scoped custom dimensions:
- `acq_source`
- `acq_medium`
- `acq_campaign`
- `acq_content`

Useful event-scoped dimensions:
- `destination`
- `tool`
- `plan`
- `feature`
- `surface`
- `widget_type`
- `format`
- `pay_type`
- `roster_type`

Useful custom metric:
- `month_count`

Recommended key events:
- `roster_created`
- `pro_purchase_success`
- `roster_invite_accepted`

Do not register high-cardinality timestamps or IDs as custom dimensions.

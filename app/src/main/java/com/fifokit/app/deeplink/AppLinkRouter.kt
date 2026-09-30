package com.fifokit.app.deeplink

enum class AppLinkDestination {
    ROSTER,
    FINANCE,
    INVITE
}

data class AppLinkRequest(
    val destination: AppLinkDestination,
    val inviteId: String? = null,
    val source: String? = null,
    val medium: String? = null,
    val campaign: String? = null,
    val content: String? = null
)

object AppLinkRouter {

    fun parse(
        scheme: String?,
        host: String?,
        pathSegments: List<String>,
        query: Map<String, String?>
    ): AppLinkRequest? {
        if (
            scheme != "https" ||
            host != "fifokit.com"
        ) {
            return null
        }

        val attribution =
            Attribution(
                source =
                    query["utm_source"]
                        ?: query["source"],
                medium =
                    query["utm_medium"],
                campaign =
                    query["utm_campaign"],
                content =
                    query["utm_content"]
            )

        if (
            pathSegments.size == 2 &&
            pathSegments[0] == "invite" &&
            pathSegments[1].isNotBlank()
        ) {
            return AppLinkRequest(
                destination =
                    AppLinkDestination.INVITE,
                inviteId =
                    pathSegments[1],
                source =
                    attribution.source,
                medium =
                    attribution.medium,
                campaign =
                    attribution.campaign,
                content =
                    attribution.content
            )
        }

        if (
            pathSegments.size == 2 &&
            pathSegments[0] == "app"
        ) {
            val destination =
                when (
                    pathSegments[1]
                ) {
                    "roster" ->
                        AppLinkDestination.ROSTER

                    "finance" ->
                        AppLinkDestination.FINANCE

                    else ->
                        return null
                }

            return AppLinkRequest(
                destination = destination,
                source =
                    attribution.source,
                medium =
                    attribution.medium,
                campaign =
                    attribution.campaign,
                content =
                    attribution.content
            )
        }

        return null
    }

    private data class Attribution(
        val source: String?,
        val medium: String?,
        val campaign: String?,
        val content: String?
    )
}

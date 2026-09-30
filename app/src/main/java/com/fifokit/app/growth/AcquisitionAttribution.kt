package com.fifokit.app.growth

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class AcquisitionAttribution(
    val source: String,
    val medium: String? = null,
    val campaign: String? = null,
    val content: String? = null
)

object AcquisitionAttributionParser {

    private const val MAX_EVENT_VALUE_LENGTH = 100

    fun parse(
        referrer: String
    ): AcquisitionAttribution {
        val params =
            parseParameters(
                referrer
            )

        return AcquisitionAttribution(
            source =
                clean(
                    params["utm_source"]
                        ?: params["source"]
                )
                    ?: "unattributed_play",
            medium =
                clean(
                    params["utm_medium"]
                ),
            campaign =
                clean(
                    params["utm_campaign"]
                ),
            content =
                clean(
                    params["utm_content"]
                )
        )
    }

    private fun parseParameters(
        referrer: String
    ): Map<String, String> {
        if (referrer.isBlank()) {
            return emptyMap()
        }

        val decoded =
            decode(referrer)

        return decoded
            .split("&")
            .mapNotNull { entry ->
                val separator =
                    entry.indexOf("=")

                if (separator <= 0) {
                    return@mapNotNull null
                }

                val key =
                    decode(
                        entry.substring(
                            0,
                            separator
                        )
                    )
                        .trim()

                val value =
                    decode(
                        entry.substring(
                            separator + 1
                        )
                    )
                        .trim()

                if (
                    key.isBlank() ||
                    value.isBlank()
                ) {
                    null
                } else {
                    key to value
                }
            }
            .toMap()
    }

    private fun clean(
        value: String?
    ): String? {
        return value
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
            ?.take(
                MAX_EVENT_VALUE_LENGTH
            )
    }

    private fun decode(
        value: String
    ): String {
        return runCatching {
            URLDecoder.decode(
                value,
                StandardCharsets.UTF_8
                    .name()
            )
        }.getOrDefault(value)
    }
}

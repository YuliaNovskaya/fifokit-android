package com.fifokit.app.domain.sharing

object SharingPolicy {

    const val FREE_RECIPIENT_LIMIT = 1
    const val PRO_RECIPIENT_LIMIT = 5

    fun recipientLimit(
        isPro: Boolean
    ): Int {
        return if (isPro) {
            PRO_RECIPIENT_LIMIT
        } else {
            FREE_RECIPIENT_LIMIT
        }
    }

    fun canCreateInvite(
        activeRecipientCount: Int,
        isPro: Boolean
    ): Boolean {
        return activeRecipientCount <
                recipientLimit(isPro)
    }

    fun recipientIdsToKeepActive(
        recipientEntries: List<Pair<String, Long>>,
        limit: Int
    ): Set<String> {
        return recipientEntries
            .filter { it.first.isNotBlank() }
            .groupBy { it.first }
            .mapValues { (_, entries) ->
                entries.minOf { it.second }
            }
            .entries
            .sortedBy { it.value }
            .take(limit)
            .map { it.key }
            .toSet()
    }

    fun canAcceptRecipient(
        activeRecipientCount: Int,
        recipientAlreadyActive: Boolean,
        recipientLimit: Int
    ): Boolean {
        return recipientAlreadyActive ||
                activeRecipientCount < recipientLimit
    }
}

class SharingLimitReachedException(
    val limit: Int,
    val isPro: Boolean
) : IllegalStateException(
    if (isPro) {
        "Pro sharing limit reached. Remove access before sharing with another person."
    } else {
        "Free sharing includes 1 person. Upgrade to Pro to share with up to 5 people."
    }
)

package net.dankito.utils.services


class Times {

    companion object {
        const val DefaultWaitTimeBeforeStartingCommunicationManagerMillis = 5000L

        const val DefaultDelayBeforeUpdatingIndexSeconds = 60

        const val DefaultIntervalToRunIndexOptimizationDays = 7

        const val DefaultDelayBeforeOptimizingDatabaseSeconds = 5 * 60

        const val DefaultIntervalToRunDatabaseOptimizationDays = 7
    }

}
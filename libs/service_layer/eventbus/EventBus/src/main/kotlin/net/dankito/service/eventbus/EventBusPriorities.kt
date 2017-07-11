package net.dankito.service.eventbus


class EventBusPriorities {

    companion object {

        const val Indexer = Int.MAX_VALUE

        const val EntityService = Indexer - 100

    }

}
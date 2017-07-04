package net.dankito.service.synchronization.initialsync.model


data class DeepThoughtSyncInfo(val id: String, val applicationLanguageIds: Map<String, String>,
                               val noteTypeIds: Map<String, String>, val fileTypeIds: Map<String, String>) {

    internal constructor() : this("", mapOf(), mapOf(), mapOf()) // for Jackson
}
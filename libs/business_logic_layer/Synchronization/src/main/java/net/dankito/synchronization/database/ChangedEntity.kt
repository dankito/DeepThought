package net.dankito.synchronization.database


data class ChangedEntity <T> (val entityClass: Class<T>, val entity: T?, val id: String?, val isDeleted: Boolean = false)
package net.dankito.service.synchronization.initialsync.model


open class UserSyncInfo(val id: String, val userName: String, val universallyUniqueId: String, val firstName: String, val lastName: String,
                        val countSynchronizedDevices: Int, val countIgnoredDevices: Int) {

    internal constructor() : this("", "", "", "", "", 0, 0) // for Jackson
}
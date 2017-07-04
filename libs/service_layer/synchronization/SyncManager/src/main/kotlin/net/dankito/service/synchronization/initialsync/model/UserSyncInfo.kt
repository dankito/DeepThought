package net.dankito.service.synchronization.initialsync.model

import net.dankito.deepthought.model.User


open class UserSyncInfo(val id: String, val userName: String, val universallyUniqueId: String, val firstName: String, val lastName: String,
                        val countSynchronizedDevices: Int, val countIgnoredDevices: Int) {

    internal constructor() : this("", "", "", "", "", 0, 0) // for Jackson

    constructor(user: User) : this(user.id!!, user.userName, user.universallyUniqueId, user.firstName, user.lastName, user.synchronizedDevices.size, user.ignoredDevices.size)

}
package net.dankito.synchronization.model


data class UserSyncInfo(val id: String, val userName: String, val universallyUniqueId: String, val firstName: String, val lastName: String,
                        val synchronizedDevicesIds: List<String>, val ignoredDevicesIds: List<String>) {

    internal constructor() : this("", "", "", "", "", listOf(), listOf()) // for Jackson

    constructor(user: User) : this(user.id!!, user.userName, user.universallyUniqueId, user.firstName, user.lastName, user.synchronizedDevices.map { it.id!! },
            user.ignoredDevices.map { it.id!! })

}
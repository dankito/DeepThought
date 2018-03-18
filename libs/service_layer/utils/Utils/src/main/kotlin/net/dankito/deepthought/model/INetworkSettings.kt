package net.dankito.deepthought.model


interface INetworkSettings {

    companion object {
        const val DefaultDeviceDiscoveryMessagePrefix = "DeepThought"
    }

    val localHostDevice: Device

    val localUser: User

    val deviceDiscoveryMessagePrefix: String

    var messagePort: Int

    var basicDataSynchronizationPort: Int

    var synchronizationPort: Int

    var fileSynchronizationPort: Int

    fun getDiscoveredDevice(uniqueDeviceId: String): DiscoveredDevice?

    fun addDiscoveredDevice(device: DiscoveredDevice)

    fun removeDiscoveredDevice(device: DiscoveredDevice)

    fun addConnectedDevicePermittedToSynchronize(device: DiscoveredDevice)

    fun removeConnectedDevicePermittedToSynchronize(device: DiscoveredDevice)

    // TODO: only add if user really has been asked, e.g. has been added to queue but different device is shown, then disconnects -> gets removed from queue, then reconnects -> thinks notification already has been displayed
    fun addDevicesAskedForPermittingSynchronization(device: DiscoveredDevice)

    fun removeDevicesAskedForPermittingSynchronization(device: DiscoveredDevice)

    fun didShowNotificationToUserForUnknownDevice(device: DiscoveredDevice): Boolean

    fun addUnknownDeviceNotificationShownToUser(device: DiscoveredDevice)

    fun removeUnknownDeviceNotificationShownToUser(device: DiscoveredDevice)

    fun addListener(listener: NetworkSettingsChangedListener)

    fun removeListener(listener: NetworkSettingsChangedListener)

}

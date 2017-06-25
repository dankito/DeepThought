package net.dankito.deepthought.model


interface INetworkSettings {

    val localHostDevice: Device

    var messagePort: Int

    var basicDataSynchronizationPort: Int

    var synchronizationPort: Int

    fun getDiscoveredDevice(uniqueDeviceId: String): DiscoveredDevice?

    fun addDiscoveredDevice(device: DiscoveredDevice)

    fun removeDiscoveredDevice(device: DiscoveredDevice)

    fun addConnectedDevicePermittedToSynchronize(device: DiscoveredDevice)

    fun removeConnectedDevicePermittedToSynchronize(device: DiscoveredDevice)

    fun addDevicesAskedForPermittingSynchronization(device: DiscoveredDevice)

    fun removeDevicesAskedForPermittingSynchronization(device: DiscoveredDevice)

    fun addListener(listener: NetworkSettingsChangedListener)

    fun removeListener(listener: NetworkSettingsChangedListener)

}

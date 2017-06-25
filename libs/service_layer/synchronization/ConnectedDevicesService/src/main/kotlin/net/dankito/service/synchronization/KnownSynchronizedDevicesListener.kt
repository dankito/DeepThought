package net.dankito.service.synchronization

import net.dankito.deepthought.model.DiscoveredDevice


interface KnownSynchronizedDevicesListener {

    fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice)

    fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

}

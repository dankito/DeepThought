package net.dankito.service.synchronization

import net.dankito.synchronization.model.DiscoveredDevice


interface KnownSynchronizedDevicesListener {

    fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice)

    fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

}

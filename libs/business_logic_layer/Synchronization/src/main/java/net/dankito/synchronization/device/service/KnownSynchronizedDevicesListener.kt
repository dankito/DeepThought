package net.dankito.synchronization.device.service

import net.dankito.synchronization.model.DiscoveredDevice


interface KnownSynchronizedDevicesListener {

    fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice)

    fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

}

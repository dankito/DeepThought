package net.dankito.synchronization.device.service

import net.dankito.synchronization.model.DiscoveredDevice


interface DiscoveredDevicesListener {

    fun deviceDiscovered(connectedDevice: DiscoveredDevice, type: DiscoveredDeviceType)

    fun disconnectedFromDevice(disconnectedDevice: DiscoveredDevice)

}

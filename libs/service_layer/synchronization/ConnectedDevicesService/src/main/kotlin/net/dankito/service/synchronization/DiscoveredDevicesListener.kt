package net.dankito.service.synchronization

import net.dankito.deepthought.model.DiscoveredDevice


interface DiscoveredDevicesListener {

    fun deviceDiscovered(connectedDevice: DiscoveredDevice, type: DiscoveredDeviceType)

    fun disconnectedFromDevice(disconnectedDevice: DiscoveredDevice)

}

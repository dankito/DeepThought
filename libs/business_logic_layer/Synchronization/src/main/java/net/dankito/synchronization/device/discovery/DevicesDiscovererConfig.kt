package net.dankito.synchronization.device.discovery


class DevicesDiscovererConfig(val localDeviceInfo: String,
                              val discoverDevicesPort: Int,
                              val checkForDevicesIntervalMillis: Int,
                              val discoveryMessagePrefix: String,
                              val listener: DevicesDiscovererListener) {

    override fun toString(): String {
        return localDeviceInfo
    }

}

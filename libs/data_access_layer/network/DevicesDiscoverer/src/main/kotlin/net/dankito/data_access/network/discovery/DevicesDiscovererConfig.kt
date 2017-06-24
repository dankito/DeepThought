package net.dankito.data_access.network.discovery


class DevicesDiscovererConfig {

    companion object {
        val DEFAULT_DISCOVERY_MESSAGE_PREFIX = "DevicesDiscovery"
    }


    var localDeviceInfo: String

    var discoverDevicesPort: Int = 0

    var checkForDevicesIntervalMillis: Int = 0

    var discoveryMessagePrefix = DEFAULT_DISCOVERY_MESSAGE_PREFIX

    var listener: DevicesDiscovererListener


    constructor(localDeviceInfo: String, discoverDevicesPort: Int, checkForDevicesIntervalMillis: Int, listener: DevicesDiscovererListener) {
        this.localDeviceInfo = localDeviceInfo
        this.discoverDevicesPort = discoverDevicesPort
        this.checkForDevicesIntervalMillis = checkForDevicesIntervalMillis
        this.listener = listener
    }

    constructor(localDeviceInfo: String, discoverDevicesPort: Int, checkForDevicesIntervalMillis: Int, discoveryMessagePrefix: String, listener: DevicesDiscovererListener) : this(localDeviceInfo, discoverDevicesPort, checkForDevicesIntervalMillis, listener) {
        this.discoveryMessagePrefix = discoveryMessagePrefix
    }


    override fun toString(): String {
        return localDeviceInfo
    }

}

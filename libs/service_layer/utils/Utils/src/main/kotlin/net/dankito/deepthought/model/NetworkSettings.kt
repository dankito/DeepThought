package net.dankito.deepthought.model

import java.util.concurrent.CopyOnWriteArrayList


class NetworkSettings(override val localHostDevice: Device, override val localUser: User,
                      override val deviceDiscoveryMessagePrefix: String = INetworkSettings.DefaultDeviceDiscoveryMessagePrefix) : INetworkSettings {

    override var messagePort: Int = 0
        set(value) {
            val oldValue = this.messagePort

            field = value

            callSettingChangedListeners(NetworkSetting.MESSAGES_PORT, value, oldValue)
        }

    override var basicDataSynchronizationPort: Int = 0
        set(value) {
            val oldValue = this.basicDataSynchronizationPort

            field = value

            callSettingChangedListeners(NetworkSetting.BASIC_DATA_SYNCHRONIZATION_PORT, value, oldValue)
        }

    override var synchronizationPort: Int = 0
        set(value) {
            val oldValue = this.synchronizationPort

            field = value

            callSettingChangedListeners(NetworkSetting.SYNCHRONIZATION_PORT, value, oldValue)
        }

    private val discoveredDevices: MutableMap<String, DiscoveredDevice> = HashMap()

    private val connectedDevicesPermittedToSynchronization: MutableMap<String, DiscoveredDevice> = HashMap()

    private val devicesAskedForPermittingSynchronization: MutableMap<String, DiscoveredDevice> = HashMap()

    private val unknownDeviceNotificationShownToUser: MutableMap<String, DiscoveredDevice> = HashMap()

    private val listeners: MutableList<NetworkSettingsChangedListener> = CopyOnWriteArrayList()



    override fun getDiscoveredDevice(uniqueDeviceId: String): DiscoveredDevice? {
        return discoveredDevices[uniqueDeviceId]
    }

    override fun addDiscoveredDevice(device: DiscoveredDevice) {
        discoveredDevices.put(device.device.uniqueDeviceId, device)

        callSettingChangedListeners(NetworkSetting.ADDED_DISCOVERED_DEVICE, device, null)
    }

    override fun removeDiscoveredDevice(device: DiscoveredDevice) {
        discoveredDevices.remove(device.device.uniqueDeviceId)

        callSettingChangedListeners(NetworkSetting.REMOVED_DISCOVERED_DEVICE, device, null)
    }


    override fun addConnectedDevicePermittedToSynchronize(device: DiscoveredDevice) {
        connectedDevicesPermittedToSynchronization.put(device.device.uniqueDeviceId, device)

        callSettingChangedListeners(NetworkSetting.ADDED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE, device, null)
    }

    override fun removeConnectedDevicePermittedToSynchronize(device: DiscoveredDevice) {
        connectedDevicesPermittedToSynchronization.remove(device.device.uniqueDeviceId)

        callSettingChangedListeners(NetworkSetting.REMOVED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE, device, null)
    }


    /**
     * Actually only needed to start CouchbaseLite Listener
     * @param device
     */
    override fun addDevicesAskedForPermittingSynchronization(device: DiscoveredDevice) {
        devicesAskedForPermittingSynchronization.put(device.device.uniqueDeviceId, device)

        callSettingChangedListeners(NetworkSetting.ADDED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION, device, null)
    }

    override fun removeDevicesAskedForPermittingSynchronization(device: DiscoveredDevice) {
        devicesAskedForPermittingSynchronization.remove(device.device.uniqueDeviceId)

        callSettingChangedListeners(NetworkSetting.REMOVED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION, device, null)
    }


    override fun didShowNotificationToUserForUnknownDevice(device: DiscoveredDevice): Boolean {
        return unknownDeviceNotificationShownToUser.containsKey(device.device.uniqueDeviceId)
    }

    override fun addUnknownDeviceNotificationShownToUser(device: DiscoveredDevice) {
        unknownDeviceNotificationShownToUser.put(device.device.uniqueDeviceId, device)

        callSettingChangedListeners(NetworkSetting.ADDED_UNKNOWN_DEVICE_NOTIFICATION_SHOWN_TO_USER, device, null)
    }

    override fun removeUnknownDeviceNotificationShownToUser(device: DiscoveredDevice) {
        unknownDeviceNotificationShownToUser.remove(device.device.uniqueDeviceId)

        callSettingChangedListeners(NetworkSetting.REMOVED_UNKNOWN_DEVICE_NOTIFICATION_SHOWN_TO_USER, device, null)
    }


    override fun addListener(listener: NetworkSettingsChangedListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: NetworkSettingsChangedListener) {
        listeners.remove(listener)
    }

    protected fun callSettingChangedListeners(setting: NetworkSetting, newValue: Any, oldValue: Any?) {
        for (listener in listeners) {
            listener.settingsChanged(this, setting, newValue, oldValue)
        }
    }


    override fun toString(): String {
        return "$localHostDevice, Messages Port: $messagePort, Synchronization Port: $synchronizationPort"
    }

}

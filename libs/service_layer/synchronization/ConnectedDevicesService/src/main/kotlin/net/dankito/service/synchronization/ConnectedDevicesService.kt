package net.dankito.service.synchronization

import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.RequestStartSynchronizationResponseBody
import net.dankito.data_access.network.communication.message.RequestStartSynchronizationResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.data_access.network.discovery.DevicesDiscovererConfig
import net.dankito.data_access.network.discovery.DevicesDiscovererListener
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.model.User
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


// TODO: replace IEntityManager with DevicesService
class ConnectedDevicesService(private val devicesDiscoverer: IDevicesDiscoverer, private val clientCommunicator: IClientCommunicator, private val syncManager: ISyncManager,
                              registrationHandler: IDeviceRegistrationHandler, private val networkSettings: INetworkSettings, private val entityManager: IEntityManager)
    : IConnectedDevicesService {

    companion object {
        const val DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR = "|"

        const val MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR = ":"

        private val log = LoggerFactory.getLogger(ConnectedDevicesService::class.java)
    }


    private var localDevice: Device = networkSettings.localHostDevice

    private var localUser: User = networkSettings.localUser


    private var discoveredDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    private var devicesPendingStartSynchronization: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    private var knownSynchronizedDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    private var knownIgnoredDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    private var unknownDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()


    private var discoveredDevicesListeners: MutableList<DiscoveredDevicesListener> = CopyOnWriteArrayList()

    private var knownSynchronizedDevicesListeners: MutableList<KnownSynchronizedDevicesListener> = CopyOnWriteArrayList()


    init {
        registrationHandler.addRequestingToSynchronizeWithRemoteListener { _ -> syncManager.openSynchronizationPort() }

        registrationHandler.addNewDeviceRegisteredListener { remoteDevice -> startSynchronizingWithNewlyRegisteredDevice(remoteDevice) }

        clientCommunicator.addRemoteRequestedToStartSynchronizationListener { remoteDevice -> startSynchronizingWithDevice(remoteDevice) }
    }

    override fun start() {
        val localDeviceInfoKey = getDeviceInfoKey(networkSettings)

        devicesDiscoverer.startAsync(DevicesDiscovererConfig(localDeviceInfoKey, ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT,
                ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS, ConnectedDevicesServiceConfig.DISCOVERY_MESSAGE_PREFIX, discovererListener))
    }

    override fun stop() {
        devicesDiscoverer.stop()

        for (knownSynchronizedDevice in knownSynchronizedDevices.values) {
            callKnownSynchronizedDeviceDisconnected(knownSynchronizedDevice)
        }

        knownSynchronizedDevices.clear()
        knownIgnoredDevices.clear()
        unknownDevices.clear()


        for (discoveredDevice in discoveredDevices.values) {
            callKnownSynchronizedDeviceDisconnected(discoveredDevice)
        }

        discoveredDevices.clear()
    }


    private val discovererListener: DevicesDiscovererListener = object : DevicesDiscovererListener {
        override fun deviceFound(deviceInfo: String, address: String) {
            discoveredDevice(deviceInfo, address)
        }

        override fun deviceDisconnected(deviceInfo: String) {
            this@ConnectedDevicesService.deviceDisconnected(deviceInfo)
        }
    }


    private fun discoveredDevice(deviceInfoKey: String, address: String) {
        getDeviceIdFromDeviceInfoKey(deviceInfoKey)?.let { deviceId ->
            val messagesPort = getMessagesPortFromDeviceInfoKey(deviceInfoKey)
            val remoteDevice = getPersistedDeviceForDeviceId(deviceId)

            if(remoteDevice != null) {
                discoveredDevice(deviceInfoKey, remoteDevice, address, messagesPort)
            }
            else { // remote device not known and therefore not persisted yet
                val basicDataSyncPort = getBasicDataSyncPortFromDeviceInfoKey(deviceInfoKey)
                syncBasicDataWithUnknownDevice(deviceInfoKey, deviceId, address, messagesPort, basicDataSyncPort)
            }
        }
    }

    private fun getPersistedDeviceForDeviceId(deviceId: String): Device? {
        return entityManager.getEntityById(Device::class.java, deviceId)
    }

    private fun syncBasicDataWithUnknownDevice(deviceInfoKey: String, deviceId: String, address: String, messagesPort: Int, basicDataSyncPort: Int) {
        syncManager.syncBasicDataWithDevice(deviceId, address, basicDataSyncPort, { remoteDevice ->
            discoveredDevice(deviceInfoKey, remoteDevice, address, messagesPort)
        })
    }


    private fun discoveredDevice(deviceInfoKey: String, device: Device, address: String, messagesPort: Int) {
        val discoveredDevice = DiscoveredDevice(device, address)

        discoveredDevice.messagesPort = messagesPort

        discoveredDevice(deviceInfoKey, discoveredDevice)
    }

    private fun discoveredDevice(deviceInfoKey: String, device: DiscoveredDevice) {
        synchronized(discoveredDevices) {
            discoveredDevices.put(deviceInfoKey, device)
            networkSettings.addDiscoveredDevice(device)

            val type = determineDiscoveredDeviceType(device)

            if (type === DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
                discoveredKnownSynchronizedDevice(device, deviceInfoKey)
                addDeviceToKnownSynchronizedDevices(deviceInfoKey, device)
            } else if (type === DiscoveredDeviceType.KNOWN_IGNORED_DEVICE) {
                knownIgnoredDevices.put(deviceInfoKey, device)
            } else {
                unknownDevices.put(deviceInfoKey, device)
            }

            callDiscoveredDeviceConnectedListeners(device, type)
        }
    }

    private fun determineDiscoveredDeviceType(device: DiscoveredDevice): DiscoveredDeviceType {
        if (isKnownSynchronizedDevice(device)) {
            return DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE
        } else if (isKnownIgnoredDevice(device)) {
            return DiscoveredDeviceType.KNOWN_IGNORED_DEVICE
        } else {
            return DiscoveredDeviceType.UNKNOWN_DEVICE
        }
    }

    private fun discoveredKnownSynchronizedDevice(device: DiscoveredDevice, deviceInfoKey: String) {
        syncManager.openSynchronizationPort()

        devicesPendingStartSynchronization.put(deviceInfoKey, device)

        clientCommunicator.requestStartSynchronization(device) { response ->
            handleRequestStartSynchronizationResponse(response, device, deviceInfoKey)
        }
    }

    private fun handleRequestStartSynchronizationResponse(response: Response<RequestStartSynchronizationResponseBody>, device: DiscoveredDevice, deviceInfoKey: String) {
        if (response.isCouldHandleMessage) {
            response.body?.let { body ->
                if (body.result === RequestStartSynchronizationResult.ALLOWED) {
                    device.synchronizationPort = body.synchronizationPort

                    syncManager.startSynchronizationWithDevice(device)

                    devicesPendingStartSynchronization.remove(deviceInfoKey)

                    callKnownSynchronizedDeviceConnected(device)
                }
            }
        }
        // TODO: what to do if not?
    }


    private fun deviceDisconnected(deviceInfoKey: String) {
        val device = discoveredDevices[deviceInfoKey]
        if (device != null) {
            disconnectedFromDevice(deviceInfoKey, device)
        } else {
            log.error("This should never occur! Disconnected from Device, but was not in discoveredDevices: " + deviceInfoKey)
        }
    }

    private fun disconnectedFromDevice(deviceInfo: String, device: DiscoveredDevice) {
        synchronized(discoveredDevices) {
            discoveredDevices.remove(deviceInfo)
            networkSettings.removeDiscoveredDevice(device)

            if (isKnownSynchronizedDevice(device)) {
                knownSynchronizedDevices.remove(deviceInfo)
                networkSettings.removeConnectedDevicePermittedToSynchronize(device)
                callKnownSynchronizedDeviceDisconnected(device)
            } else if (isKnownIgnoredDevice(device)) {
                knownIgnoredDevices.remove(deviceInfo)
            } else {
                unknownDevices.remove(deviceInfo)
            }

            callDiscoveredDeviceDisconnectedListeners(device)
        }
    }


    private fun isKnownSynchronizedDevice(device: DiscoveredDevice): Boolean {
        return localUser.synchronizedDevices.contains(device.device)
    }

    private fun isKnownIgnoredDevice(device: DiscoveredDevice): Boolean {
        return localUser.ignoredDevices.contains(device.device)
    }


    private fun getDeviceInfoKey(networkSettings: INetworkSettings): String {
        return networkSettings.localHostDevice.id + DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR + networkSettings.messagePort +
                MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR + networkSettings.basicDataSynchronizationPort
    }

    private fun getDeviceKeyForDevice(device: DiscoveredDevice): String {
        return device.device.id ?: "" // should actually never be null at this stage
    }

    private fun getDeviceIdFromDeviceInfoKey(deviceInfoKey: String): String? {
        val deviceIdEndIndex = deviceInfoKey.lastIndexOf(DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR)
        if (deviceIdEndIndex > 0) {
            return deviceInfoKey.substring(0, deviceIdEndIndex)
        }

        return null
    }

    private fun getBasicDataSyncPortFromDeviceInfoKey(deviceInfoKey: String): Int {
        var portStartIndex = deviceInfoKey.lastIndexOf(MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR)
        if (portStartIndex > 0) {
            portStartIndex += MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR.length

            val portString = deviceInfoKey.substring(portStartIndex)
            return Integer.parseInt(portString)
        }

        return -1
    }

    private fun getMessagesPortFromDeviceInfoKey(deviceInfoKey: String): Int {
        var portStartIndex = deviceInfoKey.lastIndexOf(DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR)
        if (portStartIndex > 0) {
            portStartIndex += DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR.length

            val portEndIndex = deviceInfoKey.indexOf(MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR, portStartIndex)

            val portString = deviceInfoKey.substring(portStartIndex, portEndIndex)
            return Integer.parseInt(portString)
        }

        return -1
    }


    override fun startSynchronizingWithNewlyRegisteredDevice(device: DiscoveredDevice) {
        // TODO: the whole process should actually run in a transaction
        addDeviceToKnownSynchronizedDevicesAndCallListeners(device)
    }

    override fun remoteDeviceStartedSynchronizingWithUs(remoteDevice: Device) {
        getDiscoveredDeviceForDevice(remoteDevice).let { discoveredRemoteDevice ->
            for(discoveredDevice in discoveredDevices.values) {
                if(discoveredDevice == discoveredRemoteDevice) {
                    addDeviceToKnownSynchronizedDevicesAndCallListeners(discoveredDevice)
                    break
                }
            }
        }
    }

    private fun addDeviceToKnownSynchronizedDevicesAndCallListeners(device: DiscoveredDevice) {
        if (addDeviceToKnownSynchronizedDevices(device)) {
            addDeviceToSynchronizedDevices(device)

            callDiscoveredDeviceDisconnectedListeners(device)
            callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE)

            callKnownSynchronizedDeviceConnected(device)
        }
    }

    private fun addDeviceToKnownSynchronizedDevices(device: DiscoveredDevice): Boolean {
        val deviceInfoKey = getDeviceKeyForDevice(device)

        if(deviceInfoKey != null) {
            unknownDevices.remove(deviceInfoKey)
            knownIgnoredDevices.remove(deviceInfoKey)

            startSynchronizingWithDevice(deviceInfoKey, device)

            return true
        }

        return false
    }

    private fun addDeviceToKnownSynchronizedDevices(deviceInfoKey: String, device: DiscoveredDevice) {
        knownSynchronizedDevices.put(deviceInfoKey, device)
    }

    private fun startSynchronizingWithDevice(device: DiscoveredDevice) {
        startSynchronizingWithDevice(getDeviceKeyForDevice(device), device)
    }

    private fun startSynchronizingWithDevice(deviceInfoKey: String, device: DiscoveredDevice) {
        syncManager.startSynchronizationWithDevice(device)

        networkSettings.addConnectedDevicePermittedToSynchronize(device)

        addDeviceToKnownSynchronizedDevices(deviceInfoKey, device)
    }

    private fun addDeviceToSynchronizedDevices(device: DiscoveredDevice) {
        if (localUser.ignoredDevices.contains(device.device)) {
            localUser.removeIgnoredDevice(device.device)
        }
        localUser.addSynchronizedDevice(device.device)

        entityManager.updateEntity(localUser)
    }


    override fun stopSynchronizingWithDevice(device: DiscoveredDevice) {
        localUser.removeSynchronizedDevice(device.device)

        val deviceInfoKey = getDeviceKeyForDevice(device)
        knownSynchronizedDevices.remove(deviceInfoKey)
        unknownDevices.put(deviceInfoKey, device)

        entityManager.updateEntity(localDevice)

        callKnownSynchronizedDeviceDisconnected(device)

        callDiscoveredDeviceDisconnectedListeners(device)
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE)
    }

    override fun addDeviceToIgnoreList(device: DiscoveredDevice) {
        if (localUser.addIgnoredDevice(device.device)) {
            if (entityManager.updateEntity(localDevice)) {
                val deviceInfoKey = getDeviceKeyForDevice(device)
                unknownDevices.remove(deviceInfoKey)
                knownIgnoredDevices.put(deviceInfoKey, device)

                callDiscoveredDeviceDisconnectedListeners(device)
                callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_IGNORED_DEVICE)
            }
        }
    }

    override fun startSynchronizingWithIgnoredDevice(device: DiscoveredDevice) {
        if (localUser.removeIgnoredDevice(device.device)) {
            if (entityManager.updateEntity(localDevice)) {
                val deviceInfoKey = getDeviceKeyForDevice(device)
                knownIgnoredDevices.remove(deviceInfoKey)

                startSynchronizingWithNewlyRegisteredDevice(device)
            }
        }
    }


    override fun addDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean {
        return discoveredDevicesListeners.add(listener)
    }

    override fun removeDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean {
        return discoveredDevicesListeners.remove(listener)
    }

    private fun callDiscoveredDeviceConnectedListeners(device: DiscoveredDevice, type: DiscoveredDeviceType) {
        for (listener in discoveredDevicesListeners) {
            listener.deviceDiscovered(device, type)
        }
    }

    private fun callDiscoveredDeviceDisconnectedListeners(device: DiscoveredDevice) {
        for (listener in discoveredDevicesListeners) {
            listener.disconnectedFromDevice(device)
        }
    }


    override fun addKnownSynchronizedDevicesListener(listener: KnownSynchronizedDevicesListener): Boolean {
        return knownSynchronizedDevicesListeners.add(listener)
    }

    override fun removeKnownSynchronizedDevicesListener(listener: KnownSynchronizedDevicesListener): Boolean {
        return knownSynchronizedDevicesListeners.remove(listener)
    }

    private fun callKnownSynchronizedDeviceConnected(device: DiscoveredDevice) {
        for (listener in knownSynchronizedDevicesListeners) {
            listener.knownSynchronizedDeviceConnected(device)
        }
    }

    private fun callKnownSynchronizedDeviceDisconnected(device: DiscoveredDevice) {
        for (listener in knownSynchronizedDevicesListeners) {
            listener.knownSynchronizedDeviceDisconnected(device)
        }
    }


    override fun getDiscoveredDeviceForDevice(device: Device): DiscoveredDevice? {
        device.id?.let { deviceId ->
            return getDiscoveredDeviceForId(deviceId)
        }

        return null
    }

    override fun getDiscoveredDeviceForId(deviceId: String): DiscoveredDevice? {
        for (device in allDiscoveredDevices) {
            if (device.device.id == deviceId) {
                return device
            }
        }

        return null
    }

    override val allDiscoveredDevices: List<DiscoveredDevice>
        get() = ArrayList(discoveredDevices.values)

    override val knownSynchronizedDiscoveredDevices: List<DiscoveredDevice>
        get() = ArrayList(knownSynchronizedDevices.values)

    override val knownIgnoredDiscoveredDevices: List<DiscoveredDevice>
        get() = ArrayList(knownIgnoredDevices.values)

    override val unknownDiscoveredDevices: List<DiscoveredDevice>
        get() = ArrayList(unknownDevices.values)

}

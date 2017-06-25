package net.dankito.service.synchronization

import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.callback.SendRequestCallback
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.data_access.network.communication.message.RequestStartSynchronizationResponseBody
import net.dankito.data_access.network.communication.message.RequestStartSynchronizationResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.data_access.network.discovery.DevicesDiscovererConfig
import net.dankito.data_access.network.discovery.DevicesDiscovererListener
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.service.data.DataManager
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


// TODO: replace IEntityManager with DevicesService
class ConnectedDevicesService(protected var devicesDiscoverer: IDevicesDiscoverer, protected var clientCommunicator: IClientCommunicator, dataManager: DataManager, protected var networkSettings: INetworkSettings, protected var entityManager: IEntityManager) : IConnectedDevicesService {

    protected var localDevice: Device


    protected var discoveredDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    protected var devicesPendingStartSynchronization: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    protected var knownSynchronizedDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    protected var knownIgnoredDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()

    protected var unknownDevices: MutableMap<String, DiscoveredDevice> = ConcurrentHashMap()


    protected var discoveredDevicesListeners: MutableList<DiscoveredDevicesListener> = CopyOnWriteArrayList()

    protected var knownSynchronizedDevicesListeners: MutableList<KnownSynchronizedDevicesListener> = CopyOnWriteArrayList()


    init {
        this.localDevice = dataManager.localDevice
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


    protected var discovererListener: DevicesDiscovererListener = object : DevicesDiscovererListener {
        override fun deviceFound(deviceInfo: String, address: String) {
            getDeviceDetailsForDiscoveredDevice(deviceInfo, address)
        }

        override fun deviceDisconnected(deviceInfo: String) {
            this@ConnectedDevicesService.deviceDisconnected(deviceInfo)
        }
    }

    protected fun getDeviceDetailsForDiscoveredDevice(deviceInfoKey: String, address: String) {
        try {
            val messagesPort = getMessagesPortFromDeviceInfoKey(deviceInfoKey)

            retrieveDeviceInfoFromRemote(deviceInfoKey, address, messagesPort)
        } catch (e: Exception) {
            log.error("Could not deserialize Device from " + deviceInfoKey, e)
        }

    }

    protected fun retrieveDeviceInfoFromRemote(deviceInfoKey: String, address: String, messagesPort: Int) {
        clientCommunicator.getDeviceInfo(InetSocketAddress(address, messagesPort), object : SendRequestCallback<DeviceInfo> {
            override fun done(response: Response<DeviceInfo>) {
                if (response.isCouldHandleMessage) {
                    (response.body as? DeviceInfo)?.let { deviceInfo ->
                        successfullyRetrievedDeviceInfo(deviceInfoKey, deviceInfo, address, messagesPort)
                    }
                } else {
                    // TODO: try periodically to get DeviceInfo from remote
                }
            }
        })
    }

    protected fun successfullyRetrievedDeviceInfo(deviceInfoKey: String, deviceInfo: DeviceInfo, address: String, messagesPort: Int) {
        var remoteDevice = getPersistedDeviceForUniqueId(deviceInfo.uniqueDeviceId)

        if (remoteDevice == null) { // remote device not known and therefore not persisted yet
            remoteDevice = mapDeviceInfoToDevice(deviceInfo)

            entityManager.persistEntity(remoteDevice)
        }

        discoveredDevice(deviceInfoKey, remoteDevice, address, messagesPort)
    }

    protected fun getPersistedDeviceForUniqueId(deviceUniqueId: String): Device? {
        val persistedDevices = entityManager.getAllEntitiesOfType(Device::class.java)
        for (device in persistedDevices) {
            if (deviceUniqueId == device.uniqueDeviceId) {
                return device
            }
        }

        return null
    }

    protected fun mapDeviceInfoToDevice(deviceInfo: DeviceInfo): Device {
        return Device(deviceInfo.id, deviceInfo.name, deviceInfo.uniqueDeviceId, deviceInfo.osType, deviceInfo.osName,
                deviceInfo.osVersion, deviceInfo.description)
    }

    protected fun discoveredDevice(deviceInfoKey: String, device: Device, address: String, messagesPort: Int) {
        val discoveredDevice = DiscoveredDevice(device, address)

        discoveredDevice.messagesPort = messagesPort

        discoveredDevice(deviceInfoKey, discoveredDevice)
    }

    protected fun discoveredDevice(deviceInfoKey: String, device: DiscoveredDevice) {
        synchronized(discoveredDevices) {
            discoveredDevices.put(deviceInfoKey, device)
            networkSettings.addDiscoveredDevice(device)

            val type = determineDiscoveredDeviceType(device)

            if (type === DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
                discoveredKnownSynchronizedDevice(device, deviceInfoKey)
                knownSynchronizedDevices.put(deviceInfoKey, device)
            } else if (type === DiscoveredDeviceType.KNOWN_IGNORED_DEVICE) {
                knownIgnoredDevices.put(deviceInfoKey, device)
            } else {
                unknownDevices.put(deviceInfoKey, device)
            }

            callDiscoveredDeviceConnectedListeners(device, type)
        }
    }

    protected fun determineDiscoveredDeviceType(device: DiscoveredDevice): DiscoveredDeviceType {
        if (isKnownSynchronizedDevice(device)) {
            return DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE
        } else if (isKnownIgnoredDevice(device)) {
            return DiscoveredDeviceType.KNOWN_IGNORED_DEVICE
        } else {
            return DiscoveredDeviceType.UNKNOWN_DEVICE
        }
    }

    protected fun discoveredKnownSynchronizedDevice(device: DiscoveredDevice, deviceInfoKey: String) {
        devicesPendingStartSynchronization.put(deviceInfoKey, device)

        clientCommunicator.requestStartSynchronization(device, object : SendRequestCallback<RequestStartSynchronizationResponseBody> {
            override fun done(response: Response<RequestStartSynchronizationResponseBody>) {
                handleRequestStartSynchronizationResponse(response, device, deviceInfoKey)
            }
        })
    }

    protected fun handleRequestStartSynchronizationResponse(response: Response<RequestStartSynchronizationResponseBody>, device: DiscoveredDevice, deviceInfoKey: String) {
        if (response.isCouldHandleMessage) {
            (response.body as? RequestStartSynchronizationResponseBody)?.let { body ->
                if (body.result === RequestStartSynchronizationResult.ALLOWED) {
                    device.synchronizationPort = body.synchronizationPort

                    devicesPendingStartSynchronization.remove(deviceInfoKey)

                    callKnownSynchronizedDeviceConnected(device)
                }
            }
        }
        // TODO: what to do if not?
    }


    protected fun deviceDisconnected(deviceInfoKey: String) {
        val device = discoveredDevices[deviceInfoKey]
        if (device != null) {
            disconnectedFromDevice(deviceInfoKey, device)
        } else {
            log.error("This should never occur! Disconnected from Device, but was not in discoveredDevices: " + deviceInfoKey)
        }
    }

    protected fun disconnectedFromDevice(deviceInfo: String, device: DiscoveredDevice) {
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


    protected fun isKnownSynchronizedDevice(device: DiscoveredDevice): Boolean {
        return localDevice.synchronizedDevices.contains(device.device)
    }

    protected fun isKnownIgnoredDevice(device: DiscoveredDevice): Boolean {
        return localDevice.ignoredDevices.contains(device.device)
    }


    protected fun getDeviceInfoKey(networkSettings: INetworkSettings): String {
        val localDevice = DiscoveredDevice(networkSettings.localHostDevice, "localhost", networkSettings.messagePort, 0)

        return getDeviceInfoKey(localDevice)
    }

    protected fun getDeviceInfoKey(device: DiscoveredDevice): String {
        return device.device.uniqueDeviceId + DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR + device.messagesPort
    }

    protected fun getMessagesPortFromDeviceInfoKey(deviceInfoKey: String): Int {
        var portStartIndex = deviceInfoKey.lastIndexOf(DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR)
        if (portStartIndex > 0) {
            portStartIndex += DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR.length

            val portString = deviceInfoKey.substring(portStartIndex)
            return Integer.parseInt(portString)
        }

        return -1
    }


    override fun startSynchronizingWithDevice(device: DiscoveredDevice) {
        // TODO: the whole process should actually run in a transaction
        networkSettings.addConnectedDevicePermittedToSynchronize(device)

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

    protected fun addDeviceToKnownSynchronizedDevicesAndCallListeners(device: DiscoveredDevice) {
        if (addDeviceToKnownSynchronizedDevices(device)) {
            addDeviceToLocalConfigSynchronizedDevices(device)

            callDiscoveredDeviceDisconnectedListeners(device)
            callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE)

            callKnownSynchronizedDeviceConnected(device)
        }
    }

    protected fun addDeviceToKnownSynchronizedDevices(device: DiscoveredDevice): Boolean {
        val deviceInfoKey = getDeviceInfoKey(device)

        if (deviceInfoKey != null) {
            unknownDevices.remove(deviceInfoKey)
            knownIgnoredDevices.remove(deviceInfoKey)

            knownSynchronizedDevices.put(deviceInfoKey, device)

            return true
        }

        return false
    }

    protected fun addDeviceToLocalConfigSynchronizedDevices(device: DiscoveredDevice) {
        if (localDevice.ignoredDevices.contains(device.device)) {
            localDevice.removeIgnoredDevice(device.device)
        }
        localDevice.addSynchronizedDevice(device.device)

        entityManager.updateEntity(localDevice)
    }


    override fun stopSynchronizingWithDevice(device: DiscoveredDevice) {
        localDevice.removeSynchronizedDevice(device.device)

        val deviceInfoKey = getDeviceInfoKey(device)
        knownSynchronizedDevices.remove(deviceInfoKey)
        unknownDevices.put(deviceInfoKey, device)

        entityManager.updateEntity(localDevice)

        callKnownSynchronizedDeviceDisconnected(device)

        callDiscoveredDeviceDisconnectedListeners(device)
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE)
    }

    override fun addDeviceToIgnoreList(device: DiscoveredDevice) {
        if (localDevice.addIgnoredDevice(device.device)) {
            if (entityManager.updateEntity(localDevice)) {
                val deviceInfoKey = getDeviceInfoKey(device)
                unknownDevices.remove(deviceInfoKey)
                knownIgnoredDevices.put(deviceInfoKey, device)

                callDiscoveredDeviceDisconnectedListeners(device)
                callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_IGNORED_DEVICE)
            }
        }
    }

    override fun startSynchronizingWithIgnoredDevice(device: DiscoveredDevice) {
        if (localDevice.removeIgnoredDevice(device.device)) {
            if (entityManager.updateEntity(localDevice)) {
                val deviceInfoKey = getDeviceInfoKey(device)
                knownIgnoredDevices.remove(deviceInfoKey)

                startSynchronizingWithDevice(device)
            }
        }
    }


    override fun addDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean {
        return discoveredDevicesListeners.add(listener)
    }

    override fun removeDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean {
        return discoveredDevicesListeners.remove(listener)
    }

    protected fun callDiscoveredDeviceConnectedListeners(device: DiscoveredDevice, type: DiscoveredDeviceType) {
        for (listener in discoveredDevicesListeners) {
            listener.deviceDiscovered(device, type)
        }
    }

    protected fun callDiscoveredDeviceDisconnectedListeners(device: DiscoveredDevice) {
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

    protected fun callKnownSynchronizedDeviceConnected(device: DiscoveredDevice) {
        for (listener in knownSynchronizedDevicesListeners) {
            listener.knownSynchronizedDeviceConnected(device)
        }
    }

    protected fun callKnownSynchronizedDeviceDisconnected(device: DiscoveredDevice) {
        for (listener in knownSynchronizedDevicesListeners) {
            listener.knownSynchronizedDeviceDisconnected(device)
        }
    }


    override fun getDiscoveredDeviceForDevice(device: Device): DiscoveredDevice? {
        return getDiscoveredDeviceForId(device.uniqueDeviceId)
    }

    override fun getDiscoveredDeviceForId(uniqueDeviceId: String): DiscoveredDevice? {
        for (device in allDiscoveredDevices) {
            if (device.device.uniqueDeviceId == uniqueDeviceId) {
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

    companion object {

        protected val DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR = ":"

        private val log = LoggerFactory.getLogger(ConnectedDevicesService::class.java)
    }

}

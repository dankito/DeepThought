package net.dankito.service.synchronization

import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.DiscoveredDevice


interface IConnectedDevicesService {

    fun start()

    fun stop()


    fun startSynchronizingWithNewlyRegisteredDevice(device: DiscoveredDevice)

    fun stopSynchronizingWithDevice(device: DiscoveredDevice)

    fun addDeviceToIgnoreList(device: DiscoveredDevice)

    fun startSynchronizingWithIgnoredDevice(device: DiscoveredDevice)


    fun addDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean

    fun removeDiscoveredDevicesListener(listener: DiscoveredDevicesListener): Boolean

    fun addKnownSynchronizedDevicesListener(listener: KnownSynchronizedDevicesListener): Boolean

    fun removeKnownSynchronizedDevicesListener(listener: KnownSynchronizedDevicesListener): Boolean


    fun getDiscoveredDeviceForDevice(device: Device): DiscoveredDevice?

    fun getDiscoveredDeviceForId(deviceId: String): DiscoveredDevice?

    val allDiscoveredDevices: List<DiscoveredDevice>

    val knownSynchronizedDiscoveredDevices: List<DiscoveredDevice>

    val knownIgnoredDiscoveredDevices: List<DiscoveredDevice>

    val unknownDiscoveredDevices: List<DiscoveredDevice>

}

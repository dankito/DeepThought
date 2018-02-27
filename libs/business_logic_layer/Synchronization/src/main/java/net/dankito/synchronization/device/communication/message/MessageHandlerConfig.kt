package net.dankito.synchronization.device.communication.message

import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.device.communication.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.NetworkSettings


data class MessageHandlerConfig(val entityManager: IEntityManager, val networkSettings: NetworkSettings, val challengeHandler: ChallengeHandler, val registrationHandler: IDeviceRegistrationHandler) {

    private val remoteRequestedToStartSynchronizationListeners = HashSet<(DiscoveredDevice) -> Unit>()


    fun addRemoteRequestedToStartSynchronizationListener(listener: (DiscoveredDevice) -> Unit) {
        remoteRequestedToStartSynchronizationListeners.add(listener)
    }

    fun callRemoteRequestedToStartSynchronizationListeners(device: DiscoveredDevice) {
        remoteRequestedToStartSynchronizationListeners.forEach { it(device) }
    }

}

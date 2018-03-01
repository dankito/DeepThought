package net.dankito.synchronization.device.messaging.message

import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
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

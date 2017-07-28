package net.dankito.data_access.network.communication.message

import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings


data class MessageHandlerConfig(val entityManager: IEntityManager, val networkSettings: INetworkSettings, val challengeHandler: ChallengeHandler, val registrationHandler: IDeviceRegistrationHandler) {

    private val remoteRequestedToStartSynchronizationListeners = HashSet<(DiscoveredDevice) -> Unit>()


    fun addRemoteRequestedToStartSynchronizationListener(listener: (DiscoveredDevice) -> Unit) {
        remoteRequestedToStartSynchronizationListeners.add(listener)
    }

    fun callRemoteRequestedToStartSynchronizationListeners(device: DiscoveredDevice) {
        remoteRequestedToStartSynchronizationListeners.forEach { it(device) }
    }

}
